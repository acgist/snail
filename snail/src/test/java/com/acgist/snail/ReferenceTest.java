package com.acgist.snail;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ReferenceTest extends BaseTest {

	@Test
	public void testReference() {
		final List<Object> list = new ArrayList<>();
		// 注意这里int类型存在缓存
		for (int i = 126; i < 140; i++) {
			// 强引用
//			list.add(i);
			// 软引用
			list.add(new SoftReference<>(i));
			// 弱引用
//			list.add(new WeakReference<>(i));
			// 虚引用
//			list.add(new PhantomReference<T>(referent, q)); // ReferenceQueue
		}
		this.log(list.size());
		System.gc();
		this.log(list.size());
		list.forEach(i -> {
			if(i instanceof Reference) {
				this.log("r=" + ((Reference<?>) i).get());
			} else {
				this.log("i=" + i);
			}
		});
	}
	
}
