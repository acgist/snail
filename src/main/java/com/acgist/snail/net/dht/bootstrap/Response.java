package com.acgist.snail.net.dht.bootstrap;

import java.util.List;
import java.util.Map;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.utils.CollectionUtils;

public class Response {

	private final String t;
	private final String y;
	private final Map<?, ?> r;
	private final List<?> e;

	protected Response(String t, String y, Map<?, ?> r, List<?> e) {
		this.t = t;
		this.y = y;
		this.r = r;
		this.e = e;
	}

	public static final Response valueOf(byte[] bytes) {
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		decoder.mustMap();
		final String t = decoder.getString(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final Map<?, ?> r = decoder.getMap(DhtConfig.KEY_R);
		final List<?> e = decoder.getList(DhtConfig.KEY_E);
		return new Response(t, y, r, e);
	}
	
	public String getT() {
		return t;
	}

	public String getY() {
		return y;
	}

	public Map<?, ?> getR() {
		return r;
	}

	public List<?> getE() {
		return e;
	}
	
	public Object get(String key) {
		return this.r.get(key);
	}
	
	public String getString(String key) {
		return (String) this.get(key);
	}
	
	public String getId() {
		return getString(DhtConfig.KEY_ID);
	}
	
	/**
	 * 是否成功
	 */
	public boolean success() {
		return CollectionUtils.isEmpty(this.e);
	}

	/**
	 * 失败编码
	 */
	public int errorCode() {
		return (int) this.e.get(0);
	}

	/**
	 * 失败描述
	 */
	public String errorMessage() {
		return (String) this.e.get(1);
	}

}
