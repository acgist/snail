package com.acgist.snail.net.dht.bootstrap.response;

import com.acgist.snail.net.dht.bootstrap.Response;

/**
 * 广播下载地址：广播上面一级
 */
public class AnnouncePeerResponse extends Response {

	public AnnouncePeerResponse(Response response) {
		super(response.getT(), response.getY(), response.getR(), response.getE());
	}

	public static final AnnouncePeerResponse newInstance(Response response) {
		return new AnnouncePeerResponse(response);
	}

}
