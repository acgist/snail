package com.acgist.snail.pojo.bean;

/**
 * <p>种子文件基本数据</p>
 * 
 * @author acgist
 */
public class TorrentFileMatedata {

	/**
	 * <p>文件大小</p>
	 */
	protected Long length;
	/**
	 * <p>文件ed2k</p>
	 */
	protected byte[] ed2k;
	/**
	 * <p>文件filehash</p>
	 */
	protected byte[] filehash;
	
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
	 * <p>获取文件ed2k</p>
	 * 
	 * @return 文件ed2k
	 */
	public byte[] getEd2k() {
		return this.ed2k;
	}
	
	/**
	 * <p>设置文件ed2k</p>
	 * 
	 * @param ed2k 文件ed2k
	 */
	public void setEd2k(byte[] ed2k) {
		this.ed2k = ed2k;
	}

	/**
	 * <p>获取文件filehash</p>
	 * 
	 * @return 文件filehash
	 */
	public byte[] getFilehash() {
		return this.filehash;
	}

	/**
	 * <p>设置文件filehash</p>
	 * 
	 * @param filehash 文件filehash
	 */
	public void setFilehash(byte[] filehash) {
		this.filehash = filehash;
	}
	
}
