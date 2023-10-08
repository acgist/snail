package com.acgist.snail.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>散列算法工具</p>
 * <p>散列算法：计算数据摘要</p>
 * 
 * @author acgist
 */
public final class DigestUtils {
	
	private DigestUtils() {
	}
	
	/**
	 * <p>散列算法：{@value}</p>
	 */
	public static final String ALGO_MD5 = "MD5";
	/**
	 * <p>散列算法：{@value}</p>
	 */
	public static final String ALGO_SHA1 = "SHA-1";
	
	/**
	 * <p>获取MD5散列算法对象</p>
	 * 
	 * @return MD5散列算法对象
	 * 
	 * @see #digest(String)
	 */
	public static final MessageDigest md5() {
		return DigestUtils.digest(ALGO_MD5);
	}

	/**
	 * <p>获取SHA-1散列算法对象</p>
	 * 
	 * @return SHA-1散列算法对象
	 * 
	 * @see #digest(String)
	 */
	public static final MessageDigest sha1() {
		return DigestUtils.digest(ALGO_SHA1);
	}
	
	/**
	 * <p>获取散列算法对象</p>
	 * 
	 * @param algo 算法名称
	 * 
	 * @return 算法对象
	 */
	public static final MessageDigest digest(String algo) {
		try {
			return MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("不支持的散列算法：" + algo, e);
		}
	}
	
	/**
	 * <p>计算字节数组的MD5散列值</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return MD5散列值
	 */
	public static final byte[] md5(byte[] bytes) {
		return DigestUtils.md5().digest(bytes);
	}
	
	/**
	 * 计算字符串的MD5散列值
	 * 
	 * @param value 字符串
	 * 
	 * @return MD5散列值
	 */
	public static final String md5Hex(String value) {
	    return StringUtils.hex(DigestUtils.md5(value.getBytes()));
	}
	
	/**
	 * <p>计算字节数组的SHA-1散列值</p>
	 * 
	 * @param bytes 字节数组
	 * 
	 * @return SHA-1散列值
	 */
	public static final byte[] sha1(byte[] bytes) {
		return DigestUtils.sha1().digest(bytes);
	}
	
	/**
	 * 计算字符串的SHA-1散列值
	 * 
	 * @param value 字符串
	 * 
	 * @return SHA-1散列值
	 */
	public static final String sha1Hex(String value) {
	    return StringUtils.hex(DigestUtils.sha1(value.getBytes()));
	}
	
}
