package com.acgist.snail.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * <p>散列（摘要）算法</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public final class DigestUtils {
	
	/**
	 * <p>散列算法：MD5</p>
	 */
	public static final String ALGO_MD5 = "MD5";
	/**
	 * <p>散列算法：SHA-1</p>
	 */
	public static final String ALGO_SHA1 = "SHA-1";
	
	/**
	 * <p>MD5散列算法</p>
	 */
	public static final MessageDigest md5() {
		return digest(ALGO_MD5);
	}

	/**
	 * <p>SHA-1散列算法</p>
	 */
	public static final MessageDigest sha1() {
		return digest(ALGO_SHA1);
	}
	
	/**
	 * <p>散列算法</p>
	 * 
	 * @param algo 算法名称
	 * 
	 * @return 算法对象
	 */
	public static final MessageDigest digest(String algo) {
		try {
			return MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			throw new ArgumentException("不支持的散列算法：" + algo, e);
		}
	}
	
}
