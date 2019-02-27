package com.acgist.snail.module.coder.torrent;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.utils.StringUtils;

/**
 * 文件信息
 */
public class TorrentFile {

	private Long length; // 大小
	private byte[] ed2k; // ed2k
	private byte[] filehash; // 文件hash
	private List<String> path = new ArrayList<>(); // 路径
	private List<String> pathUtf8 = new ArrayList<>(); // 路径UTF8

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public byte[] getEd2k() {
		return ed2k;
	}
	public String getEd2kHex() {
		return StringUtils.hex(ed2k);
	}
	
	public void setEd2k(byte[] ed2k) {
		this.ed2k = ed2k;
	}

	public byte[] getFilehash() {
		return filehash;
	}
	
	public String getFilehashHex() {
		return StringUtils.hex(filehash);
	}
	
	public void setFilehash(byte[] filehash) {
		this.filehash = filehash;
	}

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public List<String> getPathUtf8() {
		return pathUtf8;
	}

	public void setPathUtf8(List<String> pathUtf8) {
		this.pathUtf8 = pathUtf8;
	}

}