package com.acgist.snail.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.acgist.snail.system.exception.ArgumentException;

/**
 * 散列（摘要）算法
 * 
 * @author acgist
 * @since 1.1.1
 */
public class DigestUtils {
	
	/**
	 * 散列算法：MD5
	 */
	public static final String MD5 = "MD5";
	/**
	 * 散列算法：SHA-1
	 */
	public static final String SHA1 = "SHA-1";
	
	/**
	 * MD5散列算法
	 */
	public static final MessageDigest md5() {
		return digest(MD5);
	}

	/**
	 * SHA-1散列算法
	 */
	public static final MessageDigest sha1() {
		return digest(SHA1);
	}
	
	/**
	 * 散列算法
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
