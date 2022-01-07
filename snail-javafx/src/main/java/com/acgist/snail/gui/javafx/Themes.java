package com.acgist.snail.gui.javafx;

import com.acgist.snail.context.SystemContext.SystemType;
import com.acgist.snail.gui.javafx.theme.ITheme;
import com.acgist.snail.gui.javafx.theme.WindowsTheme;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * <p>主题助手</p>
 * <p>使用CMD命令获取（可以使用JNA调用系统接口替换）</p>
 * 
 * @author acgist
 */
public final class Themes {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Themes.class);

	/**
	 * <p>FXML样式路径：{@value}</p>
	 */
	public static final String FXML_STYLE = "/style/fxml.css";
	/**
	 * <p>图标文件路径（16PX）：{@value}</p>
	 */
	public static final String LOGO_ICON_16 = "/image/16/logo.png";
	/**
	 * <p>图标文件路径（256PX）：{@value}</p>
	 */
	public static final String LOGO_ICON = "/image/logo.png";
	/**
	 * <p>红色：禁用</p>
	 */
	public static final Color COLOR_RED = Color.rgb(0xDD, 0x33, 0x55);
	/**
	 * <p>灰色：禁用</p>
	 */
	public static final Color COLOR_GRAY = Color.rgb(0xCC, 0xCC, 0xCC);
	/**
	 * <p>蓝色：可用</p>
	 */
	public static final Color COLOR_BLUD = Color.rgb(0x00, 0x99, 0xCC);
	/**
	 * <p>绿色：可用</p>
	 */
	public static final Color COLOR_GREEN = Color.rgb(0x22, 0xAA, 0x22);
	/**
	 * <p>黄色：警告</p>
	 */
	public static final Color COLOR_YELLOW = Color.rgb(0xFF, 0xEE, 0x99);
	/**
	 * <p>托盘样式</p>
	 */
	public static final String CLASS_TRAY = "tray";
	/**
	 * <p>图标样式</p>
	 */
	public static final String CLASS_SNAIL_ICON = "snail-icon";
	/**
	 * <p>没有任务样式</p>
	 */
	public static final String CLASS_TASK_EMPTY = "placeholder";
	/**
	 * <p>系统信息样式</p>
	 */
	public static final String CLASS_SYSTEM_INFO = "system-info";
	/**
	 * <p>画图信息样式</p>
	 */
	public static final String CLASS_PAINTER_INFO = "painter-info";
	/**
	 * <p>统计信息样式</p>
	 */
	public static final String CLASS_STATISTICS_INFO = "statistics-info";
	/**
	 * <p>默认主题颜色</p>
	 */
	public static final Color DEFAULT_THEME_COLOR = Themes.COLOR_BLUD;
	/**
	 * <p>系统主题颜色</p>
	 */
	private static final Color SYSTEM_THEME_COLOR;
	/**
	 * <p>系统主题样式</p>
	 */
	private static final String SYSTEM_THEME_STYLE;

	static {
		// 设置系统主题颜色
		ITheme themeHandler = null;
		final SystemType systemType = SystemType.local();
		if(systemType == SystemType.WINDOWS) {
			themeHandler = WindowsTheme.newInstance();
		} else {
			LOGGER.info("没有适配系统主题类型：{}", systemType);
		}
		Color color;
		if(themeHandler != null) {
			color = themeHandler.systemThemeColor();
		} else {
			color = DEFAULT_THEME_COLOR;
		}
		SYSTEM_THEME_COLOR = color;
		// 设置系统主题样式
		final String colorHex = SYSTEM_THEME_COLOR.toString();
		final StringBuilder themeStyle = new StringBuilder();
		themeStyle
			.append("-fx-snail-main-color:#")
			// 十六进制颜色：0x + RRGGBB + OPACITY
			.append(colorHex, 2, 8)
			.append(";");
		SYSTEM_THEME_STYLE = themeStyle.toString();
	}
	
	private Themes() {
	}
	
	/**
	 * <p>获取Logo图标</p>
	 * 
	 * @return Logo图标
	 */
	public static final Image getLogo() {
		return new Image(Themes.LOGO_ICON);
	}
	
	/**
	 * <p>获取系统主题颜色</p>
	 * 
	 * @return 系统主题颜色
	 */
	public static final Color getColor() {
		return SYSTEM_THEME_COLOR;
	}
	
	/**
	 * <p>获取系统主题样式</p>
	 * 
	 * @return 系统主题样式
	 */
	public static final String getStyle() {
		return SYSTEM_THEME_STYLE;
	}
	
	/**
	 * <p>设置Logo图标</p>
	 * 
	 * @param icons 图标列表
	 */
	public static final void applyLogo(ObservableList<Image> icons) {
		icons.add(Themes.getLogo());
	}
	
	/**
	 * <p>设置场景主题样式</p>
	 * 
	 * @param scene 场景
	 */
	public static final void applyStyle(Scene scene) {
		final Parent root = scene.getRoot();
		// 设置主题样式
		Themes.applyStyle(root);
		// 设置样式文件
		root.getStylesheets().add(Themes.FXML_STYLE);
	}

	/**
	 * <p>设置节点主题样式</p>
	 * 
	 * @param node 节点
	 */
	public static final void applyStyle(Node node) {
		node.setStyle(Themes.getStyle());
	}
	
	/**
	 * <p>添加样式</p>
	 * 
	 * @param styleable 样式列表
	 * @param styleClass 样式名称
	 */
	public static final void applyClass(Styleable styleable, String styleClass) {
		styleable.getStyleClass().add(styleClass);
	}
	
}
