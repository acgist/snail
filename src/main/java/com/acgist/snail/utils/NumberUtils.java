package com.acgist.snail.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>数字工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class NumberUtils {

	private static final int MIN_INT_INDEX = 1000;
	private static final int MAX_INT_INDEX = 9999;
	
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
	 * 除法，如果相除有余数，结果+1。
	 */
	public static final int divideUp(long dividend, long divisor) {
		int value = (int) (dividend / divisor);
		if(dividend % divisor != 0) {
			value++;
		}
		return value;
	}

	/**
	 * 统计数字位上1的个数。
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
	 * 大整数转为无符号大整数二进制字符数组
	 * 
	 * @param 原始大整数
	 * @param byteCount 二进制字符数组长度
	 */
	public static byte[] encodeUnsigned(BigInteger value, int byteCount) {
        if (byteCount < 1) {
            throw new ArgumentException("数组长度错误");
        }
		byte[] bytes = value.toByteArray();
		if (bytes[0] == 0) {
			bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
		}
		if (bytes.length > byteCount) {
			throw new ArgumentException("数组长度错误");
		}
		if (bytes.length < byteCount) {
			final byte[] copy = bytes;
			bytes = new byte[byteCount];
			System.arraycopy(copy, 0, bytes, (bytes.length - copy.length), copy.length);
		}
		return bytes;
	}

	/**
	 * 无符号大整数二进制字符数组转为大整数
	 * 
	 * @param buffer 二进制字符数组
	 * @param length 二进制字符数组长度
	 */
	public static BigInteger decodeUnsigned(ByteBuffer buffer, int length) {
		if (length < 1 || buffer.remaining() < length) {
			throw new ArgumentException("数组长度错误");
		}
		byte b;
		int index = 0;
		while ((b = buffer.get()) == 0 && ++index < length) {
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
	
}
