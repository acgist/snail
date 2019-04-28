package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Response;

public class FindNodeResponse extends Response {

	public FindNodeResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final FindNodeResponse newInstance(byte[] bytes) {
		return new FindNodeResponse(Response.valueOf(bytes));
	}

	public void getNodes() {
		// TODO：
	}
	
}
