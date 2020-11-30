package com.acgist.snail.pojo.wrapper;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class KeyValueWrapperTest extends BaseTest {

	@Test
	public void encode() {
		var wrapper = KeyValueWrapper.newInstance();
		this.log(wrapper.encode(Map.of("1", "2", "3", "4")));
		wrapper = KeyValueWrapper.newInstance(true, '-', '=');
		this.log(wrapper.encode(Map.of("a", "b", "3", "4")));
	}
	
	@Test
	public void decode() {
		var wrapper = KeyValueWrapper.newInstance();
		this.log(wrapper.decode("1=2&"));
		this.log(wrapper.decode("&1=2&"));
		this.log(wrapper.decode("1=2&3"));
		this.log(wrapper.decode("1=2&3="));
		this.log(wrapper.decode("1=2&3=4"));
		this.log(wrapper.decode("1=2 & 3=4"));
	}
	
}
