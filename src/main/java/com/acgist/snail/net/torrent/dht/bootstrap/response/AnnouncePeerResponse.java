package com.acgist.snail.net.torrent.dht.bootstrap.response;

import com.acgist.snail.net.torrent.dht.bootstrap.Request;
import com.acgist.snail.net.torrent.dht.bootstrap.Response;

/**
 * 声明Peer
 * 
 * @author acgist
 * @since 1.0.0
 */
public class AnnouncePeerResponse extends Response {

	private AnnouncePeerResponse(byte[] t) {
		super(t);
	}
	
	private AnnouncePeerResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final AnnouncePeerResponse newInstance(Response response) {
		return new AnnouncePeerResponse(response);
	}

	public static final AnnouncePeerResponse newInstance(Request request) {
		return new AnnouncePeerResponse(request.getT());
	}
	
}
