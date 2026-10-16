package cn.oyzh.fx.xserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XAuthorityCookieTest {

    @Test
    void generatesAStandardCookie() {
        byte[] cookie = XAuthorityCookie.generate();
        assertEquals(XAuthorityCookie.LENGTH, cookie.length);

        String hex = XAuthorityCookie.generateHex();
        assertEquals(XAuthorityCookie.LENGTH * 2, hex.length());
        assertTrue(hex.matches("[0-9a-f]+"), hex);
    }

    @Test
    void convertsToAndFromTheXauthorityHexForm() {
        byte[] cookie = XAuthorityCookie.generate();
        String hex = XAuthorityCookie.toHex(cookie);
        assertArrayEquals(cookie, XAuthorityCookie.parseHex(hex));
        assertArrayEquals(cookie, XAuthorityCookie.parseHex("0X" + hex.toUpperCase()));
    }

    @Test
    void refusesMalformedCookieValues() {
        assertThrows(IllegalArgumentException.class, () -> XAuthorityCookie.parseHex(""));
        assertThrows(IllegalArgumentException.class, () -> XAuthorityCookie.parseHex("abc"));
        assertThrows(IllegalArgumentException.class, () -> XAuthorityCookie.parseHex("aabb"));
        assertThrows(IllegalArgumentException.class, () -> XAuthorityCookie.parseHex("not-hex"));
    }
}
