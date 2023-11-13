package com.acgist.snail.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.protocol.Protocol;

/**
 * URL工具
 * 
 * @author acgist
 */
public final class UrlUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlUtils.class);
    
    private UrlUtils() {
    }
    
    /**
     * URL编码
     * 
     * @param content 原始内容
     * 
     * @return 编码内容
     */
    public static final String encode(String content) {
        if(StringUtils.isEmpty(content)) {
            return content;
        }
        try {
            return URLEncoder
                .encode(content, SystemConfig.DEFAULT_CHARSET)
                // 空格编码变成加号
                // 加号解码变成空格
                .replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("URL编码异常：{}", content, e);
        }
        return content;
    }
    
    /**
     * URL解码
     * 
     * @param content 编码内容
     * 
     * @return 原始内容
     */
    public static final String decode(String content) {
        if(StringUtils.isEmpty(content)) {
            return content;
        }
        try {
            return URLDecoder.decode(content, SystemConfig.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("URL解码异常：{}", content, e);
        }
        return content;
    }
    
    /**
     * 支持协议：HTTP、HTTPS
     * 
     * @param source 原始页面链接
     * @param target 目标页面链接
     * 
     * @return 完整目标页面链接
     */
    public static final String redirect(final String source, String target) {
        Objects.requireNonNull(source, "原始页面链接不能为空");
        Objects.requireNonNull(target, "目标页面链接不能为空");
        target = target.strip();
        // 去掉引号
        if(target.startsWith(SymbolConfig.Symbol.DOUBLE_QUOTE.toString())) {
            target = target.substring(1);
        }
        if(target.endsWith(SymbolConfig.Symbol.DOUBLE_QUOTE.toString())) {
            target = target.substring(0, target.length() - 1);
        }
        final String slash = SymbolConfig.Symbol.SLASH.toString();
        final char slashChar = SymbolConfig.Symbol.SLASH.toChar();
        if(Protocol.Type.HTTP.verify(target)) {
            // 完整链接
            return target;
        } else if(target.startsWith(slash)) {
            // 绝对目录链接
            final String prefix = Protocol.Type.HTTP.prefix(source);
            final int index = source.indexOf(slashChar, prefix.length());
            if(index > prefix.length()) {
                return source.substring(0, index) + target;
            } else {
                return source + target;
            }
        } else {
            // 相对目录链接
            final String prefix = Protocol.Type.HTTP.prefix(source);
            final int index = source.lastIndexOf(slashChar);
            if(index > prefix.length()) {
                return source.substring(0, index) + slash + target;
            } else {
                return source + slash + target;
            }
        }
    }
    
}
