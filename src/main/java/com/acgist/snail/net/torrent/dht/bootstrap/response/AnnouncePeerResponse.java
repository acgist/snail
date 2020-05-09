package com.acgist.snail.net.torrent.dht.bootstrap.response;

import com.acgist.snail.net.torrent.dht.bootstrap.DhtRequest;
import com.acgist.snail.net.torrent.dht.bootstrap.DhtResponse;

/**
 * <p>声明Peer</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class AnnouncePeerResponse extends DhtResponse {

	private AnnouncePeerResponse(byte[] t) {
		super(t);
	}
	
	private AnnouncePeerResponse(DhtResponse response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final AnnouncePeerResponse newInstance(DhtResponse response) {
		return new AnnouncePeerResponse(response);
	}

	public static final AnnouncePeerResponse newInstance(DhtRequest request) {
		return new AnnouncePeerResponse(request.getT());
	}
	
}
