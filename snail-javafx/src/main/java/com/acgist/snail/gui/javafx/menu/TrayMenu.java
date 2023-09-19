package com.acgist.snail.gui.javafx.menu;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.event.MouseInputAdapter;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.Fonts.SnailIcon;
import com.acgist.snail.gui.javafx.Themes;
import com.acgist.snail.gui.javafx.window.about.AboutWindow;
import com.acgist.snail.gui.javafx.window.main.MainWindow;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * 托盘菜单
 * 
 * @author acgist
 */
public final class TrayMenu extends Menu {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrayMenu.class);
    
    private static final TrayMenu INSTANCE;
    
    public static final TrayMenu getInstance() {
        return INSTANCE;
    }
    
    static {
        INSTANCE = new TrayMenu();
        // 必须设置此项：否者窗口关闭后将不能通过托盘显示
        Platform.setImplicitExit(false);
    }
    
    /**
     * 是否支持托盘
     */
    private final boolean support;
    /**
     * 托盘容器
     */
    private Stage trayStage;
    /**
     * 托盘
     */
    private TrayIcon trayIcon;

    private TrayMenu() {
        this.support = SystemTray.isSupported();
        if(this.support) {
            this.buildMenus();
            this.buildTray();
        }
    }
    
    @Override
    protected void buildMenus() {
        // 显示按钮
        this.buildMenuItem("显示", SnailIcon.AS_ENLARGE, this.showAction);
        // 隐藏按钮
        this.buildMenuItem("隐藏", SnailIcon.AS_SHRINK, this.hideAction);
        // 官网与源码按钮
        this.buildMenuItem("官网与源码", SnailIcon.AS_HOME2, this.sourceAction);
        // 问题与建议按钮
        this.buildMenuItem("问题与建议", SnailIcon.AS_ROCKET, this.supportAction);
        // 关于按钮
        this.buildMenuItem("关于", SnailIcon.AS_INFO, this.aboutAction);
        // 分割线
        this.buildSeparator();
        // 退出按钮
        this.buildMenuItem("退出", SnailIcon.AS_SWITCH, this.exitAction);
        // 设置窗口隐藏事件
        this.addEventFilter(WindowEvent.WINDOW_HIDDEN, this.windowHiddenAction);
    }
    
    /**
     * 添加系统托盘
     */
    private void buildTray() {
        // 托盘鼠标事件
        final MouseListener mouseListener = new MouseInputAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    // 左键：显示隐藏
                    if (MainWindow.getInstance().isShowing()) {
                        Platform.runLater(MainWindow.getInstance()::hide);
                    } else {
                        Platform.runLater(MainWindow.getInstance()::show);
                    }
                } else if(event.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                    // 右键：托盘菜单
                    Platform.runLater(() -> {
                        // HiDPI支持
                        final double scaleX = Screen.getPrimary().getOutputScaleX();
                        final double scaleY = Screen.getPrimary().getOutputScaleY();
                        // 窗口高度（首次获取是零）：减去图标高度
                        final double height = TrayMenu.this.getHeight() == 0 ? 200 : TrayMenu.this.getHeight() - 20;
                        final double x = event.getXOnScreen() / scaleX;
                        final double y = event.getYOnScreen() / scaleY - height;
                        TrayMenu.INSTANCE.show(INSTANCE.buildTrayStage(), x, y);
                    });
                }
            }
        };
        // 添加系统托盘
        try(final InputStream input = MainWindow.class.getResourceAsStream(Themes.LOGO_ICON_16)) {
            final BufferedImage image = ImageIO.read(input);
            this.trayIcon = new TrayIcon(image, SystemConfig.getName());
            this.trayIcon.addMouseListener(mouseListener);
            SystemTray.getSystemTray().add(this.trayIcon);
        } catch (IOException | AWTException e) {
            LOGGER.error("添加系统托盘异常", e);
        }
    }
    
    /**
     * 新建托盘菜单容器
     * 
     * @return 容器
     */
    private Stage buildTrayStage() {
        final FlowPane trayPane = new FlowPane();
        trayPane.setBackground(Background.EMPTY);
        Themes.applyClass(trayPane, Themes.CLASS_TRAY);
        final Scene trayScene = new Scene(trayPane);
        Themes.applyStyle(trayScene);
        trayScene.setFill(Color.TRANSPARENT);
        this.trayStage = new Stage();
        // 隐藏状态栏图标
        this.trayStage.initStyle(StageStyle.UTILITY);
        // 透明隐藏窗口
        this.trayStage.setOpacity(0);
        this.trayStage.setMaxWidth(0);
        this.trayStage.setMaxHeight(0);
        this.trayStage.setAlwaysOnTop(true);
        this.trayStage.setScene(trayScene);
        this.trayStage.show();
        return this.trayStage;
    }
    
    /**
     * 关闭托盘菜单容器
     */
    private void closeTrayStage() {
        if(this.trayStage != null) {
            this.trayStage.close();
            this.trayStage = null;
        }
    }
    
    /**
     * 提示信息（提示）
     * 
     * @param title   标题
     * @param content 内容
     */
    public void info(String title, String content) {
        this.notice(title, content, GuiContext.MessageType.INFO);
    }
    
    /**
     * 提示信息（警告）
     * 
     * @param title   标题
     * @param content 内容
     */
    public void warn(String title, String content) {
        this.notice(title, content, GuiContext.MessageType.WARN);
    }

    /**
     * 提示信息
     * 
     * @param title   标题
     * @param content 内容
     * @param type    类型
     */
    public void notice(String title, String content, GuiContext.MessageType type) {
        if(DownloadConfig.getNotice() && this.support) {
            this.trayIcon.displayMessage(title, content, this.getMessageType(type));
        }
    }
    
    /**
     * 关闭托盘
     */
    public static final void exit() {
        if(INSTANCE.support) {
            SystemTray.getSystemTray().remove(INSTANCE.trayIcon);
        }
    }
    
    /**
     * 显示
     */
    private EventHandler<ActionEvent> showAction = event -> Platform.runLater(MainWindow.getInstance()::show);
    
    /**
     * 隐藏
     */
    private EventHandler<ActionEvent> hideAction = event -> Platform.runLater(MainWindow.getInstance()::hide);
    
    /**
     * 官网与源码
     */
    private EventHandler<ActionEvent> sourceAction = event -> Desktops.browse(SystemConfig.getSource());
    
    /**
     * 问题与建议
     */
    private EventHandler<ActionEvent> supportAction = event -> Desktops.browse(SystemConfig.getSupport());
    
    /**
     * 关于
     */
    private EventHandler<ActionEvent> aboutAction = event -> AboutWindow.getInstance().show();
    
    /**
     * 退出
     */
    private EventHandler<ActionEvent> exitAction = event -> SystemContext.shutdown();
    
    /**
     * 窗口隐藏时：移除托盘菜单容器
     */
    private EventHandler<WindowEvent> windowHiddenAction = event -> Platform.runLater(this::closeTrayStage);
    
    /**
     * 通过Gui消息类型获取托盘消息类型
     * 
     * @param type Gui消息类型
     * 
     * @return 托盘消息类型
     */
    private MessageType getMessageType(GuiContext.MessageType type) {
        return switch (type) {
        case NONE  -> MessageType.NONE;
        case INFO  -> MessageType.INFO;
        case WARN  -> MessageType.WARNING;
        case ERROR -> MessageType.ERROR;
        default    -> MessageType.INFO;
        };
    }
    
}
