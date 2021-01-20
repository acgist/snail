package com.acgist.snail.gui.javafx.window;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.Snail;
import com.acgist.snail.gui.javafx.Themes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * <p>窗口</p>
 * 
 * @param <T> 控制器泛型
 * 
 * @author acgist
 */
public abstract class Window<T extends Controller> extends Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
	
	/**
	 * <p>容器</p>
	 */
	protected Stage stage;
	/**
	 * <p>控制器</p>
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
	 * <p>设置ESC隐藏窗口</p>
	 */
	protected void esc() {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode() == KeyCode.ESCAPE) {
				this.stage.hide();
			}
		});
	}
	
	/**
	 * <p>设置Icon</p>
	 */
	protected void icon() {
		this.stage.getIcons().add(new Image(Themes.LOGO_ICON_200));
	}
	
	/**
	 * <p>设置窗口最大化</p>
	 * <p>窗口最小化隐藏到托盘后，需要设置此项才能正常显示窗口。</p>
	 */
	protected void maximize() {
		this.stage.setIconified(false);
	}
	
	/**
	 * <p>禁止改变窗口大小</p>
	 */
	protected void disableResize() {
		this.stage.setResizable(false);
	}
	
	/**
	 * <p>对话框通用设置</p>
	 * 
	 * @see #esc()
	 * @see #icon()
	 * @see #disableResize()
	 */
	protected void dialogWindow() {
		this.esc();
		this.icon();
		this.disableResize();
	}
	
	/**
	 * <p>加载fxml、controller</p>
	 * 
	 * @param <X> 面板泛型
	 * 
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
	 * <p>创建窗口</p>
	 * 
	 * @param stage 容器
	 * @param title 标题
	 * @param width 宽度
	 * @param height 高度
	 * @param fxml fxml路径
	 * @param modality 模态
	 * 
	 * @throws IOException IO异常
	 */
	protected void buildWindow(Stage stage, String title, int width, int height, String fxml, Modality modality) throws IOException {
		final Parent root = this.loadFxml(fxml);
		final Scene scene = new Scene(root, width, height);
		root.setStyle(Themes.getThemeStyle());
		stage.initModality(modality);
		stage.setScene(scene);
		stage.setTitle(title);
	}
	
	/**
	 * <p>显示窗口（异步）</p>
	 */
	public void show() {
		if(Snail.available()) {
			this.stage.show();
		}
	}
	
	/**
	 * <p>显示窗口（同步）</p>
	 */
	public void showAndWait() {
		if(Snail.available()) {
			this.stage.showAndWait();
		}
	}
	
	/**
	 * <p>隐藏窗口</p>
	 */
	public void hide() {
		this.stage.hide();
	}
	
	/**
	 * <p>判断窗口是否显示</p>
	 * 
	 * @return 是否显示
	 */
	public boolean isShowing() {
		return
			// 显示
			this.stage.isShowing() &&
			// 图标显示
			!this.stage.isIconified();
	}
	
	/**
	 * <p>获取容器</p>
	 * 
	 * @return 容器
	 */
	public Stage stage() {
		return this.stage;
	}
	
	/**
	 * <p>获取控制器</p>
	 * 
	 * @return 控制器
	 */
	public T controller() {
		return this.controller;
	}
	
}
