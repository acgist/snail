package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Response;

public class AnnouncePeerResponse extends Response {

	public AnnouncePeerResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final AnnouncePeerResponse newInstance(byte[] bytes) {
		return new AnnouncePeerResponse(Response.valueOf(bytes));
	}

}
