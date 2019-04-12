package com.acgist.snail.protocol.magnet.impl;

import java.util.Map;

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
	public String requestUrl() {
		return "http://www.btbttv.cc/torrent.html";
	}

	@Override
	public Map<String, String> formData() {
		return Map.of(
			"vid", "0",
			"route", "3",
			"hash", this.hash
		);
	}

}
