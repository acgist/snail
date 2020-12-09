package com.acgist.snail.gui.javafx;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class ThemesTest extends Performance {

	@Test
	public void testTetThemeStyle() throws Exception {
		this.log(Themes.getThemeStyle());
	}

}
