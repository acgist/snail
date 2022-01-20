package com.acgist.snail.gui.javafx.window;

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
	 * <p>容器</p>
	 */
	private final Stage stage;
	/**
	 * <p>场景</p>
	 */
	private final Scene scene;
	/**
	 * <p>面板</p>
	 */
	private final Parent root;
	/**
	 * <p>控制器</p>
	 */
	protected final T controller;
	
	/**
	 * @param title 窗口标题
	 * @param width 窗口宽度
	 * @param height 窗口高度
	 * @param modality 模态
	 * @param fxml 窗口FXML路径
	 */
	protected Window(String title, int width, int height, Modality modality, String fxml) {
		try {
			LOGGER.debug("初始化窗口：{}-{}", title, fxml);
			// 加载面板
			final FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fxml));
			this.root = loader.load();
			this.controller = loader.getController();
			// 加载样式
			Themes.applyStyle(this.root);
			// 加载容器
			this.stage = new Stage();
			this.stage.setTitle(title);
			this.scene = new Scene(this.root, width, height);
			this.stage.setScene(this.scene);
			this.stage.initModality(modality);
			this.start(this.stage);
		} catch (Exception e) {
			throw new IllegalArgumentException(fxml, e);
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
