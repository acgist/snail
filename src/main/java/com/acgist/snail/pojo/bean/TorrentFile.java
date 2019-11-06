package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.CollectionUtils;

/**
 * 文件信息
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentFile {

	/**
	 * 文件路径拼接时分隔符
	 */
	public static final String SEPARATOR = "/";
	
	/**
	 * 是否选中下载
	 */
	private boolean selected = false;
	/**
	 * 文件大小
	 */
	private Long length;
	/**
	 * ed2k
	 */
	private byte[] ed2k;
	/**
	 * filehash
	 */
	private byte[] filehash;
	/**
	 * 路径
	 */
	private List<String> path;
	/**
	 * 路径UTF8
	 */
	private List<String> pathUtf8;

	protected TorrentFile() {
	}
	
	public static final TorrentFile valueOf(Map<?, ?> map) {
		if(map == null) {
			return null;
		}
		final TorrentFile file = new TorrentFile();
		file.setLength(BEncodeDecoder.getLong(map, "length"));
		file.setEd2k(BEncodeDecoder.getBytes(map, "ed2k"));
		file.setFilehash(BEncodeDecoder.getBytes(map, "filehash"));
		final List<Object> path = BEncodeDecoder.getList(map, "path");
		if(path != null) {
			file.setPath(path(path));
		} else {
			file.setPath(new ArrayList<>());
		}
		final List<Object> pathUtf8 = BEncodeDecoder.getList(map, "path.utf-8");
		if(pathUtf8 != null) {
			file.setPathUtf8(path(pathUtf8));
		} else {
			file.setPathUtf8(new ArrayList<>());
		}
		return file;
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
	public void selected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * 拼接路径
	 */
	public String path() {
		if (CollectionUtils.isNotEmpty(this.pathUtf8)) {
			return String.join(TorrentFile.SEPARATOR, this.pathUtf8);
		}
		return String.join(TorrentFile.SEPARATOR, this.path);
	}
	
	/**
	 * <p>读取路径</p>
	 * <p>每个元素都是一个字节数组</p>
	 */
	private static final List<String> path(List<Object> path) {
		return path.stream()
			.map(value -> BEncodeDecoder.getString(value))
			.collect(Collectors.toList());
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