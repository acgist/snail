package com.acgist.snail.module.coder.torrent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1Hash { // 其实就是sha1算法

	public static String toHex(byte[] node) { // 返回一个20byte的sha1字符串

		try {

			MessageDigest d = MessageDigest.getInstance("SHA1"); // 指定算法 sha1摘要

			d.update(node); // 进行摘要

			return to20Bytre(d.digest()); // 摘要的结果是 20byte 转16进制 得到字符串 而调用下面方法。

		} catch (NoSuchAlgorithmException ex) {

		}

		return "";

	}

	private static String to20Bytre(byte[] b) { // byte数组转16进制

		String tmp = "";

		String des = "";

		for (int i = 0; i < b.length; i++) {

			tmp = Integer.toHexString(b[i] & 0xFF);

			if (tmp.length() == 1) {

				des += "0";

			}

			des += tmp;

		}

		return des.toUpperCase(); // 最终结果 就是那磁链........前面加 magnet:?xt=urn:btih: 即可，toUpperCase()方法只是将所有字母大写。

	}

}