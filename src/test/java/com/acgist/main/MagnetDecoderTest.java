package com.acgist.main;

import org.junit.Test;

import com.acgist.snail.protocol.magnet.MagnetDecoder;
import com.acgist.snail.system.exception.DownloadException;

public class MagnetDecoderTest {

	@Test
	public void download() throws DownloadException {
		MagnetDecoder.download("DE8BAC698D85AD5D27E3F92863A255D0E2B09E2E");
	}
	
}
