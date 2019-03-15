package com.acgist.snail.protocol.torrent;

import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/**
 * 种子磁力链接HASH生成器：<br>
 * 40位hash：sha1生成，磁力链接使用
 * 20位hash：sha1生成编码为20位，tracker使用
 */
public class InfoHashBuilder {

	private boolean begin;
	private ByteArrayBuilder builder;
	
	private InfoHashBuilder() {
		begin = false;
		builder = new ByteArrayBuilder();
	}
	
	public static final InfoHashBuilder newInstance() {
		return new InfoHashBuilder();
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
	 * 创建infoHash
	 */
	public InfoHash buildInfoHash() {
		byte[] byteArray = builder.toByteArray();
		byte[] bytes = new byte[byteArray.length - 7];
		System.arraycopy(byteArray, 0, bytes, 0, byteArray.length - 7);
		return InfoHash.newInstance(bytes);
	}

}
