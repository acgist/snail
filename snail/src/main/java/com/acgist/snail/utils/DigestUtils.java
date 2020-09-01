package com.acgist.snail.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>散列算法工具</p>
 * <p>散列算法：计算数据摘要</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class DigestUtils {
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
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
		return digest(ALGO_MD5);
	}

	/**
	 * <p>获取SHA-1散列算法对象</p>
	 * 
	 * @return SHA-1散列算法对象
	 * 
	 * @see #digest(String)
	 */
	public static final MessageDigest sha1() {
		return digest(ALGO_SHA1);
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
	
}
