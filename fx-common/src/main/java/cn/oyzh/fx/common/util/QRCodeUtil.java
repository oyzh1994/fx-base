//package cn.oyzh.fx.common.util;
//
//
//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
//import lombok.NonNull;
//import lombok.experimental.UtilityClass;
//import lombok.extern.slf4j.Slf4j;
//
//import java.awt.*;
//import java.awt.geom.RoundRectangle2D;
//import java.awt.image.BufferedImage;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 二维码工具类
// *
// * @author oyzh
// * @since 2023/02/28
// */
//@Slf4j
//@UtilityClass
//public class QRCodeUtil {
//
//    /**
//     * 默认字符集
//     */
//    private static final String CHARSET = "utf-8";
//
//    /**
//     * 生成二维码
//     *
//     * @param content 内容
//     * @param w       宽
//     * @param h       高
//     * @return 二维码图片
//     * @throws Exception 异常
//     */
//    public static BufferedImage createImage(@NonNull String content, int w, int h) throws Exception {
//        Map<EncodeHintType, Object> hints = new HashMap<>();
//        hints.put(EncodeHintType.MARGIN, 1);
//        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
//        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
//        log.info("encode before.");
//        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, w, h, hints);
//        int width = bitMatrix.getWidth();
//        int height = bitMatrix.getHeight();
//        log.info("createImage before.");
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        for (int x = 0; x < width; x++) {
//            for (int y = 0; y < height; y++) {
//                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
//            }
//        }
//        log.info("createImage finish.");
//        return image;
//    }
//
//    /**
//     * 插入图片
//     *
//     * @param source 源图
//     * @param icon   二维码图片
//     */
//    public static void insertImage(@NonNull BufferedImage source, @NonNull Image icon) {
//        insertImage(source, icon, true, 50, 50);
//    }
//
//    /**
//     * 插入图片
//     *
//     * @param source       源图
//     * @param icon         二维码图片
//     * @param needCompress 是否需要压缩
//     * @param iconW        二维码图片宽
//     * @param iconH        二维码图片高
//     */
//    public static void insertImage(@NonNull BufferedImage source, @NonNull Image icon, boolean needCompress, int iconW, int iconH) {
//        int w = source.getWidth();
//        int h = source.getHeight();
//        int height = icon.getHeight(null);
//        int width = icon.getWidth(null);
//        // 压缩LOGO
//        if (needCompress) {
//            if (height > iconH) {
//                height = iconH;
//            }
//            if (width > iconW) {
//                width = iconW;
//            }
//            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//            Image image = icon.getScaledInstance(width, height, Image.SCALE_SMOOTH);
//            Graphics g = tag.getGraphics();
//            // 绘制缩小后的图
//            g.drawImage(image, 0, 0, null);
//            g.dispose();
//            icon = image;
//        }
//        // 插入LOGO
//        Graphics2D graph = source.createGraphics();
//        int x = (w - width) / 2;
//        int y = (h - height) / 2;
//        graph.drawImage(icon, x, y, width, height, null);
//        Shape shape = new RoundRectangle2D.Float(x, y, width, width, 6, 6);
//        graph.setStroke(new BasicStroke(3f));
//        graph.draw(shape);
//        graph.dispose();
//    }
//}
