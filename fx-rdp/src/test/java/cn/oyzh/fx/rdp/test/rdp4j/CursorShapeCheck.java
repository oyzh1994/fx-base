package cn.oyzh.fx.rdp.test.rdp4j;

import com.tangluobo.rdp4j.frontend.FxRdpDisplay;

/**
 * 指针形状判定自检（无需测试框架，直接运行main）。
 *
 * <p>判定逻辑用于区分“guest自己画的软件指针”与“应用界面重绘”：
 * 前者是随指针位置整体平移的图标，后者会铺满控件（如菜单项高亮）。
 *
 * @author oyzh
 * @since 2026-09-20
 */
public class CursorShapeCheck {

    /** 帧补丁半径24，即49x49的取样窗口 */
    private static final int PATCH_PIXELS = 49 * 49;

    private static int failures;

    static void main(String[] args) {
        // 24x24 是 Ubuntu(GNOME)默认光标主题尺寸，也是嵌套虚拟机旧版软件指针的尺寸
        int width = 24;
        int height = 24;

        // 真正的软件指针：旧位置擦除、新位置重绘，变化范围不超过指针矩形
        expect("指针整体平移", 576, 24, 24, width, height, true);
        expect("指针带阴影/抗锯齿", 700, 28, 26, width, height, true);
        expect("Windows 32px 指针", 1024, 32, 32, 32, 32, true);

        // Ubuntu 下拉组件：悬停高亮铺满整个取样窗口宽度，不是指针
        expect("菜单项高亮整行", 980, 49, 20, width, height, false);
        expect("菜单项高亮整行(22px)", PATCH_PIXELS * 45 / 100, 49, 22, width, height, false);
        expect("整块窗口重绘", PATCH_PIXELS, 49, 49, width, height, false);
        expect("超出指针尺寸的大面积重绘", 2000, 30, 30, width, height, false);
        expect("无变化", 0, 0, 0, width, height, false);

        if (failures == 0) {
            System.out.println("ALL OK");
        } else {
            System.out.println(failures + " 项失败");
            System.exit(1);
        }
    }

    private static void expect(String what, int changed, int boxWidth, int boxHeight,
                               int cursorWidth, int cursorHeight, boolean expected) {
        boolean actual = FxRdpDisplay.isCursorShapedChange(changed, boxWidth, boxHeight,
                cursorWidth, cursorHeight);
        boolean ok = actual == expected;
        if (!ok) {
            failures++;
        }
        System.out.printf("%-32s 变化=%4d 范围=%2dx%-2d 结果=%-5s 期望=%-5s %s%n",
                what, changed, boxWidth, boxHeight, actual, expected, ok ? "OK" : "FAIL");
    }
}
