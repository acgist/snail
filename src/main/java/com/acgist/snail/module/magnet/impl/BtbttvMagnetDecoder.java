package com.acgist.snail.module.magnet.impl;

import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.util.Map;

import com.acgist.snail.module.magnet.MagnetDecoder;
import com.acgist.snail.utils.HttpUtils;

/**
 * http://www.btbttv.cc/torrent.html
 */
public class BtbttvMagnetDecoder extends MagnetDecoder {

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
		Builder builder = HttpUtils.newFormRequest("http://www.btbttv.cc/torrent.html");
		return builder.POST(HttpUtils.formBodyPublisher(data)).build();
	}

}
