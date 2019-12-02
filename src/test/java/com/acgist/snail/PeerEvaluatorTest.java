package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerEvaluator;
import com.acgist.snail.utils.NetUtils;

public class PeerEvaluatorTest extends BaseTest {

	@Test
	public void testLoad() {
		this.cost();
		PeerEvaluator.getInstance().init();
		var map = PeerEvaluator.getInstance().ranges();
		if(map != null) {
			map.entrySet().stream()
			.sorted((a, b) -> {
//				return a.getKey().compareTo(b.getKey()); // IP段
				return a.getValue().compareTo(b.getValue()); // 评分
			})
			.forEach(entry -> {
				this.log(String.format("%05d", entry.getKey()) + "=" + String.format("%05d", entry.getValue()) + "-" + NetUtils.decodeLongToIp(1L * (2 << 15) * entry.getKey()));
			});
			this.log("数量：" + map.size());
		} else {
			this.log("--");
		}
		this.costed();
	}
	
}
