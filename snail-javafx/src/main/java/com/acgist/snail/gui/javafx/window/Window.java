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
 * 窗口超类
 * 
 * @param <T> 窗口控制器
 * 
 * @author acgist
 */
public abstract class Window<T extends Controller> extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
    
    /**
     * 按键任务
     * 
     * @author acgist
     */
    @FunctionalInterface
    public interface KeyReleasedFunction {
        
        /**
         * 执行按键任务
         */
        void execute();
        
    }
    
    /**
     * 容器
     */
    private final Stage stage;
    /**
     * 场景
     */
    private final Scene scene;
    /**
     * 面板
     */
    private final Parent root;
    /**
     * 窗口控制器
     */
    protected final T controller;
    
    /**
     * @param title    窗口标题
     * @param width    窗口宽度
     * @param height   窗口高度
     * @param modality 模态
     * @param fxml     窗口FXML路径
     */
    protected Window(String title, int width, int height, Modality modality, String fxml) {
        try {
            LOGGER.debug("新建窗口：{} - {}", title, fxml);
            // 加载面板
            final FXMLLoader loader = new FXMLLoader(this.getClass().getResource(fxml));
            this.root       = loader.load();
            this.controller = loader.getController();
            // 加载样式
            Themes.applyStyle(this.root);
            // 加载容器
            this.stage = new Stage();
            this.scene = new Scene(this.root, width, height);
            this.stage.setTitle(title);
            this.stage.setScene(this.scene);
            this.stage.initModality(modality);
            this.start(this.stage);
        } catch (Exception e) {
            throw new IllegalArgumentException(fxml, e);
        }
    }
    
    /**
     * 设置窗口置顶显示
     */
    protected void top() {
        this.stage.setIconified(false);
    }
    
    /**
     * 设置Icon
     */
    protected void icon() {
        Themes.applyLogo(this.stage.getIcons());
    }
    
    /**
     * 设置ESCAPE隐藏窗口
     */
    protected void escape() {
        this.keyReleased(KeyCode.ESCAPE, this::hide);
    }
    
    /**
     * 禁止改变窗口大小
     */
    protected void disableResize() {
        this.stage.setResizable(false);
    }
    
    /**
     * 对话框通用设置
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
     * 隐藏窗口释放资源
     */
    protected void hiddenRelease() {
        this.stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, event -> this.controller.release());
    }
    
    /**
     * 注册键盘事件
     * 
     * @param keyCode  按键编号
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
     * 显示窗口（异步）
     */
    public void show() {
        if(Snail.available()) {
            this.stage.show();
        }
    }
    
    /**
     * 显示窗口（同步）
     */
    public void showAndWait() {
        if(Snail.available()) {
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
     * @return 是否显示
     */
    public boolean isShowing() {
        // 显示同时不是图标显示
        return this.stage.isShowing() && !this.stage.isIconified();
    }

    /**
     * @return 容器
     */
    public Stage getStage() {
        return this.stage;
    }

    /**
     * @return 场景
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * @return 面板
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * @return 窗口控制器
     */
    public T getController() {
        return this.controller;
    }
    
}
