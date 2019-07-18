package com.acgist.snail.gui;

import com.acgist.snail.system.context.SystemContext;

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
	 * 设置ICON
	 */
	protected void icon() {
		this.stage.getIcons().add(new Image("/image/logo.png"));
	}
	
	/**
	 * 设置通用信息：{@link #icon}、{@link #esc}
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
	 * 设置窗口最大化：防止最小化后从托盘显示出来不能正常显示
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
