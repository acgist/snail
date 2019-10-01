package com.acgist.snail.gui;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Window<T extends Initializable> extends Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
	
	/**
	 * 容器
	 */
	protected Stage stage;
	/**
	 * 控制器
	 */
	protected T controller;
	
	protected Window() {
		this.stage = new Stage();
		try {
			this.start(this.stage);
		} catch (Exception e) {
			LOGGER.error("初始化窗口异常", e);
		}
	}
	
	/**
	 * 设置ICON
	 */
	protected void icon() {
		this.stage.getIcons().add(new Image("/image/logo.png"));
	}
	
	/**
	 * ESC隐藏窗口
	 */
	protected void esc() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, (event) -> {
			if(event.getCode() == KeyCode.ESCAPE) {
				this.stage.hide();
			}
		});
	}
	
	/**
	 * 设置会话框通用信息：{@link #icon}、{@link #esc}
	 */
	protected void dialogWindow() {
		icon();
		esc();
	}
	
	/**
	 * 禁止改变窗口大小
	 */
	protected void disableResize() {
		this.stage.setResizable(false);
	}
	
	/**
	 * 加载fxml、controller，返回面板。
	 * 
	 * @param <X> 面板泛型
	 * @param fxml fxml路径
	 * 
	 * @return 面板
	 * 
	 * @throws IOException IO异常
	 */
	protected <X> X loadFxml(String fxml) throws IOException {
		final FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fxml));
		final X x = loader.load();
		this.controller = loader.getController();
		return x;
	}
	
	/**
	 * 创建窗口
	 * 
	 * @param stage 容器
	 * @param title 标题
	 * @param width 宽度
	 * @param height 高度
	 * @param fxml fxml路径
	 * @param modality 模态
	 * 
	 * @throws IOException FXML加载异常
	 */
	protected void buildWindow(Stage stage, String title, int width, int height, String fxml, Modality modality) throws IOException {
		final Parent root = this.loadFxml(fxml);
		final Scene scene = new Scene(root, width, height);
		stage.initModality(modality);
		stage.setScene(scene);
		stage.setTitle(title);
	}
	
	/**
	 * 显示窗口（异步）
	 */
	public void show() {
		if(SystemContext.available()) {
			this.stage.show();
		}
	}
	
	/**
	 * 显示窗口（同步）
	 */
	public void showAndWait() {
		if(SystemContext.available()) {
			this.stage.showAndWait();
		}
	}
	
	/**
	 * 隐藏窗口
	 */
	public void hide() {
		this.stage.hide();
	}
	
	/**
	 * 设置窗口最大化：防止最小化后，、从托盘显示出来时不能正常显示。
	 */
	public void maximize() {
		this.stage.setIconified(false);
	}
	
	/**
	 * 窗口是否显示
	 */
	public boolean isShowing() {
		return this.stage.isShowing();
	}
	
	/**
	 * 容器
	 */
	public Stage stage() {
		return this.stage;
	}
	
	/**
	 * 控制器
	 */
	public T controller() {
		return this.controller;
	}
	
}
