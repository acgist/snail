package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.protocol.magnet.impl.BtbttvMagnetResolver;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.MagnetResolverManager;

public class MagnetResolverTest {

	@Test
	public void btbttv() throws DownloadException {
		var manager = MagnetResolverManager.getInstance();
		manager.register(BtbttvMagnetResolver.newInstance());
		manager.download("82309348090ecbec8bf509b83b30b78a8d1f6454");
	}
	
}
