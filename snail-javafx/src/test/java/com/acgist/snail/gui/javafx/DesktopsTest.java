package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

import javafx.application.Platform;
import javafx.stage.Screen;

class DesktopsTest extends Performance {

    @Test
    void testOpen() {
        assertDoesNotThrow(() -> Desktops.open(new File("D:/tmp")));
    }
    
    @Test
    void testBrowse() {
        assertDoesNotThrow(() -> Desktops.browse("https://www.acgist.com"));
    }
    
    @Test
    void testHidpi() {
        assertDoesNotThrow(() -> {
            Platform.startup(() -> {});
            this.log("横向缩放：{}", Screen.getPrimary().getOutputScaleX());
            this.log("纵向缩放：{}", Screen.getPrimary().getOutputScaleY());
        });
    }
    
}
