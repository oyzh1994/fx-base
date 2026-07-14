package cn.oyzh.fx.plus.controls.hex;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hex 查看器核心组件 — 三列布局（偏移 | 十六进制 | 原始文本）
 * Canvas 渲染 + 垂直滚动条 + 大文件窗口缓存 + 拖动区域选择。
 *
 * @author oyzh
 * @since 2026/06/20
 */
public class HexView extends FXVBox implements Destroyable {

    // ====== 暗色主题 ======
    private static final Color D_BG = Color.web("#1e1e1e");
    private static final Color D_OFFSET = Color.web("#808080");
    private static final Color D_HEX = Color.web("#d4d4d4");
    private static final Color D_HEX_HOVER = Color.web("#4fc1ff");
    private static final Color D_TEXT = Color.web("#d4d4d4");
    private static final Color D_TEXT_DOT = Color.web("#555555");
    private static final Color D_SEL = Color.web("#264f78");
    private static final Color D_SEL_EDGE = Color.web("#3a7fca");
    private static final Color D_LINE_BG = Color.web("#2a2d2e");
    private static final Color D_DIV = Color.web("#4a4a4a");
    private static final Color D_FOCUS = Color.web("#4fc1ff");
    // ====== 亮色主题 ======
    private static final Color L_BG = Color.web("#ffffff");
    private static final Color L_OFFSET = Color.web("#888888");
    private static final Color L_HEX = Color.web("#333333");
    private static final Color L_HEX_HOVER = Color.web("#0066cc");
    private static final Color L_TEXT = Color.web("#333333");
    private static final Color L_TEXT_DOT = Color.web("#cccccc");
    private static final Color L_SEL = Color.web("#b4d5ff");
    private static final Color L_SEL_EDGE = Color.web("#3388ee");
    private static final Color L_LINE_BG = Color.web("#f0f0f0");
    private static final Color L_DIV = Color.web("#cccccc");
    private static final Color L_FOCUS = Color.web("#0066cc");

    private int bytesPerRow = 32; // 可切换: 8 / 16 / 32
    private static final int[] bytesPerRow_OPTIONS = {8, 16, 32};
    private static final int PAD_LEFT = 10, PAD_TOP = 4;
    private static final int CACHE_SIZE = 65536;

    private Font font;
    private double charW, lineH;
    private double hexX, hexGapX, divX, textX;
    private static final double OFFSET_X = PAD_LEFT;

    // ====== 多区域选择 ======
    private final List<long[]> selections = new ArrayList<>(); // 每个区域 {lo, hi}
    // 拖动起始字节
    private long dragAnchor = -1;
    // 拖动当前字节（-1=无拖拽）
    private long dragCurrent = -1;
    // 键盘焦点
    private long focusByte = -1;
    private long hoverByte = -1;
    // 0=offset, 1=hex, 2=text
    private int hoverRegion = -1;
    // 上次单击时间（双击检测用）
    private long clickTime;
    // 上次单击的字节
    private long clickOff = -1;
    // 拖动刚结束，跳过 onClicked 处理
    private boolean dragFinished;

    // 内存数据源（优先于文件）
    private byte[] dataBytes;
    private FileChannel fileChannel;
    private RandomAccessFile raf;
    private long fileSize;
    private ByteBuffer cacheBuf;
    private long cacheStart = -1;

    private Canvas canvas;
    private ScrollBar scrollBar;
    private long rowsPerPage;
    private long scrollPos;

