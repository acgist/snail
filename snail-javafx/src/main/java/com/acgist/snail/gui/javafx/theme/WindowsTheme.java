package com.acgist.snail.gui.javafx.theme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.acgist.snail.gui.javafx.Themes;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.StringUtils;

import javafx.scene.paint.Color;

/**
 * <p>Windows系统主题</p>
 * 
 * @author acgist
 */
public final class WindowsTheme implements ITheme {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsTheme.class);

	/**
	 * <p>Windows主题颜色KEY</p>
	 */
	private static final String THEME_COLOR_KEY = "ColorHistory0";
	/**
	 * <p>Windows主题颜色PATH</p>
	 */
	private static final String THEME_COLOR_PATH = "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\History\\Colors";
	
	private WindowsTheme() {
	}

	/**
	 * <p>新建Windows系统主题</p>
	 * 
	 * @return {@link WindowsTheme}
	 */
	public static final WindowsTheme newInstance() {
		return new WindowsTheme();
	}
	
	@Override
	public Color systemThemeColor() {
		Process process = null;
		final ProcessBuilder builder = new ProcessBuilder();
		try {
			// 查询命令：REG QUERY HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Themes\History\Colors /v ColorHistory0
			process = builder.command("REG", "QUERY", THEME_COLOR_PATH, "/v", THEME_COLOR_KEY)
				.redirectErrorStream(true)
				.start();
			return this.process(process);
		} catch (Exception e) {
			LOGGER.error("获取Windows主题颜色异常", e);
		} finally {
			if(process != null) {
				process.destroy();
			}
		}
		return Themes.DEFAULT_THEME_COLOR;
	}
	
	/**
	 * <p>读取命令</p>
	 * 
	 * @param process 命令
	 * 
	 * @return 颜色
	 * 
	 * @throws IOException IO异常
	 */
	private Color process(Process process) throws IOException {
		String line;
		String color = null;
		try (final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			while ((line = reader.readLine()) != null) {
				line = line.strip();
				if(StringUtils.startsWith(line, THEME_COLOR_KEY)) {
					final int index = line.indexOf("0x");
					color = line.substring(index).strip();
					break;
				}
			}
		}
		return this.convertColor(color);
	}
	
	/**
	 * <p>Windows颜色转换</p>
	 * 
	 * @param colorValue 十六进制颜色字符：0xffffff、0xffffffff
	 * 
	 * @return 颜色
	 */
	private Color convertColor(String colorValue) {
		Color theme;
		if(colorValue == null) {
			theme = Themes.DEFAULT_THEME_COLOR;
		} else {
			// 使用long类型防止超过int类型大小
			final long value = Long.parseLong(colorValue.substring(2), 16);
			final int alpha = (int) (value >> 24 & 0xFF);
			final int blud = (int) (value >> 16 & 0xFF);
			final int green = (int) (value >> 8 & 0xFF);
			final int red = (int) (value & 0xFF);
			if(alpha <= 0) {
				// 没有透明度：设置不透明
				theme = Color.rgb(red, green, blud);
			} else {
				final double opacity = alpha >= 255D ? 1D : alpha / 255D;
				theme = Color.rgb(red, green, blud, opacity);
			}
		}
		LOGGER.debug("Windows系统主题颜色：{}-{}", colorValue, theme);
		return theme;
	}
	
}
