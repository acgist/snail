package com.acgist.snail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerEvaluator;
import com.acgist.snail.repository.DatabaseManager;
import com.acgist.snail.repository.impl.ConfigRepository;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.utils.NetUtils;

public class PeerEvaluatorTest extends BaseTest {

	@Test
	public void load() {
		PeerEvaluator.getInstance().init();
		long begin = System.currentTimeMillis();
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
		long end = System.currentTimeMillis();
		this.log(end - begin);
	}
	
	@Test
	public void merge() {
		ConfigRepository repository = new ConfigRepository();
		Map<Integer, Long> map = new HashMap<>();
		for (int index = 0; index < 100; index++) {
			map.put(index, (long) index);
		}
		repository.merge("acgist.system.range", new String(BEncodeEncoder.encodeMap(map)));
	}
	
	@Test
	public void delete() {
		DatabaseManager.getInstance();
		ConfigRepository repository = new ConfigRepository();
		boolean delete = repository.deleteName("acgist.system.range");
		this.log("删除结果：" + delete);
	}
	
}
