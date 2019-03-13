package com.acgist.snail.coder.torrent.pojo;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.utils.StringUtils;

/**
 * 文件列表信息：单文件时files为空
 */
public class TorrentInfo {

	private String name; // 名称
	private String nameUtf8; // 名称：utf8
	private Long length; // 大小
	private byte[] ed2k; // ed2k
	private byte[] filehash; // 文件hash
	private byte[] pieces; // 特征信息：每个piece的hash值占用20个字节
	private String publisher; // 发布者
	private String publisherUtf8; // 发布者UTF8
	private String publisherUrl; // 发布者URL
	private String publisherUrlUtf8; // 发布者URL UTF8
	private Long pieceLength; // 块大小
	private List<TorrentFile> files = new ArrayList<>(); // 多文件时存在

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameUtf8() {
		return nameUtf8;
	}

	public void setNameUtf8(String nameUtf8) {
		this.nameUtf8 = nameUtf8;
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
	
	public byte[] getPieces() {
		return pieces;
	}

	public void setPieces(byte[] pieces) {
		this.pieces = pieces;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisherUtf8() {
		return publisherUtf8;
	}

	public void setPublisherUtf8(String publisherUtf8) {
		this.publisherUtf8 = publisherUtf8;
	}

	public String getPublisherUrl() {
		return publisherUrl;
	}

	public void setPublisherUrl(String publisherUrl) {
		this.publisherUrl = publisherUrl;
	}

	public String getPublisherUrlUtf8() {
		return publisherUrlUtf8;
	}

	public void setPublisherUrlUtf8(String publisherUrlUtf8) {
		this.publisherUrlUtf8 = publisherUrlUtf8;
	}

	public Long getPieceLength() {
		return pieceLength;
	}

	public void setPieceLength(Long pieceLength) {
		this.pieceLength = pieceLength;
	}

	public List<TorrentFile> getFiles() {
		return files;
	}

	public void setFiles(List<TorrentFile> files) {
		this.files = files;
	}
	
	public TorrentFile lastTorrentFile() {
		return this.files.get(this.files.size() - 1);
	}
	
	/**
	 * 列出下载文件（兼容单个文件）
	 */
	public List<TorrentFile> files() {
		if(files.isEmpty()) {
			TorrentFile file = new TorrentFile();
			file.setEd2k(this.ed2k);
			file.setFilehash(this.filehash);
			file.setLength(this.length);
			if(this.name != null) {
				file.setPath(List.of(this.name));
			}
			if(this.nameUtf8 != null) {
				file.setPathUtf8(List.of(this.nameUtf8));
			}
			return List.of(file);
		} else {
			return files;
		}
	}
	
}