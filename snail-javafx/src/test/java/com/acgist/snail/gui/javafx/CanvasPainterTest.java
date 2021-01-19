package com.acgist.snail.gui.javafx;

import java.util.BitSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.gui.javafx.window.statistics.CanvasPainter;
import com.acgist.snail.utils.ThreadUtils;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CanvasPainterTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		final int size = 990;
		final int begin = 250;
		final int end = 520;
		BitSet bitSet = new BitSet();
		BitSet selectBitSet = new BitSet();
//		bitSet.set(0);
//		bitSet.set(49);
//		bitSet.set(50);
		Random random = new Random();
		for (int i = begin; i < end; i++) {
			int index = random.nextInt(size);
			if(begin <= index && index <= end) {
				bitSet.set(index);
			}
			selectBitSet.set(i);
		}
		CanvasPainter painter = CanvasPainter.newInstance(12, 50, size, new BitSet[] { bitSet, selectBitSet }, new Color[] { Color.rgb(0x22, 0xAA, 0x22), Color.rgb(0xFF, 0xEE, 0x99) });
		primaryStage.setTitle("画布");
		Group root = new Group();
		root.getChildren().add(painter.build().draw().canvas());
		SystemThreadContext.submit(() -> {
			while(true) {
				int index = random.nextInt(size);
				if(begin <= index && index <= end) {
					painter.draw(0, index);
				}
				ThreadUtils.sleep(100);
			}
		});
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
	
	@Test
	public void test() {
		launch();
	}
	
}
