package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.MagnetResolverManager;

public class MagnetTest {

	@Test
	public void download() throws DownloadException {
		MagnetResolverManager.getInstance().download("DE8BAC698D85AD5D27E3F92863A255D0E2B09E2E");
	}
	
}
