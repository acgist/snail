package com.acgist.snail;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.torrent.tracker.bootstrap.TrackerClient;
import com.acgist.snail.net.torrent.tracker.bootstrap.impl.HttpTrackerClient;

public class LambdaTest extends BaseTest {

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
	
}
