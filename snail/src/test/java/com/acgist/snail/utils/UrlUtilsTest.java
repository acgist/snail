package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.jupiter.api.Test;

class UrlUtilsTest extends Performance {

    @Test
    void testRedirect() {
        String path   = "https://www.acgist.com/index.html";
        String pathA  = "https://www.acgist.com/a/index.html";
        String pathAB = "https://www.acgist.com/a/b/index.html";
        String baidu  = "https://www.baidu.com";
        assertEquals(UrlUtils.redirect("https://www.acgist.com",      "index.html"),        path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/",     "index.html"),        path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com",      "/index.html"),       path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/",     "/index.html"),       path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a",    "index.html"),        path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/",   "index.html"),        pathA);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a",    "/index.html"),       path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/",   "/index.html"),       path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b",  "index.html"),        pathA);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", "index.html"),        pathAB);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b",  "/index.html"),       path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", "/index.html"),       path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", " /index.html "),     path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", "\"/index.html\""),   path);
        assertEquals(UrlUtils.redirect("https://www.acgist.com/a/b/", " \"/index.html\" "), path);
        assertEquals(UrlUtils.redirect(path, baidu), baidu);
    }

    @Test
    void testEncodeDecode() throws UnsupportedEncodingException {
        assertEquals("%2B+%2B", URLEncoder.encode("+ +", "UTF-8"));
        assertEquals("+ +", URLDecoder.decode("%2B+%2B", "UTF-8"));
        assertEquals("%2B%20%2B", UrlUtils.encode("+ +"));
        assertEquals("+ +", UrlUtils.decode("%2B+%2B"));
    }
    
}
