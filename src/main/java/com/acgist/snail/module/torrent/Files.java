package com.acgist.snail.module.torrent;

import java.util.List;

public class Files {
	
	private long length;
	private String md5sum;
	private List<String> path;

	public Files() {
	}

	public long getLength() {
		return length;
	}

	public Files(long length, String md5sum, List<String> path) {
		super();
		this.length = length;
		this.md5sum = md5sum;
		this.path = path;
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