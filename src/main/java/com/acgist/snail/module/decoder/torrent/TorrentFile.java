package com.acgist.snail.module.decoder.torrent;

import java.util.List;

/**
 * 文件信息
 */
public class TorrentFile {

	private long length;
	private String md5sum;
	private List<String> path;

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getMd5sum() {
		return md5sum;
	}

	public void setMd5sum(String md5sum) {
		this.md5sum = md5sum;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

}