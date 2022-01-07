package com.acgist.snail.gui.javafx.window;

import java.io.IOException;

import com.acgist.snail.Snail;
import com.acgist.snail.gui.javafx.Themes;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * <p>窗口</p>
 * 
 * @param <T> 控制器
 * 
 * @author acgist
 */
public abstract class Window<T extends Controller> extends Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
	
	/**
	 * <p>按键任务</p>
	 * 
	 * @author acgist
	 */
	@FunctionalInterface
	public interface KeyReleasedFunction {
		
		/**
		 * <p>执行按键任务</p>
		 */
		void execute();
		
	}
	
	/**
	 * <p>窗口标题</p>
	 */
	private final String title;
	/**
	 * <p>窗口宽度</p>
	 */
	private final int width;
	/**
	 * <p>窗口高度</p>
	 */
	private final int height;
	/**
	 * <p>窗口FXML路径</p>
	 */
	private final String fxml;
	/**
	 * <p>容器</p>
	 */
	protected Stage stage;
	/**
	 * <p>控制器</p>
	 */
	protected T controller;
	
	/**
	 * @param title 窗口标题
	 * @param width 窗口宽度
	 * @param height 窗口高度
	 * @param fxml 窗口FXML路径
	 */
	protected Window(String title, int width, int height, String fxml) {
		LOGGER.debug("初始化窗口：{}", title);
		this.title = title;
		this.width = width;
		this.height = height;
		this.fxml = fxml;
		this.stage = new Stage();
		try {
			this.start(this.stage);
		} catch (Exception e) {
			LOGGER.error("初始化窗口异常", e);
		}
	}
	
	/**
	 * <p>设置窗口置顶显示</p>
	 */
	protected void top() {
		this.stage.setIconified(false);
	}
	
	/**
	 * <p>设置Icon</p>
	 */
	protected void icon() {
		Themes.applyLogo(this.stage.getIcons());
	}
	
	/**
	 * <p>设置ESCAPE隐藏窗口</p>
	 */
	protected void escape() {
		this.keyReleased(KeyCode.ESCAPE, this::hide);
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
	 * @see #icon()
	 * @see #escape()
	 * @see #disableResize()
	 */
	protected void dialogWindow() {
		this.icon();
		this.escape();
		this.disableResize();
	}

	/**
	 * <p>隐藏窗口释放资源</p>
	 */
	protected void hiddenRelease() {
		this.stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, event -> this.controller.release());
	}
	
	/**
	 * <p>注册键盘事件</p>
	 * 
	 * @param keyCode 按键编号
	 * @param function 按键任务
	 */
	protected void keyReleased(KeyCode keyCode, KeyReleasedFunction function) {
		this.stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
			if(event.getCode() == keyCode) {
				function.execute();
			}
		});
	}
	
	/**
	 * <p>加载fxml、controller</p>
	 * 
	 * @param <X> 面板
	 * 
	 * @return 面板
	 * 
	 * @throws IOException IO异常
	 */
	protected <X> X loadFxml() throws IOException {
		final FXMLLoader loader = new FXMLLoader(this.getClass().getResource(this.fxml));
		final X root = loader.load();
		this.controller = loader.getController();
		return root;
	}
	
	/**
	 * <p>新建窗口</p>
	 * 
	 * @param stage 容器
	 * @param modality 模态
	 * 
	 * @throws IOException IO异常
	 */
	protected void buildWindow(Stage stage, Modality modality) throws IOException {
		final Parent root = this.loadFxml();
		final Scene scene = new Scene(root, this.width, this.height);
		Themes.applyStyle(root);
		stage.initModality(modality);
		stage.setScene(scene);
		stage.setTitle(this.title);
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
