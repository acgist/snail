package com.acgist.snail.module.decoder.torrent;

import java.util.List;

/**
 * 文件列表信息
 */
public class TorrentFileInfo {

	private String name;
	private byte[] pieces;
	private long piecesLength;
	private long length;
	private String md5sum;
	private List<TorrentFile> files;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getPieces() {
		return pieces;
	}

	public void setPieces(byte[] pieces) {
		this.pieces = pieces;
	}

	public long getPiecesLength() {
		return piecesLength;
	}

	public void setPiecesLength(long piecesLength) {
		this.piecesLength = piecesLength;
	}

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

	public List<TorrentFile> getFiles() {
		return files;
	}

	public void setFiles(List<TorrentFile> files) {
		this.files = files;
	}

}