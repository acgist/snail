package com.acgist.snail.pojo.bean;

import java.util.List;

/**
 * <p>M3U8信息</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class M3u8 {

	/**
	 * <p>获取文件格式</p>
	 * 
	 * TODO：CODECS
	 * 
	 * @return 文件格式
	 */
	public String format() {
		return null;
	}
	
	/**
	 * <p>判断是否是文件列表</p>
	 * 
	 * TODO：EXT-X-STREAM-INF
	 * 
	 * @return true-文件列表；false-M3U8列表
	 */
	public boolean isFileList() {
		return false;
	}

	/**
	 * <p>判断是否是直播流</p>
	 * 
	 * TODO：EXT-X-ENDLIST
	 * 
	 * @return true-直播流；false-视频流；
	 */
	public boolean isStreaming() {
		return false;
	}

	/**
	 * <p>获取文件列表</p>
	 * 
	 * @return 文件列表
	 */
	public List<String> fileList() {
		return null;
	}

	/**
	 * <p>获取下一个文件下载地址</p>
	 * 
	 * @return 文件下载地址
	 */
	public String nextFileUrl() {
		return null;
	}
	
}