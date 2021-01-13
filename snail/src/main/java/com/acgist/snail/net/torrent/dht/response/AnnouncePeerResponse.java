package com.acgist.snail.net.torrent.dht.response;

import com.acgist.snail.net.torrent.dht.DhtRequest;
import com.acgist.snail.net.torrent.dht.DhtResponse;

/**
 * <p>声明Peer</p>
 * 
 * @author acgist
 */
public final class AnnouncePeerResponse extends DhtResponse {

	/**
	 * @param t 节点ID
	 */
	private AnnouncePeerResponse(byte[] t) {
		super(t);
	}
	
	/**
	 * @param response 响应
	 */
	private AnnouncePeerResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}
	
	/**
	 * <p>创建响应</p>
	 * 
	 * @param request 请求
	 * 
	 * @return 响应
	 */
	public static final AnnouncePeerResponse newInstance(DhtRequest request) {
		return new AnnouncePeerResponse(request.getT());
	}

	/**
	 * <p>创建响应</p>
	 * 
	 * @param response 响应
	 * 
	 * @return 响应
	 */
	public static final AnnouncePeerResponse newInstance(DhtResponse response) {
		return new AnnouncePeerResponse(response);
	}
	
}
