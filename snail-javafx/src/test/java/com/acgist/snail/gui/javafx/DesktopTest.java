package com.acgist.snail.gui.javafx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DesktopTest extends Performance {

	@Test
	public void test() throws IOException {
		this.log("是否支持：" + Desktop.isDesktopSupported());
		var desktop = Desktop.getDesktop();
		desktop.open(new File("e://qrcodes.txt"));
//		desktop.browseFileDirectory(new File("e://"));
	}

	@Test
	public void testDesktopUtils() {
		Desktops.open(new File("e://qrcodes.txt"));
	}
	
}
