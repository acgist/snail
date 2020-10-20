package com.acgist.snail.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;

public class JSONTest extends BaseTest {
	
	@Test
	public void testSerialize() {
		Map<Object, Object> map = Map.of(
			"name", "\b你,:{}\"好",
			"age", 30,
			"marry", true,
			"json-a", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":[\"动漫\",\"洞箫\"]}",
			"json-b", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":\"[\"动漫\",\"洞箫\"]\"}",
			"like", List.of("动\"漫", "洞\\箫")
		);
		map = new HashMap<>(map);
		map.put("wife", null);
		map.put("list", List.of(List.of(1, 2), 2, 3));
		JSON json = JSON.ofMap(map);
		this.log(json.toJSON());
		assertEquals(json.get("name"), map.get("name"));
		this.log(json.get("name"));
		assertEquals(json.get("like"), map.get("like"));
		this.log(json.get("like"));
		assertEquals(json.get("age"), map.get("age"));
		this.log(json.get("age"));
		this.log(json.getJSON("like"));
		this.log(json.getJSON("like").getList());
//		JSON.eager(); // 禁用懒加载
		this.log("json-a");
		this.log(json.getJSON("json-a"));
		this.log(json.getJSON("json-a").get("like"));
		this.log(json.getJSON("json-a").get("like").getClass());
		this.log(json.getJSON("json-a").getJSON("like"));
		this.log("json-b");
		this.log(json.getJSON("json-b"));
		this.log(json.getJSON("json-b").get("like"));
		this.log(json.getJSON("json-b").get("like").getClass());
		this.log(json.getJSON("json-b").getJSON("like"));
	}
	
	@Test
	public void testDeserialize() {
		JSON json = JSON.ofString("{\"like\":[\"动\\\"漫\",\"洞\\\\箫\"],\"json-a\":\"{\\\"name\\\":\\\"你,:{}好\\\",\\\"age\\\":30,\\\"marry\\\":true,\\\"wife\\\":null,\\\"like\\\":[\\\"动漫\\\",\\\"洞箫\\\"]}\",\"wife\":null,\"json-b\":\"{\\\"name\\\":\\\"你,:{}好\\\",\\\"age\\\":30,\\\"marry\\\":true,\\\"wife\\\":null,\\\"like\\\":\\\"[\\\"动漫\\\",\\\"洞箫\\\"]\\\"}\",\"marry\":true,\"name\":\"\\b你,:{}\\\"好\",\"list\":[[1,2],2,3],\"age\":30}");
		this.log(json.getString("name"));
		this.log(json.getInteger("age"));
		this.log(json.getBoolean("marry"));
		this.log(json.get("wife"));
		this.log(json.get("like"));
		this.log(json.getJSON("like"));
		this.log(json.getJSON("like").getList());
		this.log(json.get("list"));
		this.log(json.getJSON("list"));
		this.log(json.getJSON("list").getList());
		this.log(json.toJSON());
	}

	@Test
	public void testCost() {
		this.cost();
		for (int i = 0; i < 100000; i++) {
			// 反序列化
//			JSON.ofString("{\"name\":\"你,:{}好\",\"age\":30,\"marry\" :true , \"wife\": null , \"like\":[\"动漫\",\"洞箫\"]}");
			// 序列化
			Map<Object, Object> map = Map.of(
				"name", "你,:{}好",
				"age", 30,
				"marry", true,
				"like", List.of("动漫", "洞箫")
			);
			map = new HashMap<>(map);
			map.put("wife", null);
			JSON.ofMap(map);
		}
		this.costed();
	}
	
}
