package com.acgist.snail.utils;

/**
 * Base32编码工具
 * 
 * @author acgist
 */
public final class Base32Utils {

    /**
     * 编码字符
     */
    private static final char[] BASE_32_ENCODE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '2', '3', '4', '5', '6', '7'
    };
    /**
     * 解码字符
     */
    private static final byte[] BASE_32_DECODE;
    /**
     * 字符位数
     */
    private static final int SIZE_BYTE = Byte.SIZE;
    /**
     * Base32编码字符位数：五位三十二个字符
     */
    private static final int SIZE_BASE_32 = 5;
    /**
     * Base32编码字符剩余位数
     */
    private static final int SIZE_LEFT = SIZE_BYTE - SIZE_BASE_32;

    static {
        BASE_32_DECODE = new byte[128];
        for (int index = 0; index < BASE_32_DECODE.length; index++) {
            BASE_32_DECODE[index] = (byte) 0xFF;
        }
        for (int index = 0; index < BASE_32_ENCODE.length; index++) {
            BASE_32_DECODE[(int) BASE_32_ENCODE[index]] = (byte) index;
            // 设置小写
            BASE_32_DECODE[(int) Character.toLowerCase(BASE_32_ENCODE[index])] = (byte) index;
        }
    }
    
    private Base32Utils() {
    }
    
    /**
     * 数据编码
     * 
     * @param content 原始数据
     * 
     * @return 编码数据
     * 
     * @see #encode(byte[])
     */
    public static final String encode(String content) {
        if(content == null) {
            return null;
        }
        return Base32Utils.encode(content.getBytes());
    }
    
    /**
     * 数据编码
     * 
     * @param content 原始数据
     * 
     * @return 编码数据
     */
    public static final String encode(final byte[] content) {
        if(content == null) {
            return null;
        }
        int value;
        int index = 0;
        int contentIndex = 0;
        final int contentLength = content.length;
        final char[] chars = new char[
            ((contentLength * SIZE_BYTE) / SIZE_BASE_32) +
            // 因为除数是五所以可以直接判断
            ((contentLength % SIZE_BASE_32) != 0 ? 1 : 0)
        ];
        final int charsLength = chars.length;
        for (int charsIndex = 0; charsIndex < charsLength; charsIndex++) {
            if (index > SIZE_LEFT) {
                value = (content[contentIndex] & 0xFF) & (0xFF >> index);
                index = (index + SIZE_BASE_32) % SIZE_BYTE;
                value <<= index;
                if (contentIndex < contentLength - 1) {
                    value |= (content[contentIndex + 1] & 0xFF) >> (SIZE_BYTE - index);
                }
                chars[charsIndex] = BASE_32_ENCODE[value];
                contentIndex++;
            } else {
                chars[charsIndex] = BASE_32_ENCODE[((content[contentIndex] >> (SIZE_BYTE - (index + SIZE_BASE_32))) & 0x1F)];
                index = (index + SIZE_BASE_32) % SIZE_BYTE;
                if (index == 0) {
                    contentIndex++;
                }
            }
        }
        return new String(chars);
    }

    /**
     * 数据解码
     * 
     * @param content 编码数据
     * 
     * @return 原始数据
     * 
     * @see #decode(String)
     */
    public static final String decodeToString(final String content) {
        if(content == null) {
            return null;
        }
        return new String(Base32Utils.decode(content));
    }
    
    /**
     * 数据解码
     * 
     * @param content 编码数据
     * 
     * @return 原始数据
     */
    public static final byte[] decode(final String content) {
        if(content == null) {
            return null;
        }
        int value;
        int index = 0;
        int bytesIndex = 0;
        final char[] chars = content.toCharArray();
        // 不用转为大写：支持小写解码
//        final char[] chars = content.toUpperCase().toCharArray();
        final int charsLength = chars.length;
        final byte[] bytes = new byte[(charsLength * SIZE_BASE_32) / SIZE_BYTE];
        final int bytesLength = bytes.length;
        for (int charsIndex = 0; charsIndex < charsLength; charsIndex++) {
            value = BASE_32_DECODE[chars[charsIndex]];
            if (index <= SIZE_LEFT) {
                index = (index + SIZE_BASE_32) % SIZE_BYTE;
                if (index == 0) {
                    bytes[bytesIndex++] |= value;
                } else {
                    bytes[bytesIndex] |= value << (SIZE_BYTE - index);
                }
            } else {
                index = (index + SIZE_BASE_32) % SIZE_BYTE;
                bytes[bytesIndex++] |= (value >> index);
                if (bytesIndex < bytesLength) {
                    bytes[bytesIndex] |= value << (SIZE_BYTE - index);
                }
            }
        }
        return bytes;
    }

}
