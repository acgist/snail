package com.acgist.snail.net.torrent.codec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.net.NetException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.Performance;

class MSECipherTest extends Performance {

	@Test
	void testMSECipher() throws NetException {
		final byte[] secret = ArrayUtils.random(16);
		final InfoHash infoHash = InfoHash.newInstance(ArrayUtils.random(20));
		final var sender = MSECipher.newSender(secret, infoHash);
		final var recver = MSECipher.newRecver(secret, infoHash);
		final byte[] data = ArrayUtils.random(20);
		final byte[] senderEncryptData = sender.encrypt(data);
		final byte[] recverDecryptData = recver.decrypt(senderEncryptData);
		this.log(data);
		this.log(senderEncryptData);
		this.log(recverDecryptData);
		assertArrayEquals(data, recverDecryptData);
		final byte[] recverEncryptData = recver.encrypt(data);
		final byte[] senderDecryptData = sender.decrypt(recverEncryptData);
		this.log(recverEncryptData);
		this.log(senderDecryptData);
		assertArrayEquals(data, senderDecryptData);
	}
	
	@Test
	void testMSECipherEquals() throws NetException {
		final byte[] secret = ArrayUtils.random(16);
		final InfoHash infoHash = InfoHash.newInstance(ArrayUtils.random(20));
		final var sender = MSECipher.newSender(secret, infoHash);
		final var recver = MSECipher.newRecver(secret, infoHash);
		final byte[] data = ArrayUtils.random(20);
		assertArrayEquals(sender.decrypt(data), recver.encrypt(data));
		assertArrayEquals(sender.encrypt(data), recver.decrypt(data));
	}

	@Test
	void testCosted() throws NetException {
		final byte[] secret = ArrayUtils.random(16);
		final InfoHash infoHash = InfoHash.newInstance(ArrayUtils.random(20));
		final var sender = MSECipher.newSender(secret, infoHash);
		final var recver = MSECipher.newRecver(secret, infoHash);
		final byte[] data = ArrayUtils.random(20);
		final long costed = this.costed(100000, () -> {
			try {
				final byte[] senderEncryptData = sender.encrypt(data);
				final byte[] recverDecryptData = recver.decrypt(senderEncryptData);
				if(recverDecryptData == null) {
					LOGGER.warn("解密失败");
				}
			} catch (NetException e) {
				LOGGER.error("数据加解密异常", e);
			}
		});
		assertTrue(costed < 1000);
	}
	
}
