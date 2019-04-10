package com.acgist.snail.protocol.torrent.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.utils.BCodeUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 下载文件信息
 */
public class TorrentFile {

	private boolean selected = false; // 是否选择
	
	private Long length; // 大小
	private byte[] ed2k; // ed2k
	private byte[] filehash; // 文件hash
	private List<String> path; // 路径
	private List<String> pathUtf8; // 路径UTF8

	protected TorrentFile() {
	}
	
	public static final TorrentFile valueOf(Map<?, ?> map) {
		if(map == null) {
			return null;
		}
		TorrentFile file = new TorrentFile();
		file.setLength(BCodeUtils.getLong(map, "length"));
		file.setEd2k(BCodeUtils.getBytes(map, "ed2k"));
		file.setFilehash(BCodeUtils.getBytes(map, "filehash"));
		List<?> path = (List<?>) map.get("path");
		if(path != null) {
			file.setPath(
				path.stream()
				.map(value -> BCodeUtils.getString(value))
				.collect(Collectors.toList())
			);
		} else {
			file.setPath(new ArrayList<>());
		}
		List<?> pathUtf8 = (List<?>) map.get("path.utf-8");
		if(pathUtf8 != null) {
			file.setPathUtf8(
				pathUtf8.stream()
				.map(value -> BCodeUtils.getString(value))
				.collect(Collectors.toList())
				);
		} else {
			file.setPathUtf8(new ArrayList<>());
		}
		return file;
	}
	
	public String path() {
		if (CollectionUtils.isNotEmpty(pathUtf8)) {
			return String.join("/", this.pathUtf8);
		}
		return String.join("/", this.path);
	}

	public String ed2kHex() {
		return StringUtils.hex(this.ed2k);
	}
	
	public String filehashHex() {
		return StringUtils.hex(this.filehash);
	}

	/**
	 * 是否选中
	 */
	public boolean selected() {
		return this.selected;
	}

	/**
	 * 设置选中
	 */
	public void select(boolean selected) {
		this.selected = selected;
	}
	
	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public byte[] getEd2k() {
		return ed2k;
	}

	public void setEd2k(byte[] ed2k) {
		this.ed2k = ed2k;
	}

	public byte[] getFilehash() {
		return filehash;
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