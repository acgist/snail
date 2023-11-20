package com.acgist.snail.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

/**
 * 数值工具
 * 
 * 向上转型需要`& 0xFF`：类型转换并且去掉符号
 * (byte) number = (byte) (number & 0xFF)
 * 
 * @author acgist
 */
public final class NumberUtils {

    private NumberUtils() {
    }
    
    /**
     * 唯一编号最小索引：{@value}
     */
    private static final int MIN_INDEX = 1000;
    /**
     * 唯一编号最大索引：{@value}
     */
    private static final int MAX_INDEX = 9999;
    
    /**
     * 唯一编号索引
     * 第一个随机初始化：防止一分钟内多次启动重复生成
     */
    private static int index = (int) (System.currentTimeMillis() % (MAX_INDEX - MIN_INDEX) + MIN_INDEX);
    
    /**
     * 生成唯一编号
     * 长度：8
     * 格式：{@link #index} + HHmm
     * 
     * @return 编号
     */
    public static final Integer build() {
        final StringBuilder builder = new StringBuilder();
        synchronized(NumberUtils.class) {
            int index = NumberUtils.index;
            builder.append(index);
            if(++index > MAX_INDEX) {
                index = MIN_INDEX;
            }
            NumberUtils.index = index;
        }
        builder.append(DateUtils.dateFormat(new Date(), "HHmm"));
        return Integer.valueOf(builder.toString());
    }
    
    /**
     * 字节数组转为long（大端）
     * 
     * @param bytes 字节数组
     * 
     * @return long
     */
    public static final long bytesToLong(byte[] bytes) {
        long value = 0L;
        value |= (bytes[0] & 0xFFL) << 56;
        value |= (bytes[1] & 0xFFL) << 48;
        value |= (bytes[2] & 0xFFL) << 40;
        value |= (bytes[3] & 0xFFL) << 32;
        value |= (bytes[4] & 0xFFL) << 24;
        value |= (bytes[5] & 0xFFL) << 16;
        value |= (bytes[6] & 0xFFL) << 8;
        value |= (bytes[7] & 0xFFL);
        return value;
    }
    
    /**
     * long转为字节数组（大端）
     * 
     * @param value long
     * 
     * @return 字节数组
     */
    public static final byte[] longToBytes(long value) {
        final byte[] bytes = new byte[8];
        bytes[0] = (byte) (value >>> 56);
        bytes[1] = (byte) (value >>> 48);
        bytes[2] = (byte) (value >>> 40);
        bytes[3] = (byte) (value >>> 32);
        bytes[4] = (byte) (value >>> 24);
        bytes[5] = (byte) (value >>> 16);
        bytes[6] = (byte) (value >>> 8);
        bytes[7] = (byte) (value);
        return bytes;
    }
    
    /**
     * 字节数组转为int（大端）
     * 
     * @param bytes 字节数组
     * 
     * @return int
     */
    public static final int bytesToInt(byte[] bytes) {
        int value = 0;
        value |= (bytes[0] & 0xFF) << 24;
        value |= (bytes[1] & 0xFF) << 16;
        value |= (bytes[2] & 0xFF) << 8;
        value |= (bytes[3] & 0xFF);
        return value;
    }
    
    /**
     * int转为字节数组（大端）
     * 
     * @param value int
     * 
     * @return 字节数组
     */
    public static final byte[] intToBytes(int value) {
        final byte[] bytes = new byte[4];
        bytes[0] = (byte) (value >>> 24);
        bytes[1] = (byte) (value >>> 16);
        bytes[2] = (byte) (value >>> 8);
        bytes[3] = (byte) (value);
        return bytes;
    }
    
    /**
     * 字节数组转为short（大端）
     * 
     * @param bytes 字节数组
     * 
     * @return short
     */
    public static final short bytesToShort(byte[] bytes) {
        short value = 0;
        value |= (bytes[0] & 0xFF) << 8;
        value |= (bytes[1] & 0xFF);
        return value;
    }
    
    /**
     * short转为字节数组（大端）
     * 
     * @param value short
     * 
     * @return 字节数组
     */
    public static final byte[] shortToBytes(short value) {
        final byte[] bytes = new byte[2];
        bytes[0] = (byte) (value >>> 8);
        bytes[1] = (byte) (value);
        return bytes;
    }
    
    /**
     * 除法向上取整
     * 
     * ceilDiv(2, 2) = 1;
     * ceilDiv(3, 2) = 2;
     * ceilDiv(4, 2) = 2;
     * 
     * @param dividend 被除数
     * @param divisor  除数
     * 
     * @return 结果
     */
    public static final int ceilDiv(int dividend, int divisor) {
        int value = dividend / divisor;
        if(dividend % divisor != 0) {
            value++;
        }
        return value;
    }

    /**
     * 乘法向上取整
     * 
     * ceilMult(2, 2) = 2;
     * ceilMult(3, 2) = 4;
     * ceilMult(4, 2) = 4;
     * 
     * @param dividend 被除数
     * @param divisor  除数
     * 
     * @return 结果
     */
    public static final int ceilMult(int dividend, int divisor) {
        return NumberUtils.ceilDiv(dividend, divisor) * divisor;
    }
    
    /**
     * 大整数转为二进制字节数组
     * 
     * @param value  大整数（正数）
     * @param length 数组长度
     * 
     * @return 字节数组
     */
    public static final byte[] encodeBigInteger(final BigInteger value, final int length) {
        if (length < 1) {
            throw new IllegalArgumentException("数组长度错误：" + length);
        }
        // 二进制补码
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0) {
            // 符号位是零：去掉符号位
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        // 填充数据
        if (bytes.length < length) {
            final byte[] copy = bytes;
            bytes = new byte[length];
            System.arraycopy(copy, 0, bytes, (bytes.length - copy.length), copy.length);
        }
        return bytes;
    }

    /**
     * 二进制字节数组转为大整数
     * 
     * @param buffer 字节数组
     * @param length 数组长度
     * 
     * @return 大整数（正数）
     */
    public static final BigInteger decodeBigInteger(final ByteBuffer buffer, final int length) {
        if (length < 1 || buffer.remaining() < length) {
            throw new IllegalArgumentException("数组长度错误：" + length);
        }
        int index = 0;
        byte nonzero;
        while ((nonzero = buffer.get()) == 0 && ++index < length) {
            // 去掉前导零
        }
        if (index == length) {
            // 所有位全是零
            return BigInteger.ZERO;
        }
        final int newLength = length - index;
        final byte[] bytes = new byte[newLength];
        // 读取非零数据
        bytes[0] = nonzero;
        buffer.get(bytes, 1, newLength - 1);
        // 正整数
        return new BigInteger(1, bytes);
    }
    
    /**
     * 判断数值是否相等
     * 
     * @param source 原始数值
     * @param target 目标数值
     * 
     * @return 是否相等
     */
    public static final boolean equals(Number source, Number target) {
        return source == null ? target == null : source.equals(target);
    }

    /**
     * 注意（存在性能问题）：SecureRandom.getInstanceStrong()
     * 
     * @return 随机数对象
     */
    public static final SecureRandom random() {
        return new SecureRandom();
    }
    
}
