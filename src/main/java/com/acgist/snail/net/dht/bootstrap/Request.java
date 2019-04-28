package com.acgist.snail.net.dht.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;

import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.QType;

public class Request {

	private final String t;
	private final String y;
	private final DhtConfig.QType q;
	private final Map<String, Object> a;
	
	protected Request(String t, DhtConfig.QType q) {
		this.t = t;
		this.y = DhtConfig.KEY_Q;
		this.q = q;
		this.a = new LinkedHashMap<>();
	}

	public String getT() {
		return t;
	}

	public String getY() {
		return y;
	}

	public QType getQ() {
		return q;
	}

	public Map<String, Object> getA() {
		return a;
	}
	
	public void put(String key, Object value) {
		this.a.put(key, value);
	}

	public Object get(String key) {
		return this.a.get(key);
	}
	
	public String getString(String key) {
		return (String) this.get(key);
	}
	
	public String getId() {
		return getString(DhtConfig.KEY_ID);
	}
	
	public byte[] toBytes() {
		final Map<String, Object> request = new LinkedHashMap<>();
		request.put(DhtConfig.KEY_T, this.t);
		request.put(DhtConfig.KEY_Y, DhtConfig.KEY_Q);
		request.put(DhtConfig.KEY_Q, this.q.name());
		request.put(DhtConfig.KEY_A, this.a);
		return BCodeEncoder.mapToBytes(request);
	}
	
}
