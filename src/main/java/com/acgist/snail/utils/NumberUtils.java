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

import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>数字工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class NumberUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumberUtils.class);
	
	/**
	 * <p>最小唯一编号索引</p>
	 */
	private static final int MIN_INT_INDEX = 1000;
	/**
	 * <p>最大唯一编号索引</p>
	 */
	private static final int MAX_INT_INDEX = 9999;
	
	/**
	 * <p>唯一编号索引</p>
	 */
	private static int index = MIN_INT_INDEX;
	
	/**
	 * <p>生成唯一编号</p>
	 * <p>长度：8</p>
	 * <p>格式：index(4) + mmss</p>
	 */
	public static final Integer build() {
		final StringBuilder builder = new StringBuilder();
		synchronized(NumberUtils.class) {
			int index = NumberUtils.index;
			builder.append(index);
			if(++index > MAX_INT_INDEX) {
				index = MIN_INT_INDEX;
			}
			NumberUtils.index = index;
		}
		builder.append(DateUtils.dateToString(new Date(), "mmss"));
		return Integer.valueOf(builder.toString());
	}
	
	/**
	 * <p>字节数组转int（大端）</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return int
	 */
	public static final int bytesToInt(byte[] bytes) {
		int value = 0;
		value += ((bytes[0] & 0xFF) << 24);
		value += ((bytes[1] & 0xFF) << 16);
		value += ((bytes[2] & 0xFF) << 8);
		value += bytes[3] & 0xFF;
		return value;
	}
	
	/**
	 * <p>int转字节数组（大端）</p>
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
	 * <p>字节数组转short（大端）</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return short
	 */
	public static final short bytesToShort(byte[] bytes) {
		short value = 0;
		value += ((bytes[0] & 0xFF) << 8);
		value += bytes[1] & 0xFF;
		return value;
	}
	
	/**
	 * <p>short转字节数组（大端）</p>
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
	 * <p>向上取整</p>
	 * 
	 * @param dividend 被除数
	 * @param divisor 除数
	 * 
	 * @param 商
	 */
	public static final int ceilDiv(long dividend, long divisor) {
		int value = (int) (dividend / divisor);
		if(dividend % divisor != 0) {
			value++;
		}
		return value;
	}

	/**
	 * <p>统计数字位上是1的个数</p>
	 * 
	 * @param number 数字
	 * 
	 * @return 位上是1的个数
	 */
	public static final byte bitCount(int number) {
		byte count = 0;
		while (number != 0) {
			number = number & (number - 1);
			count++;
		}
		return count;
	}
	
	/**
	 * <p>大整数转为无符号二进制字节数组</p>
	 * 
	 * @param value 大整数
	 * @param length 二进制字节数组长度
	 * 
	 * @return 字节数组
	 */
	public static final byte[] encodeUnsigned(BigInteger value, int length) {
		if (length < 1) {
			throw new ArgumentException("数组长度错误");
		}
		byte[] bytes = value.toByteArray();
		if (bytes[0] == 0) {
			bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		}
		if (bytes.length > length) {
			throw new ArgumentException("数组长度错误");
		}
		if (bytes.length < length) {
			final byte[] copy = bytes;
			bytes = new byte[length];
			System.arraycopy(copy, 0, bytes, (bytes.length - copy.length), copy.length);
		}
		return bytes;
	}

	/**
	 * <p>无符号二进制字节数组转为大整数</p>
	 * 
	 * @param buffer 字节数组
	 * @param length 二进制字节数组长度
	 * 
	 * @return 大整数
	 */
	public static final BigInteger decodeUnsigned(ByteBuffer buffer, int length) {
		if (length < 1 || buffer.remaining() < length) {
			throw new ArgumentException("数组长度错误");
		}
		byte b;
		int index = 0;
		while ((b = buffer.get()) == 0 && ++index < length) { // 去掉前导零
		}
		if (index == length) {
			return BigInteger.ZERO;
		}
		final int newLength = length - index;
		final byte[] bytes = new byte[newLength];
		bytes[0] = b;
		buffer.get(bytes, 1, newLength - 1);
		return new BigInteger(1, bytes);
	}
	
	/**
	 * <p>获取随机数工具</p>
	 * 
	 * @return 随机数工具
	 */
	public static final Random random() {
		try {
			return SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("获取随机数工具异常", e);
		}
		return new Random();
	}
	
}
