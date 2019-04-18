package com.acgist.snail.protocol.torrent.bean;

import java.util.Objects;

import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子InfoHash
 */
public class InfoHash {

	private int size; // 种子文件info数据的长度
	private final byte[] data;
	
	private InfoHash(byte[] data) {
		this.data = data;
	}
	
	private InfoHash(byte[] data, int size) {
		this.data = data;
		this.size = size;
	}

	public static final InfoHash newInstance(byte[] data) {
		return new InfoHash(StringUtils.sha1(data), data.length);
	}
	
	public static final InfoHash newInstance(String hash) throws DownloadException {
		hash = Objects.requireNonNull(hash, "不支持的hash");
		if(hash.length() == 40) {
			return new InfoHash(StringUtils.unhex(hash));
		} else if(hash.length() == 32) {
			return new InfoHash(Base32Utils.decode(hash));
		} else {
			throw new DownloadException("不支持的hash：" + hash);
		}
	}
	
	public int size() {
		return this.size;
	}
	
	public void size(int size) {
		this.size = size;
	}
	
	/**
	 * hash byte（20位）
	 */
	public byte[] infoHash() {
		return data;
	}
	
	/**
	 * 磁力链接hash（小写）（40位）
	 */
	public String infoHashHex() {
		return StringUtils.hex(data);
	}
	
	/**
	 * 种子ID（网络传输使用）
	 */
	public String infoHashURL() {
		final String magnetHash = infoHashHex();
		int index = 0;
		final int length = magnetHash.length();
		final StringBuilder builder = new StringBuilder();
		do {
			builder.append("%").append(magnetHash.substring(index, index + 2));
			index += 2;
		} while (index < length);
		return builder.toString();
	}
	
}
