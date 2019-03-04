package com.acgist.snail.coder.torrent;

import com.acgist.snail.utils.StringUtils;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

/**
 * 种子磁力链接HASH生成器
 */
public class TorrentHashBuilder {

	private boolean begin;
	private ByteArrayBuilder builder;
	
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

	public String hash() {
		byte[] byteArray = builder.toByteArray();
		byte[] bytes = new byte[byteArray.length - 7];
		System.arraycopy(byteArray, 0, bytes, 0, byteArray.length - 7);
		return StringUtils.sha1(bytes);
	}

}
