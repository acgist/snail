package com.acgist.snail.window.menu;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.event.MouseInputAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.SystemConfig;
import com.acgist.snail.utils.BrowseUtils;
import com.acgist.snail.utils.PlatformUtils;
import com.acgist.snail.window.about.AboutWindow;
import com.acgist.snail.window.main.MainWindow;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * 托盘按钮
 */
public class TrayMenu extends ContextMenu {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
	
	private Stage stage;
	private Stage trayStage;
	private TrayIcon trayIcon;
	private SystemConfig systemConfig;
	
	private static TrayMenu INSTANCE;

	public static final TrayMenu getInstance(Stage stage) {
		if (INSTANCE == null) {
			INSTANCE = new TrayMenu();
		}
		INSTANCE.stage = stage;
		return INSTANCE;
	}
	
	private TrayMenu() {
		createMenu();
		enableTray();
//		systemConfig = SpringContextUtils.getBean(SystemConfig.class);
	}

	/**
	 * 创建菜单
	 */
	private void createMenu() {
		MenuItem showMenu = new MenuItem("显示", new ImageView("/image/16/show.png"));
		MenuItem hideMenu = new MenuItem("隐藏", new ImageView("/image/16/hide.png"));
		MenuItem exitMenu = new MenuItem("退出", new ImageView("/image/16/exit.png"));
		MenuItem aboutMenu = new MenuItem("关于", new ImageView("/image/16/about.png"));
		MenuItem sourceMenu = new MenuItem("官网与源码", new ImageView("/image/16/source.png"));
		MenuItem supportMenu = new MenuItem("问题与建议", new ImageView("/image/16/support.png"));
		
		showMenu.setOnAction((event) -> {
			Platform.runLater(() -> {
				stage.show();
			});
		});
		
		hideMenu.setOnAction((event) -> {
			Platform.runLater(() -> {
				stage.hide();
			});
		});
		
		exitMenu.setOnAction((event) -> {
			PlatformUtils.exit(trayIcon);
		});
		
		aboutMenu.setOnAction((event) -> {
			AboutWindow.show();
		});
		
		sourceMenu.setOnAction((event) -> {
			BrowseUtils.open(systemConfig.getSource());
		});
		
		supportMenu.setOnAction((event) -> {
			BrowseUtils.open(systemConfig.getSupport());
		});
		
		this.addEventFilter(WindowEvent.WINDOW_HIDDEN, (event) -> { // 窗口隐藏时移除托盘显示的窗口
			Platform.runLater(() -> {
				if(trayStage != null) {
					trayStage.close();
				}
			});
		});
		
		this.getItems().add(showMenu);
		this.getItems().add(hideMenu);
		this.getItems().add(sourceMenu);
		this.getItems().add(supportMenu);
		this.getItems().add(aboutMenu);
		this.getItems().add(exitMenu);
	}
	
	/**
	 * 系统托盘
	 */
	private void enableTray() {
		MouseListener mouseListener = new MouseInputAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent event) {
				Platform.setImplicitExit(false);
				if (event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
					if (stage.isShowing()) {
						Platform.runLater(() -> {
							stage.hide();
						});
					} else {
						Platform.runLater(() -> {
							stage.show();
						});
					}
				} else if(event.getButton() == java.awt.event.MouseEvent.BUTTON3) {
					Platform.runLater(() -> {
						TrayMenu.INSTANCE.show(createTrayStage(), event.getXOnScreen(), event.getYOnScreen());
					});
				}
			}
		};
		try {
			BufferedImage image = ImageIO.read(MainWindow.class.getResourceAsStream("/image/16/logo.png"));
			trayIcon = new TrayIcon(image, "蜗牛");
			trayIcon.addMouseListener(mouseListener);
			SystemTray.getSystemTray().add(trayIcon);
		} catch (Exception e) {
			LOGGER.error("添加托盘异常", e);
		}
	}
	
	/**
	 * 托盘菜单
	 */
	private Stage createTrayStage() {
		FlowPane trayPane = new FlowPane();
		Stage trayStage = new Stage();
		trayPane.setBackground(Background.EMPTY);
		Scene scene = new Scene(trayPane);
		scene.setFill(Color.TRANSPARENT);
		trayStage.initStyle(StageStyle.UTILITY);
		trayStage.setOpacity(0);
		trayStage.setMaxWidth(0);
		trayStage.setMaxHeight(0);
		trayStage.setAlwaysOnTop(true);
		trayStage.setScene(scene);
		trayStage.show();
		this.trayStage = trayStage;
		return trayStage;
	}
	
}
