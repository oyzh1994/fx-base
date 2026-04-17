package cn.oyzh.fx.plus.test;

import javafx.scene.input.KeyCode;

import java.awt.*;

/**
 *
 * @author oyzh
 * @since 2026-04-16
 */
public class KeyboardTest {

    public static void main(String[] args) {
        try {
            // 456
            Robot robot = new Robot();
            robot.delay(3000);
            robot.keyPress(KeyCode.SHIFT.getCode());
            robot.keyPress(KeyCode.LEFT.getCode());
            robot.keyRelease(KeyCode.LEFT.getCode());
            robot.keyPress(KeyCode.LEFT.getCode());
            robot.keyRelease(KeyCode.LEFT.getCode());
            robot.keyPress(KeyCode.LEFT.getCode());
            robot.keyRelease(KeyCode.LEFT.getCode());
            robot.keyRelease(KeyCode.SHIFT.getCode());
            robot.keyPress(KeyCode.META.getCode());
            robot.keyPress(KeyCode.V.getCode());
            robot.keyRelease(KeyCode.V.getCode());
            robot.keyRelease(KeyCode.META.getCode());

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
