package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class ThemesTest extends Performance {

    @Test
    void testThemeStyle() throws Exception {
        assertNotNull(Themes.getStyle());
    }

}
