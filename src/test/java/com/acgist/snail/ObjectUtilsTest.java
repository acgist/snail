package com.acgist.snail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.acgist.snail.utils.ObjectUtils;

public class ObjectUtilsTest {

	@Test
	public void toStringTest() {
		List<String> list = new ArrayList<>();
		list.add("1");
		list.add("2");
		System.out.println(ObjectUtils.toString(list));
	}
	
}
