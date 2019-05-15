package com.acgist.snail.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件校验工具
 */
public class FileVerifyUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileVerifyUtils.class);
	
	/**
	 * 散列计算
	 * @param path 文件地址，如果是目录计算里面每一个文件
	 * @param algo 算法：MD5/SHA1
	 */
	private static final Map<String, String> hash(String path, String algo) {
		final File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		final Map<String, String> data = new HashMap<>();
		if (!file.isFile()) {
			final File[] files = file.listFiles();
			for (File children : files) {
				data.putAll(hash(children.getPath(), algo));
			}
			return data;
		} else {
			MessageDigest digest = null;
			int length;
			byte bytes[] = new byte[10 * 1024];
			try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
				digest = MessageDigest.getInstance(algo);
				while ((length = input.read(bytes)) != -1) {
					digest.update(bytes, 0, length);
				}
			} catch (Exception e) {
				LOGGER.error("文件HASH计算异常", e);
				return data;
			}
			data.put(path, StringUtils.hex(digest.digest()));
			return data;
		}
	}

	/**
	 * MD5散列算法
	 */
	public static final Map<String, String> md5(String path) {
		return hash(path, "MD5");
	}

	/**
	 * SHA1散列算法
	 */
	public static final Map<String, String> sha1(String path) {
		return hash(path, "SHA1");
	}
	
}
