package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.CollectionUtils;

/**
 * <p>文件信息</p>
 * <p>种子文件包含多个下载文件时使用</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentFile {

	/**
	 * <p>文件路径拼接时分隔符</p>
	 */
	public static final String SEPARATOR = "/";
	
	//================种子文件自带信息================//
	/**
	 * <p>文件大小</p>
	 */
	private Long length;
	/**
	 * <p>ed2k</p>
	 */
	private byte[] ed2k;
	/**
	 * <p>filehash</p>
	 */
	private byte[] filehash;
	/**
	 * <p>路径</p>
	 */
	private List<String> path;
	/**
	 * <p>路径UTF8</p>
	 */
	private List<String> pathUtf8;
	
	//================种子文件临时信息================//
	/**
	 * <p>是否选中下载</p>
	 */
	private transient boolean selected = false;

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
	 * <p>是否选中下载</p>
	 */
	public boolean selected() {
		return this.selected;
	}

	/**
	 * <p>设置选中下载</p>
	 */
	public void selected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * <p>文件路径</p>
	 */
	public String path() {
		if (CollectionUtils.isNotEmpty(this.pathUtf8)) {
			return String.join(TorrentFile.SEPARATOR, this.pathUtf8);
		}
		return String.join(TorrentFile.SEPARATOR, this.path);
	}
	
	/**
	 * <p>获取文件路径</p>
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