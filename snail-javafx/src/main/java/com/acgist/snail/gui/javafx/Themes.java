package com.acgist.snail.gui.javafx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.gui.javafx.theme.ITheme;
import com.acgist.snail.gui.javafx.theme.WindowsTheme;
import com.acgist.snail.system.context.SystemContext.SystemType;

import javafx.scene.paint.Color;

/**
 * <p>主题工具</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class Themes {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Themes.class);

	/**
	 * <p>默认主题颜色</p>
	 */
	public static final Color DEFAULT_THEME_COLOR = Color.rgb(0, 153, 204);
	/**
	 * <p>系统主题颜色</p>
	 */
	private static final Color SYSTEM_THEME_COLOR;
	/**
	 * <p>系统主题样式</p>
	 */
	private static final String SYSTEM_THEME_STYLE;

	static {
		final SystemType systemType = SystemType.local();
		ITheme themeHandler = null;
		if(systemType != null) {
			switch (systemType) {
			case WINDOWS:
				themeHandler = WindowsTheme.newInstance();
				break;
				// TODO：Mac、Linux
			default:
				LOGGER.info("系统主题没有适配系统类型：{}", systemType);
				break;
			}
		}
		// 系统主题颜色
		Color color;
		if(themeHandler != null) {
			color = themeHandler.systemThemeColor();
		} else {
			color = DEFAULT_THEME_COLOR;
		}
		SYSTEM_THEME_COLOR = color;
		// 设置主题样式
		final String colorHex = SYSTEM_THEME_COLOR.toString();
		final StringBuilder themeStyle = new StringBuilder();
		// 设置主题颜色
		themeStyle.append("-fx-snail-main-color:#").append(colorHex.toString().substring(2, colorHex.length() - 2)).append(";");
		SYSTEM_THEME_STYLE = themeStyle.toString();
	}
	
	private Themes() {
	}
	
	/**
	 * <p>获取系统主题样式</p>
	 * 
	 * @return 系统主题样式
	 */
	public static final String getThemeStyle() {
		return SYSTEM_THEME_STYLE;
	}
	
}
