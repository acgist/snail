package com.acgist.main;

import java.util.List;

import org.junit.Test;

import com.acgist.snail.utils.JsonUtils;

public class JSONUtilsTest {

	@Test
	public void map() {
		System.out.println(JsonUtils.toMap("{\"x\":\"x\"}"));
	}
	
	@Test
	public void list() {
		List<String> list = JsonUtils.toList("[\"x\",\"x\"]", String.class);
		System.out.println(list);
	}
	
}
