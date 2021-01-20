package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class DesktopsTest extends Performance {

	@Test
	public void testOpen() {
		assertDoesNotThrow(() -> Desktops.open(new File("E://snail")));
	}
	
	@Test
	public void testBrowse() {
		assertDoesNotThrow(() -> Desktops.browse("https://www.acgist.com"));
	}
	
}
