package com.acgist.snail.gui.javafx;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DesktopsTest extends Performance {

	@Test
	public void testOpen() {
		Desktops.open(new File("E://snail"));
	}
	
}
