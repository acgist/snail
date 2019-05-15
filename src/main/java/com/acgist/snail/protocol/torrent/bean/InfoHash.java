package com.acgist.snail.protocol.torrent.bean;

import java.util.Objects;

import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子InfoHash
 */
public class InfoHash {

	/**
	 * InfoHash长度
	 */
	public static final int INFO_HASH_LENGTH = 20;
	
	private int size; // 种子文件info数据的长度
	private byte[] info; // 种子文件info数据
	private final byte[] infoHash;
	
	private InfoHash(byte[] infoHash) {
		this.infoHash = infoHash;
	}

	public static final InfoHash newInstance(byte[] data) {
		final InfoHash infoHash = new InfoHash(StringUtils.sha1(data));
		infoHash.info = data;
		infoHash.size = data.length;
		return infoHash;
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
	
	public byte[] info() {
		return this.info;
	}
	
	public void info(byte[] info) {
		this.info = info;
	}

	/**
	 * hash byte（20位）
	 */
	public byte[] infoHash() {
		return infoHash;
	}
	
	/**
	 * 磁力链接hash（小写）（40位）
	 */
	public String infoHashHex() {
		return StringUtils.hex(infoHash);
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
