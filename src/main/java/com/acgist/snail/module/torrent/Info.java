package com.acgist.snail.module.torrent;

import java.util.List;

public class Info {

	private String name;
	private byte[] pieces;
	private long piecesLength;
	private long length;
	private String md5sum;
	private List<Files> files;

	public Info() {
	}

	public Info(String name, byte[] pieces, long piecesLength, long length, String md5sum, List<Files> files) {
		super();
		this.name = name;
		this.pieces = pieces;
		this.piecesLength = piecesLength;
		this.length = length;
		this.md5sum = md5sum;
		this.files = files;
	}

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

	public List<Files> getFiles() {
		return files;
	}

	public void setFiles(List<Files> files) {
		this.files = files;
	}
}