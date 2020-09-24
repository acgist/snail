package com.acgist.snail.pojo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.BEncodeDecoder;

/**
 * <p>文件信息</p>
 * <p>种子文件Hash：此类信息转为B编码计算SHA-1散列值（注意顺序）</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TorrentInfo extends TorrentFileMatedata implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * <p>填充文件前缀：{@value}</p>
	 * <p>不需要下载和显示</p>
	 */
	public static final String PADDING_FILE_PREFIX = "_____padding_file";
	/**
	 * <p>私有种子：{@value}</p>
	 */
	public static final byte PRIVATE_TORRENT = 1;
	/**
	 * <p>文件名称：{@value}</p>
	 */
	public static final String ATTR_NAME = "name";
	/**
	 * <p>文件名称UTF8：{@value}</p>
	 */
	public static final String ATTR_NAME_UTF8 = "name.utf-8";
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
	 * <p>特征信息：{@value}</p>
	 */
	public static final String ATTR_PIECES = "pieces";
	/**
	 * <p>Piece大小：{@value}</p>
	 */
	public static final String ATTR_PIECE_LENGTH = "piece length";
	/**
	 * <p>发布者：{@value}</p>
	 */
	public static final String ATTR_PUBLISHER = "publisher";
	/**
	 * <p>发布者UTF8：{@value}</p>
	 */
	public static final String ATTR_PUBLISHER_UTF8 = "publisher.utf-8";
	/**
	 * <p>发布者URL：{@value}</p>
	 */
	public static final String ATTR_PUBLISHER_URL = "publisher-url";
	/**
	 * <p>发布者URL UTF8：{@value}</p>
	 */
	public static final String ATTR_PUBLISHER_URL_UTF8 = "publisher-url.utf-8";
	/**
	 * <p>私有种子：{@value}</p>
	 */
	public static final String ATTR_PRIVATE = "private";
	/**
	 * <p>文件列表：{@value}</p>
	 */
	public static final String ATTR_FILES = "files";
	
	/**
	 * <p>名称</p>
	 * <p>单文件种子使用</p>
	 */
	private String name;
	/**
	 * <p>名称UTF8</p>
	 * <p>单文件种子使用</p>
	 */
	private String nameUtf8;
	/**
	 * <p>特征信息</p>
	 * <p>所有Piece Hash集合</p>
	 * <p>长度：Piece数量 * {@linkplain SystemConfig#SHA1_HASH_LENGTH 20}</p>
	 */
	private byte[] pieces;
	/**
	 * <p>Piece大小</p>
	 */
	private Long pieceLength;
	/**
	 * <p>发布者</p>
	 */
	private String publisher;
	/**
	 * <p>发布者UTF8</p>
	 */
	private String publisherUtf8;
	/**
	 * <p>发布者URL</p>
	 */
	private String publisherUrl;
	/**
	 * <p>发布者URL UTF8</p>
	 */
	private String publisherUrlUtf8;
	/**
	 * <p>私有种子</p>
	 * 
	 * @see #PRIVATE_TORRENT
	 */
	private Long privateTorrent;
	/**
	 * <p>文件列表</p>
	 * <p>多文件种子使用，单文件种子为空。</p>
	 */
	private List<TorrentFile> files;

	protected TorrentInfo() {
	}

	/**
	 * <p>读取种子信息</p>
	 * 
	 * @param map 种子信息
	 * @param encoding 编码
	 * 
	 * @return 种子信息
	 */
	public static final TorrentInfo valueOf(Map<String, Object> map, String encoding) {
		Objects.requireNonNull(map, "种子信息为空");
		final TorrentInfo info = new TorrentInfo();
		info.setName(BEncodeDecoder.getString(map, ATTR_NAME, encoding));
		info.setNameUtf8(BEncodeDecoder.getString(map, ATTR_NAME_UTF8));
		info.setLength(BEncodeDecoder.getLong(map, ATTR_LENGTH));
		info.setEd2k(BEncodeDecoder.getBytes(map, ATTR_ED2K));
		info.setFilehash(BEncodeDecoder.getBytes(map, ATTR_FILEHASH));
		info.setPieces(BEncodeDecoder.getBytes(map, ATTR_PIECES));
		info.setPieceLength(BEncodeDecoder.getLong(map, ATTR_PIECE_LENGTH));
		info.setPublisher(BEncodeDecoder.getString(map, ATTR_PUBLISHER, encoding));
		info.setPublisherUtf8(BEncodeDecoder.getString(map, ATTR_PUBLISHER_UTF8));
		info.setPublisherUrl(BEncodeDecoder.getString(map, ATTR_PUBLISHER_URL, encoding));
		info.setPublisherUrlUtf8(BEncodeDecoder.getString(map, ATTR_PUBLISHER_URL_UTF8));
		info.setPrivateTorrent(BEncodeDecoder.getLong(map, ATTR_PRIVATE));
		final List<Object> files = BEncodeDecoder.getList(map, ATTR_FILES);
		if(files != null) {
			info.setFiles(readFiles(files, encoding));
		} else {
			info.setFiles(new ArrayList<>());
		}
		return info;
	}
	
	/**
	 * <p>Piece数量</p>
	 * 
	 * @return Piece数量
	 */
	public int pieceSize() {
		return this.pieces.length / SystemConfig.SHA1_HASH_LENGTH;
	}
	
	/**
	 * <p>是否是私有种子</p>
	 * 
	 * @return 是否是私有种子
	 */
	public boolean isPrivateTorrent() {
		return this.privateTorrent != null && this.privateTorrent.byteValue() == PRIVATE_TORRENT;
	}
	
	/**
	 * <p>获取下载文件列表（兼容单文件种子）</p>
	 * <p>每个文件包含完整的路径</p>
	 * 
	 * @return 下载文件列表
	 */
	public List<TorrentFile> files() {
		if (this.files.isEmpty()) { // 单文件种子
			final TorrentFile file = new TorrentFile();
			file.setEd2k(this.ed2k);
			file.setFilehash(this.filehash);
			file.setLength(this.length);
			if (this.name != null) {
				file.setPath(List.of(this.name));
			}
			if (this.nameUtf8 != null) {
				file.setPathUtf8(List.of(this.nameUtf8));
			}
			return List.of(file);
		} else { // 多文件种子
			return this.files;
		}
	}
	
	/**
	 * <p>获取多文件种子文件列表</p>
	 * <p>每个元素都是一个Map，Map里面包含文件信息。</p>
	 * 
	 * @param files 文件信息
	 * @param encoding 编码
	 * 
	 * @return 文件列表
	 */
	private static final List<TorrentFile> readFiles(List<Object> files, String encoding) {
		return files.stream()
			.map(value -> (Map<?, ?>) value)
			.map(value -> TorrentFile.valueOf(value, encoding))
			.collect(Collectors.toList());
	}
	
	// ============== GETTER SETTER ============== //
	
	/**
	 * <p>获取名称</p>
	 * 
	 * @return 名称
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * <p>设置名称</p>
	 * 
	 * @param name 名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * <p>获取名称UTF8</p>
	 * 
	 * @return 名称UTF8
	 */
	public String getNameUtf8() {
		return this.nameUtf8;
	}

	/**
	 * <p>设置名称UTF8</p>
	 * 
	 * @param nameUtf8 名称UTF8
	 */
	public void setNameUtf8(String nameUtf8) {
		this.nameUtf8 = nameUtf8;
	}

	/**
	 * <p>获取特征信息</p>
	 * 
	 * @return 特征信息
	 */
	public byte[] getPieces() {
		return this.pieces;
	}

	/**
	 * <p>设置特征信息</p>
	 * 
	 * @param pieces 特征信息
	 */
	public void setPieces(byte[] pieces) {
		this.pieces = pieces;
	}

	/**
	 * <p>获取发布者</p>
	 * 
	 * @return 发布者
	 */
	public String getPublisher() {
		return this.publisher;
	}

	/**
	 * <p>设置发布者</p>
	 * 
	 * @param publisher 发布者
	 */
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * <p>获取发布者UTF8</p>
	 * 
	 * @return 发布者UTF8
	 */
	public String getPublisherUtf8() {
		return this.publisherUtf8;
	}

	/**
	 * <p>设置发布者UTF8</p>
	 * 
	 * @param publisherUtf8 发布者UTF8
	 */
	public void setPublisherUtf8(String publisherUtf8) {
		this.publisherUtf8 = publisherUtf8;
	}

	/**
	 * <p>获取发布者URL</p>
	 * 
	 * @return 发布者URL
	 */
	public String getPublisherUrl() {
		return this.publisherUrl;
	}

	/**
	 * <p>设置发布者URL</p>
	 * 
	 * @param publisherUrl 发布者URL
	 */
	public void setPublisherUrl(String publisherUrl) {
		this.publisherUrl = publisherUrl;
	}

	/**
	 * <p>获取发布者URL UTF8</p>
	 * 
	 * @return 发布者URL UTF8
	 */
	public String getPublisherUrlUtf8() {
		return this.publisherUrlUtf8;
	}

	/**
	 * <p>设置发布者URL UTF8</p>
	 * 
	 * @param publisherUrlUtf8 发布者URL UTF8
	 */
	public void setPublisherUrlUtf8(String publisherUrlUtf8) {
		this.publisherUrlUtf8 = publisherUrlUtf8;
	}

	/**
	 * <p>获取私有种子</p>
	 * 
	 * @return 私有种子
	 */
	public Long getPrivateTorrent() {
		return this.privateTorrent;
	}

	/**
	 * <p>设置私有种子</p>
	 * 
	 * @param privateTorrent 私有种子
	 */
	public void setPrivateTorrent(Long privateTorrent) {
		this.privateTorrent = privateTorrent;
	}

	/**
	 * <p>获取Piece大小</p>
	 * 
	 * @return Piece大小
	 */
	public Long getPieceLength() {
		return this.pieceLength;
	}

	/**
	 * <p>设置Piece大小</p>
	 * 
	 * @param pieceLength Piece大小
	 */
	public void setPieceLength(Long pieceLength) {
		this.pieceLength = pieceLength;
	}

	/**
	 * <p>获取文件列表</p>
	 * 
	 * @return 文件列表
	 */
	public List<TorrentFile> getFiles() {
		return this.files;
	}

	/**
	 * <p>设置文件列表</p>
	 * 
	 * @param files 文件列表
	 */
	public void setFiles(List<TorrentFile> files) {
		this.files = files;
	}

}