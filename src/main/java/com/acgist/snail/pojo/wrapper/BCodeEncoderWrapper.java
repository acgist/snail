package com.acgist.snail.pojo.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeDecoder.Type;
import com.acgist.snail.system.bcode.BCodeEncoder;

public class BCodeEncoderWrapper {
	
	private BCodeDecoder.Type type;
	
	private List<Object> list;
	private Map<String, Object> map;
	private final BCodeEncoder encoder;
	
	private BCodeEncoderWrapper() {
		this.encoder = BCodeEncoder.newInstance();
	}
	
	public static final BCodeEncoderWrapper newMapInstance() {
		final BCodeEncoderWrapper wrapper = new BCodeEncoderWrapper();
		wrapper.map = new HashMap<>();
		wrapper.type = Type.map;
		return wrapper;
	}
	
	public static final BCodeEncoderWrapper newListInstance() {
		final BCodeEncoderWrapper wrapper = new BCodeEncoderWrapper();
		wrapper.list = new ArrayList<>();
		wrapper.type = Type.list;
		return wrapper;
	}
	
	/**
	 * list添加数据
	 */
	public void put(Object value) {
		this.list.add(value);
	}

	/**
	 * map添加数据
	 */
	public void put(String key, String value) {
		this.map.put(key, value);
	}

	public byte[] bytes() {
		if(type == Type.list) {
			return encoder.build(this.list).bytes();
		} else if(type == Type.map) {
			return encoder.build(this.map).bytes();
		} else {
			return null;
		}
	}
	
	public String toString() {
		return new String(bytes());
	}
	
}
