package com.acgist.snail.evaluation;

import org.junit.Test;

import com.acgist.snail.repository.impl.RangeRepository;
import com.acgist.snail.utils.NetUtils;

public class PeerEvaluatorTest {

	@Test
	public void match() {
		long begin = System.currentTimeMillis();
		for (int index = 0; index < 100000; index++) {
			NetUtils.encodeIpToInt("127.0.0.1");
			for (int jndex = 0; jndex < 65536; jndex++) {
				if(index < jndex) {
				}
			}
		}
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
	}
	
	@Test
	public void save() {
		RangeRepository repository = new RangeRepository();
//		repository.findAll().forEach(entity -> {
//			repository.delete(entity.getId());
//		});
		long begin = System.currentTimeMillis();
//		for (int index = 0; index < 1; index++) {
//			repository.save(new RangeEntity());
//		}
		long end = System.currentTimeMillis();
		System.out.println(end - begin);
		var list = repository.findAll();
		if(list != null) {
			list.stream()
			.sorted((a, b) -> {
				return a.getScore().compareTo(b.getScore());
			})
			.forEach(entity -> {
				System.out.print(String.format("%05d", entity.getIndex()) + "=" + entity.getScore());
				System.out.print("-");
				System.out.println(NetUtils.decodeLongToIp(1L * (2 << 15) * entity.getIndex()));
			});
			System.out.println("数量：" + list.size());
		} else {
			System.out.println("--");
		}
		long last = System.currentTimeMillis();
		System.out.println(last - end);
	}
	
}
