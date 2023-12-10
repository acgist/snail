package com.acgist.snail.utils;

import java.nio.ByteBuffer;

/**
 * 字符工具
 * 
 * @author acgist
 */
public final class ByteUtils {

    private ByteUtils() {
    }
    
    /**
     * 读取剩余字节数据
     * 
     * @param buffer 缓冲数据
     * 
     * @return 剩余字节数据
     */
    public static final byte[] remainingToBytes(ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
    
    /**
     * 读取剩余字节数据转为字符串
     * 
     * @param buffer 缓冲数据
     * 
     * @return 剩余字节数据字符串
     */
    public static final String remainingToString(ByteBuffer buffer) {
        return new String(ByteUtils.remainingToBytes(buffer));
    }
    
}
