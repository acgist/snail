package com.acgist.snail.coder.magnet.impl;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.util.Map;

import com.acgist.snail.coder.magnet.MagnetDecoder;
import com.acgist.snail.net.http.HttpManager;

/**
 * http://www.btbttv.cc/torrent.html
 */
public class BtbttvMagnetDecoder extends MagnetDecoder {

	private BtbttvMagnetDecoder() {
	}
	
	public static final BtbttvMagnetDecoder newInstance() {
		return new BtbttvMagnetDecoder();
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
		Builder builder = HttpManager.newFormRequest("http://www.btbttv.cc/torrent.html");
		return builder.POST(HttpManager.formBodyPublisher(data)).build();
	}

}
