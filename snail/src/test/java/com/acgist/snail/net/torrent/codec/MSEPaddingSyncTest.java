package com.acgist.snail.net.torrent.codec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.utils.Performance;

class MSEPaddingSyncTest extends Performance {

	@Test
	void testToString() {
		final MSEPaddingSync sync = MSEPaddingSync.newInstance(10);
		this.log(sync.toString());
		assertNotNull(sync.toString());
	}
	
	@Test
	void testSync() throws PacketSizeException {
		final MSEPaddingSync sync = MSEPaddingSync.newInstance(3);
		final ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.putShort((short) 2).put("12".getBytes());
		buffer.putShort((short) 100);
		buffer.put("UU34".getBytes());
		buffer.flip();
		boolean success = sync.sync(buffer);
		assertFalse(success);
		final ByteBuffer append = ByteBuffer.allocate(98);
		append.put("0".repeat(96).getBytes());
		append.putShort((short) 0);
		append.flip();
		success = sync.sync(append);
		assertTrue(success);
//		sync.allPadding().forEach(bytes -> this.log("内容：" + new String(bytes)));
	}
	
	@Test
	void testCosted() throws NetException {
		this.costed(100000, () -> {
			try {
				this.testSync();
			} catch (PacketSizeException e) {
				LOGGER.error("数据校验异常", e);
			}
		});
	}
	
}
