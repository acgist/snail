package com.acgist.snail.pojo.bean;

import java.util.List;

import com.acgist.snail.net.hls.crypt.HlsCrypt;
import com.acgist.snail.utils.ObjectUtils;

/**
 * <p>M3U8信息</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class M3u8 {
	
	/**
	 * <p>类型</p>
	 */
	public enum Type {
		
		/** 文件列表 */
		FILE,
		/** M3U8列表 */
		M3U8,
		/** 流媒体列表 */
		STREAM;
		
	}
	
	/**
	 * <p>类型</p>
	 */
	private final Type type;
	/**
	 * <p>加密工具</p>
	 * <p>为空时表示数据没有加密</p>
	 */
	private final HlsCrypt hlsCrypt;
	/**
	 * <p>文件列表、M3U8列表</p>
	 * <p>多级M3U8列表：按照码率从小到大排序</p>
	 */
	private final List<String> links;
	
	/**
	 * @param type 类型
	 * @param hlsCrypt 加密工具
	 * @param links 文件列表
	 */
	public M3u8(Type type, HlsCrypt hlsCrypt, List<String> links) {
		this.type = type;
		this.hlsCrypt = hlsCrypt;
		this.links = links;
	}
	
	/**
	 * <p>获取类型</p>
	 * 
	 * @return 获取类型
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * <p>获取加密工具</p>
	 * 
	 * @return 加密工具
	 */
	public HlsCrypt getHlsCrypt() {
		return this.hlsCrypt;
	}
	
	/**
	 * <p>获取文件列表</p>
	 * 
	 * @return 文件列表
	 */
	public List<String> getLinks() {
		return this.links;
	}
	
	/**
	 * <p>获取码率最大的链接</p>
	 * 
	 * @return 码率最大的链接
	 */
	public String maxRateLink() {
		// 码率排序
		return this.links.get(this.links.size() - 1);
	}

	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.type, this.links);
	}
	
}