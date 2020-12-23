package com.acgist.snail.net.torrent.crypt;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.utils.Performance;

public class MSEPaddingSyncTest extends Performance {

	@Test
	public void testCost() throws NetException {
		this.cost();
		for (int index = 0; index < 100000; index++) {
			testSync();
		}
		this.costed();
	}
	
	@Test
	public void testSync() throws NetException {
		MSEPaddingSync sync = MSEPaddingSync.newInstance(3);
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.putShort((short) 2).put("12".getBytes());
//		buffer.putShort((short) 0);
		buffer.putShort((short) 100);
		buffer.put("UU34".getBytes());
		buffer.flip();
		
		boolean success = sync.sync(buffer);
		this.log(success);
		this.log(sync);
		
		ByteBuffer append = ByteBuffer.allocate(98);
		append.put("0".repeat(96).getBytes());
		append.putShort((short) 0);
		append.flip();
		
		success = sync.sync(append);
		this.log(success);
		this.log(sync);
		
//		ByteBuffer append = ByteBuffer.allocate(96);
//		append.put("0".repeat(96).getBytes());
//		append.flip();
//		
//		success = sync.sync(append);
//		this.log(success);
//		this.log(sync);
//		
//		ByteBuffer zero = ByteBuffer.allocate(2);
//		zero.putShort((short) 0);
//		zero.flip();
//		
//		success = sync.sync(zero);
//		this.log(success);
//		this.log(sync);
		
		sync.allPadding().forEach(bytes -> {
			this.log("内容：" + new String(bytes));
		});
	}
	
}
