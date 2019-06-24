package com.acgist.snail.magnet;

import org.junit.Test;

import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

public class MagnetProtocolTest {

	@Test
	public void test() throws DownloadException {
		MagnetProtocol.getInstance().init("magnet:?xt=urn:btih:fa493c8add6d907a0575631831033dcf94ba5217&tr=http://opentracker.acgnx.se/announce").buildTaskSession();
		ThreadUtils.sleep(Long.MAX_VALUE);
	}
	
}
