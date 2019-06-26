package com.acgist.snail.pojo.bean;

import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 磁力链接
 * 
 * @author acgist
 * @since 1.1.0
 */
public class Magnet {

	/**
	 * 磁力链接类型
	 */
	public enum Type {
		
		md5("urn:md5:"),
		aich("urn:aich:"),
		btih("urn:btih:"),
		ed2k("urn:ed2k:"),
		sha1("urn:sha1:"),
		crc32("urn:crc32:"),
		tth("urn:tree:tiger:"),
		bitprint("urn:bitprint:");
		
		private final String xt;

		private Type(String xt) {
			this.xt = xt;
		}
		
		public String xt() {
			return this.xt;
		}
		
	}
	
	/**
	 * 类型
	 */
	private Type type;
	/**
	 * 显示文件名称
	 */
	private String dn;
	/**
	 * Tracker
	 * TODO：多个
	 */
	private String tr;
	/**
	 * 资源URN
	 */
	private String xt;
	/**
	 * 可接受来源
	 */
	private String as;
	/**
	 * 绝对资源（P2P链接）
	 * TODO：直接下载
	 */
	private String xs;
	/**
	 * 绝对长度（字节）
	 */
	private String xl;
	/**
	 * 文件列表
	 */
	private String mt;
	/**
	 * 关键字（搜索）
	 */
	private String kt;
	/**
	 * 文件xt里面的hash值
	 */
	private String hash;

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

	public String getTr() {
		return tr;
	}

	public void setTr(String tr) {
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

	public String getXl() {
		return xl;
	}

	public void setXl(String xl) {
		this.xl = xl;
	}

	public String getMt() {
		return mt;
	}

	public void setMt(String mt) {
		this.mt = mt;
	}

	public String getKt() {
		return kt;
	}

	public void setKt(String kt) {
		this.kt = kt;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * 支持下载
	 */
	public boolean supportDownload() {
		return this.type == Type.btih && StringUtils.isNotEmpty(this.hash);
	}
	
	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.type, this.hash, this.dn, this.tr);
	}
	
}
