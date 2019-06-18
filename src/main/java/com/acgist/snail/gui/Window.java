package com.acgist.snail.gui;

import javafx.application.Application;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * 抽象窗口
 * 
 * @author acgist
 * @since 1.0.0
 */
public abstract class Window<T extends Initializable> extends Application {

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
	 * 禁止改变窗口大小
	 */
	protected void disableResize() {
		this.stage.setResizable(false);
	}
	
	/**
	 * 设置通用信息：{@link #icon}、{@link #esc}
	 */
	protected void dialogWindow() {
		icon();
		esc();
	}
	
	/**
	 * 显示窗口（异步）
	 */
	public void show() {
		this.stage.show();
	}
	
	/**
	 * 显示窗口（同步）
	 */
	public void showAndWait() {
		this.stage.showAndWait();
	}
	
	/**
	 * 隐藏窗口
	 */
	public void hide() {
		this.stage.hide();
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
