package com.acgist.snail.net.torrent;

/**
 * <p>种子文件基本信息</p>
 * 
 * @author acgist
 */
public abstract class TorrentFileMatedata {

	/**
	 * <p>文件Ed2k：{@value}</p>
	 */
	public static final String ATTR_ED2K = "ed2k";
	/**
	 * <p>文件大小：{@value}</p>
	 */
	public static final String ATTR_LENGTH = "length";
	/**
	 * <p>文件Hash：{@value}</p>
	 */
	public static final String ATTR_FILEHASH = "filehash";
	
	/**
	 * <p>文件Ed2k</p>
	 */
	protected byte[] ed2k;
	/**
	 * <p>文件大小</p>
	 */
	protected Long length;
	/**
	 * <p>文件Hash</p>
	 */
	protected byte[] filehash;
	
	/**
	 * <p>获取文件Ed2k</p>
	 * 
	 * @return 文件Ed2k
	 */
	public byte[] getEd2k() {
		return this.ed2k;
	}
	
	/**
	 * <p>设置文件Ed2k</p>
	 * 
	 * @param ed2k 文件Ed2k
	 */
	public void setEd2k(byte[] ed2k) {
		this.ed2k = ed2k;
	}
	
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
	 * <p>获取文件Hash</p>
	 * 
	 * @return 文件Hash
	 */
	public byte[] getFilehash() {
		return this.filehash;
	}

	/**
	 * <p>设置文件Hash</p>
	 * 
	 * @param filehash 文件Hash
	 */
	public void setFilehash(byte[] filehash) {
		this.filehash = filehash;
	}
	
}
