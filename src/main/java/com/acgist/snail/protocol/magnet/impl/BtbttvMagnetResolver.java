package com.acgist.snail.protocol.magnet.impl;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.util.Map;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.protocol.magnet.MagnetResolver;

/**
 * http://www.btbttv.cc/torrent.html
 */
public class BtbttvMagnetResolver extends MagnetResolver {

	private BtbttvMagnetResolver() {
	}
	
	public static final BtbttvMagnetResolver newInstance() {
		return new BtbttvMagnetResolver();
	}
	
	@Override
	public String name() {
		return "btbttv";
	}
	
	@Override
	public Integer order() {
		return 0;
	}

	@Override
	public HttpRequest request() {
		Map<String, String> data = Map.of(
			"vid", "0",
			"route", "3",
			"hash", this.hash
		);
		Builder builder = HTTPClient.newFormRequest("http://www.btbttv.cc/torrent.html");
		return builder.POST(HTTPClient.formBodyPublisher(data)).build();
	}

}
