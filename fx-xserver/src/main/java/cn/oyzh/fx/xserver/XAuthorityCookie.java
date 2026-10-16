package cn.oyzh.fx.xserver;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Creates and encodes standard {@code MIT-MAGIC-COOKIE-1} values.
 *
 * <p>The wire/Xauthority representation is arbitrary bytes, while configuration and {@code xauth}
 * normally show those bytes as hexadecimal. The standard cookie length is sixteen bytes, so a
 * generated value is thirty two hexadecimal digits.
 */
public final class XAuthorityCookie {

    /** The standard X authorization protocol name. */
    public static final String PROTOCOL_NAME = "MIT-MAGIC-COOKIE-1";

    /** The standard cookie length used by Xauthority/Xau. */
    public static final int LENGTH = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private XAuthorityCookie() {
    }

    /** Creates a cryptographically random sixteen byte cookie. */
    public static byte[] generate() {
        byte[] cookie = new byte[LENGTH];
        RANDOM.nextBytes(cookie);
        return cookie;
    }

    /** Creates a random cookie and returns its thirty two hexadecimal digits. */
    public static String generateHex() {
        return toHex(generate());
    }

    /** Encodes cookie bytes as lower-case hexadecimal, matching {@code xauth list}. */
    public static String toHex(byte[] cookie) {
        Objects.requireNonNull(cookie, "cookie");
        return HexFormat.of().formatHex(cookie);
    }

    /**
     * Decodes the standard thirty two digit hexadecimal cookie value. An optional {@code 0x}
     * prefix is accepted.
     */
    public static byte[] parseHex(String value) {
        Objects.requireNonNull(value, "value");
        String hex = value.trim();
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        if (hex.length() != LENGTH * 2) {
            throw new IllegalArgumentException(
                    "a MIT-MAGIC-COOKIE-1 value must be exactly " + (LENGTH * 2)
                            + " hexadecimal digits");
        }
        try {
            return HexFormat.of().parseHex(hex);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "a MIT-MAGIC-COOKIE-1 value must be hexadecimal", e);
        }
    }
}
