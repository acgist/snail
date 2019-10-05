package com.acgist.snail.net.torrent.peer.bootstrap;

/**
 * <p>扩展协议类型接口</p>
 * 
 * @author acgist
 * @since 1.1.1
 */
public interface IExtensionTypeGetter {

	/**
	 * 获取扩展协议类型
	 * 
	 * @return 扩展协议类型
	 */
	Byte extensionType();
	
}
