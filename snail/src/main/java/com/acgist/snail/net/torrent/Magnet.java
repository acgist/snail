package com.acgist.snail.net.torrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>磁力链接</p>
 * <p>协议链接：https://baike.baidu.com/item/%E7%A3%81%E5%8A%9B%E9%93%BE%E6%8E%A5/5867775</p>
 * <p>注意：只支持单文件下载</p>
 * 
 * @author acgist
 */
public final class Magnet implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>磁力链接类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>MD5</p>
		 */
		MD5("urn:md5:"),
		/**
		 * <p>AICH</p>
		 */
		AICH("urn:aich:"),
		/**
		 * <p>Kazaa</p>
		 */
		KAZAA("urn:kzhash:"),
		/**
		 * <p>BTIH：BitTorrent</p>
		 */
		BTIH("urn:btih:"),
		/**
		 * <p>ED2K</p>
		 */
		ED2K("urn:ed2k:"),
		/**
		 * <p>SHA-1</p>
		 */
		SHA1("urn:sha1:"),
		/**
		 * <p>CRC-32</p>
		 */
		CRC32("urn:crc32:"),
		/**
		 * <p>TTH：TigerTree</p>
		 */
		TTH("urn:tree:tiger:"),
		/**
		 * <p>BitPrint</p>
		 */
		BITPRINT("urn:bitprint:");
		
		/**
		 * <p>XT前缀</p>
		 */
		private final String prefix;
		
		/**
		 * @param prefix XT前缀
		 */
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
	 * <p>文件大小</p>
	 */
	private Long xl;
	/**
	 * <p>资源URN</p>
	 * <p>文件散列值URN</p>
	 */
	private String xt;
	/**
	 * <p>文件链接</p>
	 * <p>原始文件链接</p>
	 */
	private String as;
	/**
	 * <p>绝对资源</p>
	 * <p>种子文件链接</p>
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
	 * <p>BT：InfoHashHex</p>
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
	 * <p>判断是否支持下载</p>
	 * 
	 * @return 是否支持下载
	 */
	public boolean supportDownload() {
		return
			this.type == Type.BTIH &&
			StringUtils.isNotEmpty(this.hash);
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
	 * <p>获取文件大小</p>
	 * 
	 * @return 文件大小
	 */
	public Long getXl() {
		return xl;
	}

	/**
	 * <p>设置文件大小</p>
	 * 
	 * @param xl 文件大小
	 */
	public void setXl(Long xl) {
		this.xl = xl;
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
		return BeanUtils.toString(this);
	}
	
}
