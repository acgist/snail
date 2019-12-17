package com.acgist.snail;

import java.util.BitSet;
import java.util.Random;

import com.acgist.snail.gui.statistics.CanvasPainter;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class BitmapPainterTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		final int size = 990;
		BitSet bitSet = new BitSet();
//		bitSet.set(0);
//		bitSet.set(49);
//		bitSet.set(50);
		Random random = new Random();
		for (int i = 0; i < size; i++) {
			bitSet.set(random.nextInt(size));
		}
		CanvasPainter painter = CanvasPainter.newInstance(12, 50, size, bitSet);
//		CanvasPainter painter = CanvasPainter.newInstance(16, 50, 990, bitSet, Color.BLACK, Color.BLACK, Color.WHITE);
		primaryStage.setTitle("画布");
		Group root = new Group();
		root.getChildren().add(painter.draw().canvas());
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
