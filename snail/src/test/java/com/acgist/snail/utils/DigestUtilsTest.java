package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class DigestUtilsTest extends Performance {

    @Test
    void testDigestUtils() {
        assertNotNull(DigestUtils.md5());
        assertNotNull(DigestUtils.sha1());
        assertEquals("e10adc3949ba59abbe56e057f20f883e", DigestUtils.md5Hex("123456"));
        assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", DigestUtils.sha1Hex("123456"));
        assertEquals("e10adc3949ba59abbe56e057f20f883e", StringUtils.hex(DigestUtils.md5().digest("123456".getBytes())));
        assertEquals("7c4a8d09ca3762af61e59520943dc26494f8941b", StringUtils.hex(DigestUtils.sha1().digest("123456".getBytes())));
    }
    
}
