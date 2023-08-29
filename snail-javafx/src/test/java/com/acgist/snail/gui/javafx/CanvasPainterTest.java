package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.BitSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.gui.javafx.window.statistics.CanvasPainter;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.ThreadUtils;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class CanvasPainterTest extends Application {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CanvasPainterTest.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        final int size = 990;
        final int aPos = 250;
        final int zPos = 520;
        final Random random       = new Random();
        final BitSet bitSet       = new BitSet();
        final BitSet selectBitSet = new BitSet();
        for (int jndex = aPos; jndex < zPos; jndex++) {
            final int index = random.nextInt(size);
            if(aPos <= index && index <= zPos) {
                bitSet.set(index);
            }
            selectBitSet.set(jndex);
        }
        final Color[] colors = new Color[] {
            Color.rgb(0x22, 0xAA, 0x22),
            Color.rgb(0xFF, 0xEE, 0x99)
        };
        final CanvasPainter painter = CanvasPainter.newInstance(
            12, 50, size,
            new BitSet[] { bitSet, selectBitSet },
            colors,
            index -> LOGGER.info("点击：{}", index)
        );
        final HBox  hBox = new HBox();
        final Group root = new Group();
        root.getChildren().add(painter.build().draw().getCanvas());
        final String[] tabs = new String[] { "有效数据", "无效数据" };
        for (int index = 0; index < tabs.length; index++) {
            final Text  text  = new Text(tabs[index]);
            final Label label = new Label();
            label.setPrefSize(14, 14);
            label.setBackground(new Background(new BackgroundFill(colors[index], null, null)));
            final TextFlow textFlow = new TextFlow(label, text);
            hBox.getChildren().add(textFlow);
        }
        root.getChildren().add(hBox);
        SystemThreadContext.submit(() -> {
            while(true) {
                final int index = random.nextInt(size);
                if(aPos <= index && index <= zPos) {
                    bitSet.set(index);
                    painter.draw();
                }
                ThreadUtils.sleep(100);
            }
        });
        final Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("画布");
        primaryStage.show();
    }
    
    @Test
    void test() {
        assertDoesNotThrow(() -> launch());
    }
    
}
