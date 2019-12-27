package com.acgist.snail;

import java.util.BitSet;
import java.util.Random;

import com.acgist.snail.gui.statistics.CanvasPainter;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.ThreadUtils;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
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
		CanvasPainter painter = CanvasPainter.newInstance(12, 50, size, bitSet, selectBitSet);
//		CanvasPainter painter = CanvasPainter.newInstance(16, 50, 990, bitSet, Color.BLACK, Color.BLACK, Color.WHITE);
		primaryStage.setTitle("画布");
		Group root = new Group();
		root.getChildren().add(painter.build().draw().canvas());
		SystemThreadContext.submit(() -> {
			while(true) {
				int index = random.nextInt(size);
				if(begin <= index && index <= end) {
					painter.draw(index);
				}
				ThreadUtils.sleep(100);
			}
		});
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
