package com.acgist.snail.pojo.bean;

import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.PeerUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>种子InfoHash</p>
 * <p>种子文件：包含所有信息</p>
 * <p>磁力链接：size=0、info=null</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class InfoHash {

	/**
	 * <p>种子info数据长度</p>
	 */
	private int size;
	/**
	 * <p>种子info数据</p>
	 */
	private byte[] info;
	/**
	 * <p>种子info数据Hash</p>
	 */
	private final byte[] infoHash;
	/**
	 * <p>HTTP传输编码种子info数据Hash</p>
	 * 
	 * @see #infoHash
	 */
	private final String infoHashUrl;
	
	private InfoHash(byte[] infoHash) {
		this.infoHash = infoHash;
		this.infoHashUrl = buildInfoHashUrl();
	}

	/**
	 * <p>生成InfoHash</p>
	 * 
	 * @param data 种子Info
	 * 
	 * @return InfoHash
	 */
	public static final InfoHash newInstance(byte[] data) {
		final InfoHash infoHash = new InfoHash(StringUtils.sha1(data));
		infoHash.info = data;
		infoHash.size = data.length;
		return infoHash;
	}
	
	/**
	 * <p>生成InfoHash</p>
	 * 
	 * @param hash 种子info数据Hash
	 * 
	 * @return InfoHash
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final InfoHash newInstance(String hash) throws DownloadException {
		if(hash == null) {
			throw new DownloadException("不支持的Hash：" + hash);
		}
		hash = hash.trim();
		if(Protocol.Type.verifyMagnetHash40(hash)) {
			return new InfoHash(StringUtils.unhex(hash));
		} else if(Protocol.Type.verifyMagnetHash32(hash)) {
			return new InfoHash(Base32Utils.decode(hash));
		} else {
			throw new DownloadException("不支持的Hash：" + hash);
		}
	}
	
	/**
	 * <p>创建HTTP传输编码种子info数据Hash</p>
	 * 
	 * @return HTTP传输编码种子info数据Hash
	 */
	private String buildInfoHashUrl() {
		// 标准编码
		return PeerUtils.urlEncode(this.infoHash);
		// 全部编码
//		return PeerUtils.urlEncode(this.infoHashHex());
	}
	
	public int size() {
		return this.size;
	}
	
	public void size(int size) {
		this.size = size;
	}
	
	public byte[] info() {
		return this.info;
	}
	
	public void info(byte[] info) {
		this.info = info;
	}

	public byte[] infoHash() {
		return infoHash;
	}
	
	/**
	 * <p>16进制种子info数据Hash（小写）（40位）</p>
	 * 
	 * @return 16进制种子info数据Hash
	 */
	public String infoHashHex() {
		return StringUtils.hex(this.infoHash);
	}
	
	/**
	 * <p>获取HTTP传输编码种子info数据Hash</p>
	 * 
	 * @return HTTP传输编码种子info数据Hash
	 */
	public String infoHashUrl() {
		return this.infoHashUrl;
	}
	
}
