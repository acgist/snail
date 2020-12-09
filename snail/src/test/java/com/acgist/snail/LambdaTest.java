package com.acgist.snail;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.HttpTrackerClient;
import com.acgist.snail.utils.Performance;

public class LambdaTest extends Performance {

	@Test
	public void testMethodReference() throws NetException {
		TrackerClient client = HttpTrackerClient.newInstance("https://www.acgist.com");
		this.cost();
		this.costed();
		IntStream.range(0, 100000000).mapToObj(value -> client).filter(value -> value.available()).count();
		this.costed();
		IntStream.range(0, 100000000).mapToObj(value -> client).filter(TrackerClient::available).count();
		this.costed();
	}

	@Test
	public void testNull() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(1, null);
		map.put(2, null);
		var value = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 1)
			.findFirst()
			.orElse(null);
		this.log(value);
		value = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 1)
			.findFirst()
			.orElse(null);
		this.log(value);
		var valueMap = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 1)
			.map(Entry::getValue)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		this.log(valueMap);
		valueMap = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 1)
			.map(Entry::getValue)
			.findFirst() // 空指针
			.orElse("没有");
		this.log(valueMap);
	}
	
	@Test
	public void testNullEx() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		map.put(1, null);
		map.put(2, null);
		var value = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 3)
			.findFirst()
			.orElse(null);
		this.log(value);
		value = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 3)
			.findFirst()
			.orElse(null);
		this.log(value);
		var valueMap = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 3)
			.map(Entry::getValue)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		this.log(valueMap);
		valueMap = map.entrySet().stream()
			.filter(entry -> entry.getKey() == 3)
			.map(Entry::getValue)
			.findFirst()
			.orElse("没有");
		this.log(valueMap);
	}
	
}
