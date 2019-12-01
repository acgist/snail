package com.acgist.snail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.system.JSON;

public class JSONTest extends BaseTest {
	
	@Test
	public void deserialize() {
		JSON json = JSON.ofString("{\"name\":\"你,:{}好\",\"age\":30,\"marry\" :true , \"wife\": null , \"like\":[\"动漫\",\"洞箫\"]}");
		this.log(json.getString("name"));
		this.log(json.getInteger("age"));
		this.log(json.getBoolean("marry"));
		this.log(json.get("wife"));
		this.log(json.getJSON("like").getList());
		this.log(json.toJSON());
	}
	
	@Test
	public void serialize() {
		Map<Object, Object> map = Map.of(
			"name", "\b你,:{}好",
			"age", 30,
			"marry", true,
			"json", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":[\"动漫\",\"洞箫\"]}",
//			"json", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":\"[\"动漫\",\"洞箫\"]\"}",
			"like", List.of("动漫", "洞箫")
		);
		map = new HashMap<>(map);
		map.put("wife", null);
		JSON json = JSON.ofMap(map);
		this.log(json.toJSON());
		this.log(json.getJSON("json"));
		this.log(json.getJSON("json").get("like"));
		this.log(json.getJSON("json").getJSON("like"));
	}

	@Test
	public void cos() {
		long begin = System.currentTimeMillis();
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
		this.log(System.currentTimeMillis() - begin);
	}
	
}
