package com.acgist.main;

import java.util.List;

import org.junit.Test;

import com.acgist.snail.utils.JSONUtils;

public class JSONUtilsTest {

	@Test
	public void map() {
		System.out.println(JSONUtils.toMap("{\"x\":\"x\"}"));
	}
	
	@Test
	public void list() {
		List<String> list = JSONUtils.toList("[\"x\",\"x\"]", String.class);
		System.out.println(list);
	}
	
}
