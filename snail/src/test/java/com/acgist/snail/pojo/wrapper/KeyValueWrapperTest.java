package com.acgist.snail.pojo.wrapper;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class KeyValueWrapperTest extends Performance {

	@Test
	public void testEncode() {
		var wrapper = KeyValueWrapper.newInstance(Map.of("1", "2", "3", "4"));
		this.log(wrapper.encode());
		wrapper = KeyValueWrapper.newInstance(Map.of("a", "b", "3", "4"));
		this.log(wrapper.encode());
	}
	
	@Test
	public void testDecode() {
		var wrapper = KeyValueWrapper.newInstance("1=2&");
		this.log(wrapper.decode());
		wrapper = KeyValueWrapper.newInstance("&1=2&");
		this.log(wrapper.decode());
		wrapper = KeyValueWrapper.newInstance("1=2&3");
		this.log(wrapper.decode());
		wrapper = KeyValueWrapper.newInstance("1=2&3=");
		this.log(wrapper.decode());
		wrapper = KeyValueWrapper.newInstance("1=2&3=4");
		this.log(wrapper.decode());
		wrapper = KeyValueWrapper.newInstance("1=2 & 3=4");
		this.log(wrapper.decode());
		wrapper = KeyValueWrapper.newInstance("a=a&B=B&c=C");
		this.log(wrapper.decode());
		this.log(wrapper.get("a"));
		this.log(wrapper.get("B"));
		this.log(wrapper.getIgnoreCase("a"));
		this.log(wrapper.getIgnoreCase("C"));
		this.log(wrapper.getIgnoreCase("D"));
	}

	@Test
	public void testCost() {
		this.cost();
		var wrapper = KeyValueWrapper.newInstance("a=a&B=B&c=C");
		for (int i = 0; i < 1000000; i++) {
			wrapper.decode();
		}
		this.costed();
		for (int i = 0; i < 1000000; i++) {
			wrapper.get("a");
		}
		this.costed();
		for (int i = 0; i < 1000000; i++) {
			wrapper.getIgnoreCase("a");
		}
		this.costed();
	}
	
}
