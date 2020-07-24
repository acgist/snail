package com.acgist.snail.pojo.bean;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.system.exception.DownloadException;
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
	 * <p>文件列表</p>
	 * <p>如果是多级M3U8列表，安装码率从小到大排序。</p>
	 */
	private final List<String> links;
	/**
	 * <p>获取文件索引</p>
	 */
	private final AtomicInteger index;
	
	/**
	 * @param type 类型
	 * @param links 文件列表
	 */
	public M3u8(Type type, List<String> links) {
		this.type = type;
		this.links = links;
		this.index = new AtomicInteger(0);
	}

	/**
	 * <p>是否还有下一个文件</p>
	 * 
	 * @return true-有；fasle-没有；
	 */
	public boolean havaNextFile() {
		synchronized (this.index) {
			return this.index.get() < this.links.size();
		}
	}
	
	/**
	 * <p>获取下一个文件下载地址</p>
	 * 
	 * @return 文件下载地址
	 * 
	 * @throws DownloadException 下载异常
	 */
	public String nextFileLink() throws DownloadException {
		if(this.index.get() >= this.links.size()) {
			throw new DownloadException("没有更多数据");
		}
		synchronized (this.index) {
			return this.links.get(this.index.getAndIncrement());
		}
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

	@Override
	public String toString() {
		return ObjectUtils.toString(this, this.type, this.links);
	}
	
}