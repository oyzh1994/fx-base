package cn.oyzh.fx.common.exception;


import cn.oyzh.fx.common.util.ArrayUtil;

/**
 * @author oyzh
 * @since 2024-09-26
 */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException() {
        super("Data is not valid");
    }

    public InvalidDataException(String... param) {
        super("Data %s is not valid".formatted(ArrayUtil.toString(param)));
    }
}
