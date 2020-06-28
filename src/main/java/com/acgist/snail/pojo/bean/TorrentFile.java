package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.system.format.BEncodeDecoder;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>文件信息</p>
 * <p>种子文件包含多个下载文件时使用</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentFile {

	/**
	 * <p>文件路径拼接时分隔符：{@value}</p>
	 */
	public static final String SEPARATOR = "/";
	/**
	 * <p>文件大小：{@value}</p>
	 */
	public static final String ATTR_LENGTH = "length";
	/**
	 * <p>文件ED2K：{@value}</p>
	 */
	public static final String ATTR_ED2K = "ed2k";
	/**
	 * <p>文件Hash：{@value}</p>
	 */
	public static final String ATTR_FILEHASH = "filehash";
	/**
	 * <p>文件路径：{@value}</p>
	 */
	public static final String ATTR_PATH = "path";
	/**
	 * <p>文件路径UTF8：{@value}</p>
	 */
	public static final String ATTR_PATH_UTF8 = "path.utf-8";
	
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
	
	/**
	 * <p>读取种子文件信息</p>
	 * 
	 * @param map 种子文件信息
	 * @param encoding 编码
	 * 
	 * @return 种子文件信息
	 */
	public static final TorrentFile valueOf(Map<?, ?> map, String encoding) {
		Objects.requireNonNull(map, "种子文件信息为空");
		final TorrentFile file = new TorrentFile();
		file.setLength(BEncodeDecoder.getLong(map, ATTR_LENGTH));
		file.setEd2k(BEncodeDecoder.getBytes(map, ATTR_ED2K));
		file.setFilehash(BEncodeDecoder.getBytes(map, ATTR_FILEHASH));
		final List<Object> path = BEncodeDecoder.getList(map, ATTR_PATH);
		if(path != null) {
			file.setPath(readPath(path, encoding));
		} else {
			file.setPath(new ArrayList<>());
		}
		final List<Object> pathUtf8 = BEncodeDecoder.getList(map, ATTR_PATH_UTF8);
		if(pathUtf8 != null) {
			file.setPathUtf8(readPath(pathUtf8, null)); // 默认编码：UTF-8
		} else {
			file.setPathUtf8(new ArrayList<>());
		}
		return file;
	}

	/**
	 * <p>是否选中下载</p>
	 * 
	 * @return 是否选中下载
	 */
	public boolean selected() {
		return this.selected;
	}

	/**
	 * <p>设置选中下载</p>
	 * 
	 * @param selected 是否选中下载
	 */
	public void selected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * <p>获取文件路径</p>
	 * 
	 * @return 文件路径
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
	 * 
	 * @param path 文件路径信息
	 * @param encoding 编码
	 * 
	 * @return 文件路径
	 */
	private static final List<String> readPath(List<Object> path, String encoding) {
		return path.stream()
			.map(value -> StringUtils.getStringCharset(value, encoding))
			.collect(Collectors.toList());
	}

	// ==============GETTER SETTER============== //
	
	/**
	 * <p>获取文件大小</p>
	 * 
	 * @return 文件大小
	 */
	public Long getLength() {
		return this.length;
	}

	/**
	 * <p>设置文件大小</p>
	 * 
	 * @param length 文件大小
	 */
	public void setLength(Long length) {
		this.length = length;
	}

	/**
	 * <p>获取ed2k</p>
	 * 
	 * @return ed2k
	 */
	public byte[] getEd2k() {
		return this.ed2k;
	}

	/**
	 * <p>设置ed2k</p>
	 * 
	 * @param ed2k ed2k
	 */
	public void setEd2k(byte[] ed2k) {
		this.ed2k = ed2k;
	}

	/**
	 * <p>获取filehash</p>
	 * 
	 * @return filehash
	 */
	public byte[] getFilehash() {
		return this.filehash;
	}

	/**
	 * <p>设置filehash</p>
	 * 
	 * @param filehash filehash
	 */
	public void setFilehash(byte[] filehash) {
		this.filehash = filehash;
	}

	/**
	 * <p>获取路径</p>
	 * 
	 * @return 路径
	 */
	public List<String> getPath() {
		return this.path;
	}

	/**
	 * <p>设置路径</p>
	 * 
	 * @param path 路径
	 */
	public void setPath(List<String> path) {
		this.path = path;
	}

	/**
	 * <p>获取路径UTF8</p>
	 * 
	 * @return 路径UTF8
	 */
	public List<String> getPathUtf8() {
		return this.pathUtf8;
	}

	/**
	 * <p>设置路径UTF8</p>
	 * 
	 * @param pathUtf8 路径UTF8
	 */
	public void setPathUtf8(List<String> pathUtf8) {
		this.pathUtf8 = pathUtf8;
	}

}