package com.acgist.snail.pojo.session;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.utils.ThreadUtils;

public class PeerConnectMarkSessionTest extends BaseTest {

	@Test
	public void test() {
		PeerConnectMarkSession peerConnectMarkSession = new PeerConnectMarkSession();
		LOGGER.info("下载速度：{}", peerConnectMarkSession.uploadMark());
		ThreadUtils.sleep(1000);
		LOGGER.info("下载速度：{}", peerConnectMarkSession.uploadMark());
	}
	
}
