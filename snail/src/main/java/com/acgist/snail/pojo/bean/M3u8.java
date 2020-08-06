package com.acgist.snail.pojo.bean;

import java.util.List;

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
	 * <p>文件列表、M3U8列表</p>
	 * <p>多级M3U8列表：按照码率从小到大排序</p>
	 */
	private final List<String> links;
	
	/**
	 * @param type 类型
	 * @param links 文件列表
	 */
	public M3u8(Type type, List<String> links) {
		this.type = type;
		this.links = links;
	}
	
	/**
	 * <p>获取类型</p>
	 * 
	 * @return 获取类型
	 */
	public Type getType() {
		return type;
	}

	/**
	 * <p>获取文件列表</p>
	 * 
	 * @return 文件列表
	 */
	public List<String> getLinks() {
		return links;
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