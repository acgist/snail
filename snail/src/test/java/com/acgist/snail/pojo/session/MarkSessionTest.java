package com.acgist.snail.pojo.session;

import org.junit.jupiter.api.Test;

import com.acgist.snail.BaseTest;
import com.acgist.snail.utils.ThreadUtils;

public class MarkSessionTest extends BaseTest {

	@Test
	public void test() {
		MarkSession markSession = new MarkSession();
		LOGGER.info("下载速度：{}", markSession.uploadMark());
		ThreadUtils.sleep(1000);
		LOGGER.info("下载速度：{}", markSession.uploadMark());
	}
	
}
