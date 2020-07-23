package com.acgist.snail.pojo.bean;

import java.io.Serializable;
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
public final class Magnet implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>磁力链接类型</p>
	 */
	public enum Type {
		
		/** md5 */
		MD5("urn:md5:"),
		/** aich */
		AICH("urn:aich:"),
		/** btih：BitTorrent InfoHash */
		BTIH("urn:btih:"),
		/** ed2k */
		ED2K("urn:ed2k:"),
		/** sha1 */
		SHA1("urn:sha1:"),
		/** crc32 */
		CRC32("urn:crc32:"),
		/** tth */
		TTH("urn:tree:tiger:"),
		/** bitprint */
		BITPRINT("urn:bitprint:");
		
		/**
		 * <p>XT前缀</p>
		 */
		private final String prefix;
		
		private Type(String prefix) {
			this.prefix = prefix;
		}
		
		/**
		 * <p>获取XT前缀</p>
		 * 
		 * @return XT前缀
		 */
		public String prefix() {
			return this.prefix;
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
	 * <p>磁力链接类型</p>
	 * 
	 * @see Type
	 */
	private Type type;
	/**
	 * <p>BT：BitTorrent InfoHashHex</p>
	 * 
	 * @see Type
	 */
	private String hash;

	/**
	 * <p>添加Tracker服务器</p>
	 * 
	 * @param tr Tracker服务器
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
	 * 
	 * @return 是否支持下载
	 */
	public boolean supportDownload() {
		return this.type == Type.BTIH && StringUtils.isNotEmpty(this.hash);
	}
	
	/**
	 * <p>获取显示名称</p>
	 * 
	 * @return 显示名称
	 */
	public String getDn() {
		return this.dn;
	}

	/**
	 * <p>设置显示名称</p>
	 * 
	 * @param dn 显示名称
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * <p>获取资源URN</p>
	 * 
	 * @return 资源URN
	 */
	public String getXt() {
		return this.xt;
	}

	/**
	 * <p>设置资源URN</p>
	 * 
	 * @param xt 资源URN
	 */
	public void setXt(String xt) {
		this.xt = xt;
	}

	/**
	 * <p>获取文件链接</p>
	 * 
	 * @return 文件链接
	 */
	public String getAs() {
		return this.as;
	}

	/**
	 * <p>设置文件链接</p>
	 * 
	 * @param as 文件链接
	 */
	public void setAs(String as) {
		this.as = as;
	}

	/**
	 * <p>获取绝对资源</p>
	 * 
	 * @return 绝对资源
	 */
	public String getXs() {
		return this.xs;
	}

	/**
	 * <p>设置绝对资源</p>
	 * 
	 * @param xs 绝对资源
	 */
	public void setXs(String xs) {
		this.xs = xs;
	}
	
	/**
	 * <p>获取Tracker服务器列表</p>
	 * 
	 * @return Tracker服务器列表
	 */
	public List<String> getTr() {
		return this.tr;
	}
	
	/**
	 * <p>设置Tracker服务器列表</p>
	 * 
	 * @param tr Tracker服务器列表
	 */
	public void setTr(List<String> tr) {
		this.tr = tr;
	}
	
	/**
	 * <p>获取磁力链接类型</p>
	 * 
	 * @return 磁力链接类型
	 */
	public Type getType() {
		return this.type;
	}
	
	/**
	 * <p>设置磁力链接类型</p>
	 * 
	 * @param type 磁力链接类型
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * <p>获取Hash</p>
	 * 
	 * @return Hash
	 */
	public String getHash() {
		return this.hash;
	}

	/**
	 * <p>设置Hash</p>
	 * 
	 * @param hash Hash
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.dn, this.tr, this.type, this.hash);
	}
	
}
