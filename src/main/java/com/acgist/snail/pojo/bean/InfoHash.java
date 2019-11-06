package com.acgist.snail.pojo.bean;

import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子InfoHash
 * 
 * @author acgist
 * @since 1.0.0
 */
public class InfoHash {

	/**
	 * 种子info数据长度
	 */
	private int size;
	/**
	 * 种子info数据
	 */
	private byte[] info;
	/**
	 * 种子info数据hash
	 */
	private final byte[] infoHash;
	
	private InfoHash(byte[] infoHash) {
		this.infoHash = infoHash;
	}

	/**
	 * 生成InfoHash
	 * 
	 * @param data 种子Info
	 */
	public static final InfoHash newInstance(byte[] data) {
		final InfoHash infoHash = new InfoHash(StringUtils.sha1(data));
		infoHash.info = data;
		infoHash.size = data.length;
		return infoHash;
	}
	
	/**
	 * 生成InfoHash
	 * 
	 * @param hash 种子Info的Hash
	 */
	public static final InfoHash newInstance(String hash) throws DownloadException {
		if(hash == null) {
			throw new DownloadException("不支持的hash：" + hash);
		}
		hash = hash.trim();
		if(Protocol.Type.verifyMagnetHash40(hash)) {
			return new InfoHash(StringUtils.unhex(hash));
		} else if(Protocol.Type.verifyMagnetHash32(hash)) {
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

	public byte[] infoHash() {
		return infoHash;
	}
	
	/**
	 * 16进制info的hash（小写）（40位）
	 */
	public String infoHashHex() {
		return StringUtils.hex(this.infoHash);
	}
	
	/**
	 * 网络info的hash
	 */
	public String infoHashURL() {
		int index = 0;
		final String infoHashHex = infoHashHex();
		final int length = infoHashHex.length();
		final StringBuilder builder = new StringBuilder();
		do {
			builder.append("%").append(infoHashHex.substring(index, index + 2));
			index += 2;
		} while (index < length);
		return builder.toString();
	}
	
}