    public HexView() {
        this.initFont();

        canvas = new Canvas();
        canvas.setFocusTraversable(true);
        canvas.addEventFilter(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
        canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, this::onMouseReleased);
        canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);
        canvas.setOnMouseExited(e -> {
            hoverByte = -1;
            hoverRegion = -1;
            repaint();
        });
        canvas.setOnScroll(this::onScrollWheel);
        canvas.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);

        scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setMin(0);
        scrollBar.setMax(0);
        scrollBar.setVisibleAmount(0);
        scrollBar.valueProperty().addListener((o, ov, n) -> {
            if (n.intValue() != scrollPos) {
                scrollPos = n.intValue();
                repaint();
            }
        });

        FXHBox row = new FXHBox(canvas, scrollBar);
        HBox.setHgrow(canvas, Priority.ALWAYS);
        VBox.setVgrow(row, Priority.ALWAYS);
        getChildren().add(row);

        row.prefWidthProperty().bind(this.widthProperty());
        row.prefHeightProperty().bind(this.heightProperty());
        canvas.widthProperty().bind(row.widthProperty().subtract(scrollBar.widthProperty()));
        canvas.heightProperty().bind(row.heightProperty());
        canvas.widthProperty().addListener((o, ov, nv) -> {
            computeRows();
            repaint();
        });
        canvas.heightProperty().addListener((o, ov, nv) -> {
            computeRows();
            repaint();
        });
        applyThemeColors();
    }

    private void recalcLayout() {
        hexX = OFFSET_X + 8 * charW + 12;
        hexGapX = charW * 1.5;
        double hexBlockW = bytesPerRow * 2.5 * charW + ((bytesPerRow - 1) / 8) * hexGapX;
        textX = hexX + hexBlockW + 12;   // hex 尾到 text 首固定 12px
        divX = hexX + hexBlockW + 5;    // 分隔线靠近 hex 侧
    }

    /**
     * 初始化字体
     */
    private void initFont() {
        font = Font.font("Consolas", FontManager.currentFontSize());
        Text measure = new Text("0123456789ABCDEF");
        measure.setFont(font);
        measure.applyCss();
        double totalW = measure.getLayoutBounds().getWidth();
        double totalH = measure.getLayoutBounds().getHeight();
        charW = totalW / 16.0;
        // 用文本实际高度 + PAD_TOP 保证上下间距一致，避免选中行内容被遮盖
        lineH = totalH + PAD_TOP;
        this.recalcLayout();
    }

    // ====== 状态信息 API ======
    public int getBytesPerRow() {
        return bytesPerRow;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getFocusByte() {
        return focusByte;
    }

    public long getSelectionSize() {
        long total = 0;
        for (long[] s : selections) {
            total += (s[1] - s[0] + 1);
        }
        if (dragAnchor >= 0 && dragCurrent >= 0 && dragAnchor != dragCurrent) {
            total += Math.abs(dragCurrent - dragAnchor) + 1;
        }
        return total;
    }

    public String getSelectionHex() {
        StringBuilder sb = new StringBuilder();
        for (long[] s : selections) {
            for (long i = s[0]; i <= s[1]; i++) {
                sb.append(String.format("%02X ", readByte(i)));
            }
        }
        return sb.toString().trim();
    }

    public String getSelectionText() {
        StringBuilder sb = new StringBuilder();
        for (long[] s : selections) {
            for (long i = s[0]; i <= s[1]; i++) {
                int b = readByte(i) & 0xFF;
                sb.append((b >= 32 && b < 127) ? (char) b : '.');
            }
        }
        return sb.toString();
    }

    private byte readByte(long off) {
        byte[] d = readBytes(off, 1);
        return d.length > 0 ? d[0] : 0;
    }

    // ====== 功能 API ======

    /**
     * 跳转到指定偏移
     */
    public void gotoOffset(long off) {
        if (fileSize == 0) {
            return;
        }
        off = Math.max(0, Math.min(off, fileSize - 1));
        focusByte = off;
        long line = off / bytesPerRow;
        if (line < scrollPos || line >= scrollPos + rowsPerPage) {
            scrollPos = (int) Math.max(0, line - rowsPerPage / 2);
            updateScrollBar();
        }
        repaint();
    }

    /**
     * 切换每行字节数
     */
    public void toggleBytesPerRow() {
        int idx = Arrays.binarySearch(bytesPerRow_OPTIONS, bytesPerRow);
        idx = (idx + 1) % bytesPerRow_OPTIONS.length;
        bytesPerRow = bytesPerRow_OPTIONS[idx];
        recalcLayout();
        updateScrollBar();
        repaint();
    }

    /**
     * 搜索字节序列，返回第一个匹配的偏移，-1 表示未找到
     */
    public long search(byte[] pattern, long startOff) {
        if (fileSize == 0 || pattern.length == 0) {
            return -1;
        }
        for (long i = startOff; i <= fileSize - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (readByte(i + j) != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 搜索文本，返回第一个匹配的偏移
     */
    public long search(String text, long startOff) {
        return search(text.getBytes(java.nio.charset.StandardCharsets.UTF_8), startOff);
    }

    /**
     * 搜索十六进制字符串 (如 "FF 00 AB")
     */
    public long searchHex(String hex, long startOff) {
        String[] parts = hex.trim().split("\\s+");
        byte[] pattern = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            pattern[i] = (byte) Integer.parseInt(parts[i], 16);
        }
        return search(pattern, startOff);
    }

    public Color getStatusBg() {
        return ThemeManager.isDarkMode() ? Color.web("#252525") : Color.web("#e0e0e0");
    }

    public Color getStatusFg() {
        return ThemeManager.isDarkMode() ? Color.web("#808080") : Color.web("#555555");
    }

    private void applyThemeColors() {
        Color bg = ThemeManager.isDarkMode() ? D_BG : L_BG;
        Background background = ControlUtil.background(bg);
        this.setBackground(background);
    }

    // ====== 颜色快捷方法 ======
    private Color bg() {
        return ThemeManager.isDarkMode() ? D_BG : L_BG;
    }

    private Color offC() {
        return ThemeManager.isDarkMode() ? D_OFFSET : L_OFFSET;
    }

    private Color hexC() {
        return ThemeManager.isDarkMode() ? D_HEX : L_HEX;
    }

    private Color hexHC() {
        return ThemeManager.isDarkMode() ? D_HEX_HOVER : L_HEX_HOVER;
    }

    private Color txtC() {
        return ThemeManager.isDarkMode() ? D_TEXT : L_TEXT;
    }

    private Color dotC() {
        return ThemeManager.isDarkMode() ? D_TEXT_DOT : L_TEXT_DOT;
    }

    private Color selBg() {
        return ThemeManager.isDarkMode() ? D_SEL : L_SEL;
    }

    private Color selEdge() {
        return ThemeManager.isDarkMode() ? D_SEL_EDGE : L_SEL_EDGE;
    }

    private Color lnBg() {
        return ThemeManager.isDarkMode() ? D_LINE_BG : L_LINE_BG;
    }

    private Color divC() {
        return ThemeManager.isDarkMode() ? D_DIV : L_DIV;
    }

    private Color focC() {
        return ThemeManager.isDarkMode() ? D_FOCUS : L_FOCUS;
    }

    //    private static String toWeb(Color c) {
    //        return String.format("#%02X%02X%02X", (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
    //    }

    /**
     * 判断 offset 是否在任意选中区域内（包括当前拖动中的区域）
     */
    private boolean inSelection(long off) {
        // 检查当前拖动
        if (dragAnchor >= 0 && dragCurrent >= 0) {
            long lo = Math.min(dragAnchor, dragCurrent), hi = Math.max(dragAnchor, dragCurrent);
            if (off >= lo && off <= hi) {
                return true;
            }
        }
        for (long[] s : selections) {
            if (off >= s[0] && off <= s[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找 offset 所属的已固定选中区域索引，没有则返回 -1
     */
    private int findSelection(long off) {
        for (int i = 0; i < selections.size(); i++) {
            long[] s = selections.get(i);
            if (off >= s[0] && off <= s[1]) {
                return i;
            }
        }
        return -1;
    }

    //    /**
    //     * 添加一个选区并合并重叠
    //     */
    //    private void addSelection(long lo, long hi) {
    //        if (lo > hi) {
    //            long t = lo;
    //            lo = hi;
    //            hi = t;
    //        }
    //        // 先移除与新区间重叠的区域
    //        long finalLo = lo;
    //        long finalHi = hi;
    //        selections.removeIf(s -> s[1] >= finalLo && s[0] <= finalHi);
    //        selections.add(new long[]{lo, hi});
    //    }

    // ====== 数据源 API ======

    /**
     * 查看 byte[] 数组
     */
    public void openBytes(byte[] bytes) {
        close();
        this.dataBytes = bytes;
        this.fileSize = bytes.length;
        selections.clear();
        dragAnchor = dragCurrent = -1;
        focusByte = 0;
        hoverByte = -1;
        computeRows();
        updateScrollBar();
        repaint();
    }

    /**
     * 查看 ByteBuffer（复制内部数据）
     */
    public void openBytes(ByteBuffer buf) {
        byte[] bytes = new byte[buf.remaining()];
        buf.duplicate().get(bytes);
        openBytes(bytes);
    }

    /**
     * 查看 InputStream（读取全部字节）
     */
    public void openStream(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        openBytes(bos.toByteArray());
    }

    /**
     * 查看文件
     */
    public void openFile(File file) throws IOException {
        close();
        raf = new RandomAccessFile(file, "r");
        fileChannel = raf.getChannel();
        fileSize = fileChannel.size();
        cacheBuf = ByteBuffer.allocate(CACHE_SIZE);
        selections.clear();
        dragAnchor = dragCurrent = -1;
        focusByte = 0;
        hoverByte = -1;
        computeRows();
        updateScrollBar();
        repaint();
    }

    public void close() {
        try {
            if (fileChannel != null) {
                fileChannel.close();
            }
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ignored) {
        }
        fileChannel = null;
        raf = null;
        cacheBuf = null;
        cacheStart = -1;
        dataBytes = null;
        fileSize = 0;
        focusByte = -1;
        selections.clear();
        dragAnchor = dragCurrent = -1;
        repaint();
    }

    private byte[] readBytes(long offset, int len) {
        // 内存数据源
        if (dataBytes != null) {
            int clamped = (int) Math.min(len, fileSize - offset);
            if (clamped <= 0) {
                return new byte[len];
            }
            byte[] data = new byte[len];
            System.arraycopy(dataBytes, (int) offset, data, 0, clamped);
            return data;
        }
        // 文件数据源
        if (fileChannel == null || len <= 0) {
            return new byte[len];
        }
        int clamped = (int) Math.min(len, fileSize - offset);
        if (clamped <= 0) {
            return new byte[len];
        }
        try {
            if (cacheStart >= 0 && offset >= cacheStart && offset + clamped <= cacheStart + CACHE_SIZE) {
                int off = (int) (offset - cacheStart);
                byte[] data = new byte[len];
                System.arraycopy(cacheBuf.array(), off, data, 0, clamped);
                return data;
            }
            cacheStart = offset;
            cacheBuf.clear();
            fileChannel.position(offset);
            fileChannel.read(cacheBuf);
            cacheBuf.flip();
            byte[] data = new byte[len];
            System.arraycopy(cacheBuf.array(), 0, data, 0, clamped);
            return data;
        } catch (Exception e) {
            return new byte[len];
        }
    }

    private void computeRows() {
        rowsPerPage = Math.max(1, (int) ((canvas.getHeight() - PAD_TOP * 2) / lineH));
    }

    private void updateScrollBar() {
        if (fileSize == 0) {
            scrollBar.setMax(0);
            scrollBar.setVisibleAmount(0);
            scrollBar.setValue(0);
            scrollBar.setVisible(false);
            scrollBar.setManaged(false);
            return;
        }
        int totalRows = (int) ((fileSize + bytesPerRow - 1) / bytesPerRow);
        boolean needScroll = totalRows > rowsPerPage;
        scrollBar.setVisible(needScroll);
        scrollBar.setManaged(needScroll);
        if (needScroll) {
            double max = Math.max(0, totalRows - rowsPerPage);
            scrollBar.setMax(max);
            scrollBar.setVisibleAmount(rowsPerPage);
            scrollBar.setValue(Math.min(scrollPos, max));
            scrollPos = (int) scrollBar.getValue();
        }
    }

    /**
     * 根据鼠标坐标解析字节偏移
     */
    private long resolveByteAt(double mx, double my) {
        int row = (int) ((my - PAD_TOP) / lineH);
        long lineOff = (scrollPos + row) * (long) bytesPerRow;
        if (lineOff < 0 || lineOff >= fileSize) {
            return -1;
        }
        int n = (int) Math.min(bytesPerRow, fileSize - lineOff);
        long off;
        if (mx >= OFFSET_X && mx < hexX - 5) {
            off = lineOff;
        } else if (mx >= hexX && mx < divX) {
            double cw = charW * 2.5;
            double hx = mx - hexX;
            int gi = (int) (hx / cw);
            int gaps = gi / 8;
            gi = (int) ((hx - gaps * hexGapX) / cw);
            gi = Math.max(0, Math.min(gi, n - 1));
            off = lineOff + gi;
        } else if (mx >= textX) {
            int ti = (int) ((mx - textX) / charW);
            ti = Math.min(ti, n - 1);
            off = lineOff + ti;
        } else {
            off = -1;
        }
        return off < fileSize ? off : fileSize - 1;
    }

    // ====== 绘制 ======
    private void repaint() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) {
            return;
        }
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFont(font);
        g.setTextBaseline(VPos.TOP);
        g.setFill(bg());
        g.fillRect(0, 0, w, h);

        if (fileSize == 0) {
            g.setFill(offC());
            return;
        }
        computeRows();
        long totalRows = (fileSize + bytesPerRow - 1) / bytesPerRow;
        if (scrollPos > totalRows - rowsPerPage) {
            scrollPos = Math.max(0, (int) (totalRows - rowsPerPage));
            scrollBar.setValue(scrollPos);
        }
        for (int row = 0; row < rowsPerPage; row++) {
            long lineOff = ((scrollPos + row) * (long) bytesPerRow);
            if (lineOff >= fileSize) {
                break;
            }
            int n = (int) Math.min(bytesPerRow, fileSize - lineOff);
            byte[] data = readBytes(lineOff, n);
            double y = PAD_TOP + row * lineH;

            // 整行选中的背景
            boolean rowHasFocus = (focusByte >= lineOff && focusByte < lineOff + n);
            if (rowHasFocus) {
                g.setFill(lnBg());
                g.fillRect(0, y - 1, w, lineH);
            }

            drawOffset(g, lineOff, y);
            drawHex(g, data, lineOff, y);
            g.setStroke(divC());
            g.setLineWidth(1);
            g.strokeLine(divX, y - 1, divX, y + lineH - 1);
            drawText(g, data, lineOff, y);
        }
        // 选择区域边缘线（仅当有选择时绘制）
        this.drawSelectionEdges(g);
        // 焦点虚线框
        if (focusByte >= 0 && canvas.isFocused()) {
            long focusLine = focusByte / bytesPerRow;
            int focusRow = (int) (focusLine - scrollPos);
            if (focusRow >= 0 && focusRow < rowsPerPage) {
                g.setStroke(focC());
                g.setLineWidth(1);
                g.setLineDashes(4);
                g.strokeRect(0.5, PAD_TOP + focusRow * lineH - 1, w - 1, lineH);
                g.setLineDashes();
            }
        }
    }

    /**
     * 绘制所有选中区域的边界线
     */
    private void drawSelectionEdges(GraphicsContext g) {
        // 绘制当前拖动
        if (dragAnchor >= 0 && dragCurrent >= 0 && dragAnchor != dragCurrent) {
            drawSingleEdge(g, Math.min(dragAnchor, dragCurrent), Math.max(dragAnchor, dragCurrent));
        }
        // 绘制已固定选区
        for (long[] s : selections) {
            if (s[0] != s[1]) {
                drawSingleEdge(g, s[0], s[1]);
            }
        }
    }

    private void drawSingleEdge(GraphicsContext g, long lo, long hi) {
        double cw = charW * 2.5;
        // 起始边界
        long loLine = lo / bytesPerRow;
        int loRow = (int) (loLine - scrollPos);
        if (loRow >= 0 && loRow < rowsPerPage) {
            int loCol = (int) (lo % bytesPerRow);
            g.setStroke(selEdge());
            g.setLineWidth(1.5);
            double x1 = hexX + loCol * cw + (loCol / 8) * hexGapX;
            g.strokeLine(x1 - 1, PAD_TOP + loRow * lineH - 2, x1 - 1, PAD_TOP + loRow * lineH + lineH);
        }
        // 结束边界
        long hiLine = hi / bytesPerRow;
        int hiRow = (int) (hiLine - scrollPos);
        if (hiRow >= 0 && hiRow < rowsPerPage) {
            int hiCol = (int) (hi % bytesPerRow);
            g.setStroke(selEdge());
            g.setLineWidth(1.5);
            double x2 = hexX + (hiCol + 1) * cw + ((hiCol + 1) / 8) * hexGapX;
            g.strokeLine(x2 + 0.5, PAD_TOP + hiRow * lineH - 2, x2 + 0.5, PAD_TOP + hiRow * lineH + lineH);
        }
    }

    private void drawOffset(GraphicsContext g, long off, double y) {
        // 任意列悬停到该行任意字节时，偏移列同步高亮
        boolean rowHovered = (hoverByte >= off && hoverByte < off + bytesPerRow);
        long lineStart = off, lineEnd = off + bytesPerRow - 1;
        boolean lineSelected = false;
        for (long i = lineStart; i <= lineEnd && i < fileSize; i++) {
            if (this.inSelection(i)) {
                lineSelected = true;
                break;
            }
        }
        if (lineSelected) {
            g.setFill(selBg());
            g.fillRect(OFFSET_X - 2, y, 8 * charW + 6, lineH);
        }
        g.setFill(rowHovered ? hexHC() : offC());
        g.fillText(String.format("%08X", off), OFFSET_X, y);
    }

    private void drawHex(GraphicsContext g, byte[] data, long lineOff, double y) {
        double cx = hexX;
        for (int i = 0; i < data.length; i++) {
            long off = lineOff + i;
            double cw = charW * 2.5;
            boolean sel = this.inSelection(off);
            boolean hov = (hoverByte == off);
            if (sel) {
                g.setFill(selBg());
                g.fillRect(cx - 0.5, y, cw, lineH);
            }
            g.setFill(hov ? hexHC() : hexC());
            g.fillText(String.format("%02X", data[i] & 0xFF), cx, y);
            cx += cw;
            if ((i + 1) % 8 == 0 && i < data.length - 1) {
                cx += hexGapX;
            }
        }
    }

    private void drawText(GraphicsContext g, byte[] data, long lineOff, double y) {
        double cx = textX;
        for (int i = 0; i < bytesPerRow; i++) {
            long off = lineOff + i;
            boolean sel = this.inSelection(off);
            boolean hov = (hoverByte == off);
            boolean printable = i < data.length && (data[i] & 0xFF) >= 32 && (data[i] & 0xFF) < 127;
            String ch = i < data.length ? (printable ? String.valueOf((char) (data[i] & 0xFF)) : ".") : " ";
            if (sel) {
                g.setFill(selBg());
                g.fillRect(cx, y, charW, lineH);
            }
            g.setFill(hov ? hexHC() : (printable ? txtC() : dotC()));
            g.fillText(ch, cx, y);
            cx += charW;
        }
    }

    // ====== 鼠标 ======
    private void onMouseMoved(MouseEvent e) {
        if (fileSize == 0) {
            return;
        }
        hoverByte = resolveByteAt(e.getX(), e.getY());
        repaint();
    }

    private void onMousePressed(MouseEvent e) {
        this.requestFocus();
        // TabPane 等父容器可能在事件冒泡阶段把焦点抢走，
        // 用 runLater 在当前事件完全处理完后重新请求焦点
//        FXUtil.runLater(() -> canvas.requestFocus());
        if (e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        long off = resolveByteAt(e.getX(), e.getY());
        if (off < 0) {
            return;
        }
        focusByte = off;
        dragAnchor = (e.isShiftDown() && clickOff >= 0) ? clickOff : off;
        dragCurrent = off;
        repaint();
    }

    private void onMouseDragged(MouseEvent e) {
        if (dragAnchor < 0 || e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        long off = resolveByteAt(e.getX(), e.getY());
        if (off < 0) {
            return;
        }
        dragCurrent = off;
        focusByte = off;
        repaint();
    }

    private void onMouseReleased(MouseEvent e) {
        if (dragAnchor < 0) {
            return;
        }
        long lo = Math.min(dragAnchor, dragCurrent);
        long hi = Math.max(dragAnchor, dragCurrent);
        if (lo != hi) {
            // 拖动选区：如果覆盖已有选区则移除之，否则加入
            boolean removed = selections.removeIf(s -> s[1] >= lo && s[0] <= hi);
            if (!removed) {
                selections.add(new long[]{lo, hi});
            }
            clickOff = hi;
            dragFinished = true; // 标记刚完成拖动，阻止 onMouseClicked 误删
        } else {
            dragFinished = false;
            clickOff = hi;
        }
        dragAnchor = dragCurrent = -1;
        repaint();
    }

    private void onMouseClicked(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY || hoverByte < 0) {
            return;
        }
        // 拖动刚完成 → 跳过单击处理，避免误删刚创建的选区
        if (dragFinished) {
            dragFinished = false;
            clickOff = hoverByte;
            return;
        }
        long now = System.currentTimeMillis();
        if (now - clickTime < 400 && hoverByte == clickOff) {
            // 双击：如果不在选区内 → 选当前行
            int idx = findSelection(hoverByte);
            if (idx < 0) {
                long lineStart = (hoverByte / bytesPerRow) * bytesPerRow;
                long lineEnd = Math.min(lineStart + bytesPerRow - 1, fileSize - 1);
                selections.add(new long[]{lineStart, lineEnd});
            }
            clickTime = 0;
        } else {
            // 单击：点在已选区内 → 取消该选区；否则只设焦点
            int idx = findSelection(hoverByte);
            if (idx >= 0) {
                selections.remove(idx);
            }
            focusByte = hoverByte;
            clickTime = now;
        }
        clickOff = hoverByte;
        repaint();
    }

    private void onScrollWheel(ScrollEvent e) {
        double units = e.getDeltaY() / (lineH * 2);
        scrollPos = (int) Math.max(0, Math.min(scrollBar.getMax(), scrollPos - units));
        scrollBar.setValue(scrollPos);
        repaint();
    }

    //    // ====== 回调 ======
    //    private Runnable onGotoRequest, onSearchRequest, onCopyRequest;
    //
    //    public void setOnGotoRequest(Runnable r) {
    //        onGotoRequest = r;
    //    }
    //
    //    public void setOnSearchRequest(Runnable r) {
    //        onSearchRequest = r;
    //    }
    //
    //    public void setOnCopyRequest(Runnable r) {
    //        onCopyRequest = r;
    //    }

    // ====== 键盘 ======
    private void onKeyPressed(KeyEvent e) {
        if (fileSize == 0) {
            return;
        }
        long total = fileSize;
        // 全局快捷键（即使有数据也要处理）
        if (KeyboardUtil.isMainModifierDown(e)) {
            switch (e.getCode()) {
                //                case G -> {
                //                    if (onGotoRequest != null) {
                //                        onGotoRequest.run();
                //                    }
                //                    e.consume();
                //                    return;
                //                }
                //                case F -> {
                //                    if (onSearchRequest != null) {
                //                        onSearchRequest.run();
                //                    }
                //                    e.consume();
                //                    return;
                //                }
                case C -> {
                    String hexText = this.getSelectionHex();
                    ClipboardUtil.setString(hexText);
                    e.consume();
                    return;
                }
                case A -> {
                    this.selections.clear();
                    this.selections.add(new long[]{0, total - 1});
                    e.consume();
                    repaint();
                    return;
                }
            }
        }
        if (e.getCode() == KeyCode.F2) {
            toggleBytesPerRow();
            e.consume();
            return;
        }
        if (e.getCode() == KeyCode.ESCAPE) {
            selections.clear();
            dragAnchor = dragCurrent = -1;
            e.consume();
            repaint();
            return;
        }
        switch (e.getCode()) {
            case DOWN -> focusByte = Math.min(total - 1, focusByte + bytesPerRow);
            case UP -> focusByte = Math.max(0, focusByte - bytesPerRow);
            case RIGHT -> focusByte = Math.min(total - 1, focusByte + 1);
            case LEFT -> focusByte = Math.max(0, focusByte - 1);
            case PAGE_DOWN -> {
                focusByte = Math.min(total - 1, focusByte + rowsPerPage * bytesPerRow);
                scrollPos = (int) Math.min(scrollBar.getMax(), scrollPos + rowsPerPage);
                scrollBar.setValue(scrollPos);
            }
            case PAGE_UP -> {
                focusByte = Math.max(0, focusByte - rowsPerPage * bytesPerRow);
                scrollPos = Math.max(0, scrollPos - rowsPerPage);
                scrollBar.setValue(scrollPos);
            }
            case HOME -> {
                focusByte = 0;
                scrollPos = 0;
                scrollBar.setValue(0);
            }
            case END -> {
                focusByte = total - 1;
                scrollPos = (int) scrollBar.getMax();
                scrollBar.setValue(scrollPos);
            }
            default -> {
                return;
            }
        }
        if (e.isShiftDown()) {
            // Shift+方向键：扩展选区
            dragAnchor = (clickOff >= 0) ? clickOff : focusByte;
            dragCurrent = focusByte;
        } else {
            dragAnchor = dragCurrent = -1;
        }
        long focusLine = focusByte / bytesPerRow;
        if (focusLine < scrollPos) {
            scrollPos = (int) focusLine;
            scrollBar.setValue(scrollPos);
        }
        if (focusLine >= scrollPos + rowsPerPage) {
            scrollPos = (int) (focusLine - rowsPerPage + 1);
            scrollBar.setValue(scrollPos);
        }
        e.consume();
        repaint();
    }

    @Override
    public void changeFont(Font font) {
        //        super.changeFont(font);
        this.initFont();
    }

    @Override
    public void changeTheme(ThemeStyle style) {
        super.changeTheme(style);
        this.applyThemeColors();
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        FXUtil.runLater(this.canvas::requestFocus);
    }

    @Override
    public void destroy() {
        this.close();
        this.canvas = null;
        this.scrollBar = null;
    }
}
