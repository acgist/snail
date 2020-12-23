package com.acgist.snail.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>数字工具</p>
 * 
 * @author acgist
 */
public final class NumberUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumberUtils.class);
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private NumberUtils() {
	}
	
	/**
	 * <p>最小唯一编号索引：{@value}</p>
	 */
	private static final int MIN_INDEX = 1000;
	/**
	 * <p>最大唯一编号索引：{@value}</p>
	 */
	private static final int MAX_INDEX = 9999;
	
	/**
	 * <p>唯一编号索引</p>
	 * <p>随机生成：防止一分钟内多次启动生成重复索引</p>
	 */
	private static int index = (int) (System.currentTimeMillis() % (MAX_INDEX - MIN_INDEX) + MIN_INDEX);
	
	/**
	 * <p>生成唯一编号</p>
	 * <p>长度：8</p>
	 * <p>格式：{@link #index} + HHmm</p>
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
	 * <p>字节数组转为long（大端）</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return long
	 */
	public static final long bytesToLong(byte[] bytes) {
		long value = 0L;
		value |= (bytes[0] & 0xFF);
		value <<= 8;
		value |= (bytes[1] & 0xFF);
		value <<= 8;
		value |= (bytes[2] & 0xFF);
		value <<= 8;
		value |= (bytes[3] & 0xFF);
		value <<= 8;
		value |= (bytes[4] & 0xFF);
		value <<= 8;
		value |= (bytes[5] & 0xFF);
		value <<= 8;
		value |= (bytes[6] & 0xFF);
		value <<= 8;
		value |= (bytes[7] & 0xFF);
		return value;
	}
	
	/**
	 * <p>long转为字节数组（大端）</p>
	 * 
	 * @param value long
	 * 
	 * @return 字节数组
	 */
	public static final byte[] longToBytes(long value) {
		final byte[] bytes = new byte[8];
		bytes[0] = (byte) ((value >> 56) & 0xFF);
		bytes[1] = (byte) ((value >> 48) & 0xFF);
		bytes[2] = (byte) ((value >> 40) & 0xFF);
		bytes[3] = (byte) ((value >> 32) & 0xFF);
		bytes[4] = (byte) ((value >> 24) & 0xFF);
		bytes[5] = (byte) ((value >> 16) & 0xFF);
		bytes[6] = (byte) ((value >> 8) & 0xFF);
		bytes[7] = (byte) (value & 0xFF);
		return bytes;
	}
	
	/**
	 * <p>字节数组转为int（大端）</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return int
	 */
	public static final int bytesToInt(byte[] bytes) {
		int value = 0;
		value |= (bytes[0] & 0xFF);
		value <<= 8;
		value |= (bytes[1] & 0xFF);
		value <<= 8;
		value |= (bytes[2] & 0xFF);
		value <<= 8;
		value |= (bytes[3] & 0xFF);
		return value;
	}
	
	/**
	 * <p>int转为字节数组（大端）</p>
	 * 
	 * @param value int
	 * 
	 * @return 字节数组
	 */
	public static final byte[] intToBytes(int value) {
		final byte[] bytes = new byte[4];
		bytes[0] = (byte) ((value >> 24) & 0xFF);
		bytes[1] = (byte) ((value >> 16) & 0xFF);
		bytes[2] = (byte) ((value >> 8) & 0xFF);
		bytes[3] = (byte) (value & 0xFF);
		return bytes;
	}
	
	/**
	 * <p>字节数组转为short（大端）</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return short
	 */
	public static final short bytesToShort(byte[] bytes) {
		short value = 0;
		value |= (bytes[0] & 0xFF);
		value <<= 8;
		value |= (bytes[1] & 0xFF);
		return value;
	}
	
	/**
	 * <p>short转为字节数组（大端）</p>
	 * 
	 * @param value short	
	 * 
	 * @return 字节数组
	 */
	public static final byte[] shortToBytes(short value) {
		final byte[] bytes = new byte[2];
		bytes[0] = (byte) ((value >> 8) & 0xFF);
		bytes[1] = (byte) (value & 0xFF);
		return bytes;
	}
	
	/**
	 * <p>向上取整（除法）</p>
	 * <pre>
	 * ceilDiv(2, 2) = 1;
	 * ceilDiv(3, 2) = 2;
	 * ceilDiv(4, 2) = 2;
	 * </pre>
	 * 
	 * @param dividend 被除数
	 * @param divisor 除数
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
	 * <p>向上取整（乘法）</p>
	 * <pre>
	 * ceilMult(2, 2) = 2;
	 * ceilMult(3, 2) = 4;
	 * ceilMult(4, 2) = 4;
	 * </pre>
	 * 
	 * @param dividend 被除数
	 * @param divisor 除数
	 * 
	 * @return 结果
	 */
	public static final int ceilMult(int dividend, int divisor) {
		return ceilDiv(dividend, divisor) * divisor;
	}
	
	/**
	 * <p>大整数转为二进制字节数组</p>
	 * 
	 * @param value 大整数
	 * @param length 数组长度
	 * 
	 * @return 字节数组
	 */
	public static final byte[] encodeBigInteger(final BigInteger value, final int length) {
		if (length < 1) {
			throw new IllegalArgumentException("数组长度错误：" + length);
		}
		byte[] bytes = value.toByteArray(); // 二进制补码
		// 符号位是零
		if (bytes[0] == 0) {
			bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		}
		if (bytes.length > length) {
			throw new IllegalArgumentException("数组长度错误：" + length);
		}
		if (bytes.length < length) {
			final byte[] copy = bytes;
			bytes = new byte[length];
			System.arraycopy(copy, 0, bytes, (bytes.length - copy.length), copy.length);
		}
		return bytes;
	}

	/**
	 * <p>二进制字节数组转为大整数</p>
	 * 
	 * @param buffer 字节数组
	 * @param length 数组长度
	 * 
	 * @return 大整数
	 */
	public static final BigInteger decodeBigInteger(final ByteBuffer buffer, final int length) {
		if (length < 1 || buffer.remaining() < length) {
			throw new IllegalArgumentException("数组长度错误：" + length);
		}
		int index = 0;
		byte nonzero;
		// 去掉前导零
		while ((nonzero = buffer.get()) == 0 && ++index < length);
		// 所有均是零
		if (index == length) {
			return BigInteger.ZERO;
		}
		final int newLength = length - index;
		final byte[] bytes = new byte[newLength];
		// 读取非零数据
		bytes[0] = nonzero;
		buffer.get(bytes, 1, newLength - 1);
		return new BigInteger(1, bytes); // 正整数
	}
	
	/**
	 * <p>获取随机数对象</p>
	 * 
	 * @return 随机数对象
	 */
	public static final Random random() {
		try {
			return SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("获取随机数对象异常", e);
		}
		return new Random();
	}
	
	/**
	 * <p>判断数值是否相等</p>
	 * 
	 * @param source 原始数值
	 * @param target 目标数值
	 * 
	 * @return 是否相对
	 */
	public static final boolean equals(Number source, Number target) {
		return source == null ? target == null : source.equals(target);
	}
	
}
