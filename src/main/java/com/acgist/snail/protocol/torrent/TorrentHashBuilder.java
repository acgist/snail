package com.acgist.snail.protocol.torrent;

import com.acgist.snail.utils.StringUtils;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/**
 * 种子磁力链接HASH生成器：<br>
 * 40位hash：sha1生成，磁力链接使用
 * 20位hash：sha1生成编码为20位，tracker使用
 */
public class TorrentHashBuilder {

	private boolean begin;
	private ByteArrayBuilder builder;
	
	private String hash;
	private String infoHash;
	
	private TorrentHashBuilder() {
		begin = false;
		builder = new ByteArrayBuilder();
	}
	
	public static final TorrentHashBuilder newInstance() {
		return new TorrentHashBuilder();
	}
	
	public void build(String key, byte[] values) {
		for (byte value : values) {
			build(key, value);
		}
	}
	
	public void build(String key, int value) {
		build(key, (byte) value);
	}
	
	public void build(String key, byte value) {
		if(key == null) {
			return;
		}
		if("info".equals(key)) {
			begin = true;
		}
		if("nodes".equals(key)) {
			begin = false;
			return;
		}
		if(begin) {
			builder.append(value);
		}
	}

	/**
	 * 创建hash
	 */
	public void buildHash() {
		byte[] byteArray = builder.toByteArray();
		byte[] bytes = new byte[byteArray.length - 7];
		System.arraycopy(byteArray, 0, bytes, 0, byteArray.length - 7);
		this.hash = StringUtils.sha1(bytes);
		int index = 0;
		final int length = this.hash.length();
		StringBuilder builder = new StringBuilder();
		do {
			builder.append("%").append(this.hash.substring(index, index + 2));
			index+=2;
		} while (index < length);
		this.infoHash = builder.toString();
	}
	
	public String hash() {
		return this.hash;
	}
	
	public String infoHash() {
		return this.infoHash;
	}

}
