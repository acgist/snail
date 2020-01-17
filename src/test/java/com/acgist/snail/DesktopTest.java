package com.acgist.snail;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.utils.DesktopUtils;

public class DesktopTest extends BaseTest {

	@Test
	public void test() throws IOException {
		this.log("是否支持：" + Desktop.isDesktopSupported());
		var desktop = Desktop.getDesktop();
		desktop.open(new File("e://qrcodes.txt"));
//		desktop.browseFileDirectory(new File("e://"));
	}

	@Test
	public void testDesktopUtils() {
		DesktopUtils.open(new File("e://qrcodes.txt"));
	}
	
}
