package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class ThemesTest extends Performance {

	@Test
	public void testThemeStyle() throws Exception {
		this.log(Themes.getThemeStyle());
		assertNotNull(Themes.getThemeStyle());
	}

}
