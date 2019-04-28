package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Response;

public class PingResponse extends Response {

	public PingResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final PingResponse newInstance(byte[] bytes) {
		return new PingResponse(Response.valueOf(bytes));
	}

}
