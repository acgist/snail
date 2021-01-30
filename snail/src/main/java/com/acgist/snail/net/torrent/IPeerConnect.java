package com.acgist.snail.net.torrent;

/**
 * <p>Peer连接接口</p>
 * 
 * @author acgist
 */
public interface IPeerConnect {

	/**
	 * <p>连接类型</p>
	 * 
	 * @author acgist
	 */
	public enum ConnectType {

		/**
		 * <p>UTP</p>
		 */
		UTP,
		/**
		 * <p>TCP</p>
		 */
		TCP;
		
	}
	
	/**
	 * <p>获取连接类型</p>
	 * 
	 * @return 连接类型
	 */
	ConnectType connectType();
	
}
