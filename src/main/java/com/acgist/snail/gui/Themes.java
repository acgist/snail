package com.acgist.snail.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.StringUtils;

import javafx.scene.paint.Color;

/**
 * <p>Windows工具</p>
 * 
 * @author acgist
 * @since 1.4.0
 */
public final class Themes {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Themes.class);

	/**
	 * <p>主题颜色PATH</p>
	 */
	private static final String THEME_COLOR_PATH = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\History\\Colors";
	/**
	 * <p>主题颜色KEY</p>
	 */
	private static final String THEME_COLOR_KEY = "ColorHistory0";
	/**
	 * <p>获取主题颜色命令</p>
	 */
	private static final String THEME_COLOR_COMMAND = "REG QUERY " + THEME_COLOR_PATH + " /v " + THEME_COLOR_KEY;
	/**
	 * <p>默认主题颜色</p>
	 */
	private static final Color DEFAULT_THEME_COLOR = Color.rgb(0, 153, 204);
	/**
	 * <p>系统主题</p>
	 */
	private static final Color SYSTEM_COLOR;
	
	static {
		SYSTEM_COLOR = getThemeColor();
	}
	
	/**
	 * <p>获取系统主题颜色样式</p>
	 * 
	 * @return 系统主题颜色样式
	 * 
	 * TODO：缓存
	 */
	public static final String getThemeStyle() {
		final String colorHex = SYSTEM_COLOR.toString();
		final StringBuilder themeStyle = new StringBuilder();
		themeStyle.append("-fx-snail-main-color:#").append(colorHex.toString().substring(2, colorHex.length() - 2)).append(";");
		return themeStyle.toString();
	}
	
	/**
	 * <p>获取Windows主题颜色</p>
	 * 
	 * @return 主题颜色
	 * 
	 * TODO：兼容mac、linux
	 */
	public static final Color getThemeColor() {
		String color = null;
		Process process = null;
		OutputStream output = null;
		BufferedReader reader = null;
		try {
			process = Runtime.getRuntime().exec(THEME_COLOR_COMMAND);
			output = process.getOutputStream();
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if(StringUtils.startsWith(line, THEME_COLOR_KEY)) {
					color = line.substring(line.length() - 10).trim();
					break;
				}
			}
		} catch (IOException e) {
			LOGGER.error("获取Windows主题颜色异常", e);
		} finally {
			IoUtils.close(output);
			IoUtils.close(reader);
			if(process != null) {
				process.destroy();
			}
		}
		return convertColor(color);
	}

	/**
	 * <p>颜色转换</p>
	 * 
	 * @param colorValue 十六进制颜色字符：0xffffffff
	 * 
	 * @return 颜色
	 */
	private static final Color convertColor(String colorValue) {
		Color theme;
		if(colorValue == null) {
			theme = DEFAULT_THEME_COLOR;
		} else {
			final int value = (int) Long.parseLong(colorValue.substring(2), 16);
			final int alpha = (int) ((value >> 24) & 0xFF);
			final int blud = (int) ((value >> 16) & 0xFF);
			final int green = (int) ((value >> 8) & 0xFF);
			final int red = (int) (value & 0xFF);
			final double opacity = alpha > 255D ? 1D : alpha / 255D;
			if(alpha == 0) {
				theme = Color.rgb(red, green, blud);
			} else {
				theme = Color.rgb(red, green, blud, opacity);
			}
		}
		LOGGER.info("系统主题颜色：{}-{}", colorValue, theme);
		return theme;
	}
	
}
