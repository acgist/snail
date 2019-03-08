package com.acgist.snail.window.alert;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 文件验证结果
 */
public class VerifyWindow {

	public static final void build() {
		FlowPane pane = new FlowPane();
		pane.setBackground(Background.EMPTY);
		Scene trayScene = new Scene(pane);
		Stage trayStage = new Stage();
		trayStage.initStyle(StageStyle.DECORATED);
		trayStage.setOpacity(1);
		trayStage.setAlwaysOnTop(true);
		trayStage.setScene(trayScene);
		TextArea f = new TextArea();
		f.setEditable(false);
		f.setText("x\r\nx\r\nx\r\nx\r\nx\r\nxxxxxxxxxxxxxxxxxxxxxxxx\r\\r\nnx\r\nx\r\nx\r\nx\r\nx\r\nxxxxxxxxxxxxxxxxxxx");
		f.setStyle("-fx-background-color:transparent;");
		pane.getChildren().add(f);
		trayStage.show();
	}
	
}
