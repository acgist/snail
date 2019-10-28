package com.acgist.snail.net.torrent.peer.bootstrap;

/**
 * <p>扩展协议类型接口</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IExtensionTypeGetter {

	/**
	 * 是否支持扩展协议
	 * 
	 * @return true-支持；false-不支持；
	 */
	boolean supportExtensionType();
	
	/**
	 * 获取扩展协议ID
	 * 
	 * @return 扩展协议ID
	 */
	Byte extensionType();
	
}
