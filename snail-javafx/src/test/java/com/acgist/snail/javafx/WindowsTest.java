package com.acgist.snail.javafx;

import org.junit.Test;

import com.acgist.snail.gui.javafx.Themes;

public class WindowsTest extends BaseTest {

	@Test
	public void getTheme() {
//		JNA
//		<dependency>
//			<groupId>net.java.dev.jna</groupId>
//			<artifactId>jna-platform</artifactId>
//			<version>5.5.0</version>
//		</dependency>
//		long color = Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Windows\\DWM", "AccentColor");
////		long color = Advapi32Util.registryGetIntValue(
////			WinReg.HKEY_CURRENT_USER,
////			"Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\History\\Colors",
////			"ColorHistory0"
////		);
//		int a = (int) ((color >> 24) & 0xFF);
//		int b = (int) ((color >> 16) & 0xFF);
//		int c = (int) ((color >> 8) & 0xFF);
//		int d = (int) (color & 0xFF);
//		this.log(a);
//		this.log(b);
//		this.log(c);
//		this.log(d);
//		this.log(String.format("rgba(%d, %d, %d, %.2f)", d, c, b, a / 255D));
//		this.log(String.format("rgba(%d, %d, %d, 1.0)", d, c, b));
	}

	@Test
	public void getThemeCommand() throws Exception {
		this.log(Themes.getThemeStyle());
	}

}
