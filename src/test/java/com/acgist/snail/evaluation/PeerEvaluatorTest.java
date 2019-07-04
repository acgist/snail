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
		long a = System.currentTimeMillis();
//		for (int index = 0; index < 1; index++) {
//			repository.save(new RangeEntity());
//		}
		long b = System.currentTimeMillis();
		System.out.println(b - a);
		repository.findAll().forEach(entity -> {
//			System.out.println(entity);
		});
		long c = System.currentTimeMillis();
		System.out.println(c - b);
	}
	
}
