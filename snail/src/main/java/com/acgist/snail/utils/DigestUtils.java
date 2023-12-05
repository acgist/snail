package com.acgist.snail.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 散列算法工具
 * 散列算法：计算数据摘要
 * 
 * @author acgist
 */
public final class DigestUtils {
    
    private DigestUtils() {
    }
    
    /**
     * 散列算法：{@value}
     */
    public static final String ALGO_MD5 = "MD5";
    /**
     * 散列算法：{@value}
     */
    public static final String ALGO_SHA1 = "SHA-1";
    
    /**
     * @return MD5散列算法对象
     * 
     * @see #digest(String)
     */
    public static final MessageDigest md5() {
        return DigestUtils.digest(ALGO_MD5);
    }

    /**
     * @return SHA-1散列算法对象
     * 
     * @see #digest(String)
     */
    public static final MessageDigest sha1() {
        return DigestUtils.digest(ALGO_SHA1);
    }
    
    /**
     * @param algo 算法名称
     * 
     * @return 散列算法对象
     */
    public static final MessageDigest digest(String algo) {
        try {
            return MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("不支持的散列算法：" + algo, e);
        }
    }
    
    /**
     * 计算字节数组的MD5散列值
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
     * 计算字节数组的SHA-1散列值
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
