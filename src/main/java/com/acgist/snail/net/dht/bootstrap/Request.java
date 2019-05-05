package com.acgist.snail.net.dht.bootstrap;

import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.DhtConfig.QType;
import com.acgist.snail.utils.StringUtils;

public class Request {

	private final byte[] t;
	private final String y;
	private final DhtConfig.QType q;
	private final Map<String, Object> a;
	
	private Response response;
	private SocketAddress address;
	
	protected Request(byte[] t, DhtConfig.QType q) {
		this(t, DhtConfig.KEY_Q, q, new LinkedHashMap<>());
	}
	
	protected Request(byte[] t, String y, DhtConfig.QType q, Map<String, Object> a) {
		this.t = t;
		this.y = y;
		this.q = q;
		this.a = a;
	}

	public static final Request valueOf(byte[] bytes) {
		final BCodeDecoder decoder = BCodeDecoder.newInstance(bytes);
		decoder.mustMap();
		return valueOf(decoder);
	}
	
	public static final Request valueOf(final BCodeDecoder decoder) {
		final byte[] t = decoder.getBytes(DhtConfig.KEY_T);
		final String y = decoder.getString(DhtConfig.KEY_Y);
		final String q = decoder.getString(DhtConfig.KEY_Q);
		final QType type = DhtConfig.QType.valueOf(q);
		final Map<String, Object> a = decoder.getMap(DhtConfig.KEY_A);
		return new Request(t, y, type, a);
	}
	
	public byte[] getT() {
		return t;
	}

	public String getY() {
		return y;
	}

	public QType getQ() {
		return q;
	}

	public Map<?, ?> getA() {
		return a;
	}
	
	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public SocketAddress getAddress() {
		return address;
	}

	public void setAddress(SocketAddress address) {
		this.address = address;
	}

	/**
	 * 是否已经响应
	 */
	public boolean response() {
		return this.response != null;
	}
	
	public void put(String key, Object value) {
		this.a.put(key, value);
	}

	public Object get(String key) {
		if(this.a == null) {
			return null;
		}
		return this.a.get(key);
	}
	
	public Integer getInteger(String key) {
		final Long value = getLong(key);
		if(value == null) {
			return null;
		}
		return value.intValue();
	}
	
	public Long getLong(String key) {
		return (Long) this.get(key);
	}
	
	public byte[] getBytes(String key) {
		return (byte[]) this.get(key);
	}
	
	public String getString(String key) {
		final byte[] bytes = getBytes(key);
		if(bytes == null) {
			return null;
		}
		return new String(bytes);
	}
	
	public String getIdHex() {
		return StringUtils.hex(getT());
	}
	
	public byte[] getNodeId() {
		return getBytes(DhtConfig.KEY_ID);
	}
	
	public byte[] toBytes() {
		final Map<String, Object> request = new LinkedHashMap<>();
		request.put(DhtConfig.KEY_T, this.t);
		request.put(DhtConfig.KEY_Y, this.y);
		request.put(DhtConfig.KEY_Q, this.q.name());
		request.put(DhtConfig.KEY_A, this.a);
		return BCodeEncoder.mapToBytes(request);
	}
	
}
