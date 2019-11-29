package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>磁力链接</p>
 * <p>只支持单文件磁力链接下载（不支持多文件）</p>
 * <p>协议链接：https://baike.baidu.com/item/%E7%A3%81%E5%8A%9B%E9%93%BE%E6%8E%A5/5867775</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class Magnet {

	/**
	 * <p>磁力链接类型</p>
	 */
	public enum Type {
		
		/** md5 */
		MD5(		"urn:md5:"),
		/** aich */
		AICH(		"urn:aich:"),
		/** btih：BitTorrent InfoHash */
		BTIH(		"urn:btih:"),
		/** ed2k */
		ED2K(		"urn:ed2k:"),
		/** sha1 */
		SHA1(		"urn:sha1:"),
		/** crc32 */
		CRC32(		"urn:crc32:"),
		/** tth */
		TTH(		"urn:tree:tiger:"),
		/** bitprint */
		BITPRINT(	"urn:bitprint:");
		
		/**
		 * xt
		 */
		private final String xt;
		
		private Type(String xt) {
			this.xt = xt;
		}
		
		public String xt() {
			return this.xt;
		}
		
	}
	
	/**
	 * <p>显示名称</p>
	 */
	private String dn;
	/**
	 * <p>资源URN</p>
	 */
	private String xt;
	/**
	 * <p>文件链接（经过编码）</p>
	 * <p>BT磁力链接可以直接使用下载种子文件</p>
	 */
	private String as;
	/**
	 * <p>绝对资源（经过编码）</p>
	 * <p>BT磁力链接可以直接使用下载种子文件</p>
	 */
	private String xs;
	/**
	 * <p>Tracker服务器列表</p>
	 */
	private List<String> tr;
	/**
	 * <p>类型</p>
	 * <p>参考：xt</p>
	 */
	private Type type;
	/**
	 * <p>BT：BitTorrent InfoHashHex</p>
	 * <p>参考：xt</p>
	 */
	private String hash;

	/**
	 * <p>添加Tracker服务器</p>
	 */
	public void addTr(String tr) {
		if(this.tr == null) {
			this.tr = new ArrayList<>();
		}
		this.tr.add(tr);
	}
	
	/**
	 * <p>是否支持下载</p>
	 * <p>类型：{@linkplain Type#BTIH BTIH}</p>
	 */
	public boolean supportDownload() {
		return this.type == Type.BTIH && StringUtils.isNotEmpty(this.hash);
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public List<String> getTr() {
		return tr;
	}

	public void setTr(List<String> tr) {
		this.tr = tr;
	}

	public String getXt() {
		return xt;
	}

	public void setXt(String xt) {
		this.xt = xt;
	}

	public String getAs() {
		return as;
	}

	public void setAs(String as) {
		this.as = as;
	}

	public String getXs() {
		return xs;
	}

	public void setXs(String xs) {
		this.xs = xs;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.type, this.hash, this.dn, this.tr);
	}
	
}
