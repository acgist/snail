package com.acgist.snail.gui;

import java.io.File;

import com.acgist.snail.system.config.DownloadConfig;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * <p>文件、目录选择工具</p>
 * <p>打开选择框时默认设置为上次选择目录，选择后会修改上次选择目录为当前选择目录。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class Choosers {

	/**
	 * <p>不允许实例化</p>
	 */
	private Choosers() {
	}
	
	/**
	 * <p>选择文件</p>
	 * 
	 * @param window 当前窗体：模态
	 * @param title 标题
	 * @param description 过滤器描述
	 * @param filters 过滤器类型（文件类型后缀）：*.torrent
	 * 
	 * @return 选择文件
	 */
	public static final File chooseFile(Window window, String title, String description, String ... filters) {
		final FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		lastPath(chooser); // 设置上次选择目录
		chooser.getExtensionFilters().add(new ExtensionFilter(description, filters));
		final File file = chooser.showOpenDialog(window);
		if (file != null) { // 更新上次选择目录
			DownloadConfig.setLastPath(file.getParent());
		}
		return file;
	}
	
	/**
	 * <p>选择目录</p>
	 * 
	 * @param window 当前窗体：模态
	 * @param title 标题
	 * 
	 * @return 选择目录
	 */
	public static final File chooseDirectory(Window window, String title) {
		final DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(title);
		lastPath(chooser); // 设置上次选择目录
		final File file = chooser.showDialog(window);
		if (file != null) { // 更新上次选择目录
			DownloadConfig.setLastPath(file.getPath());
		}
		return file;
	}
	
	/**
	 * <p>设置上次选择目录</p>
	 */
	private static final void lastPath(FileChooser chooser) {
		final File file = DownloadConfig.getLastPathFile();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
	/**
	 * <p>设置上次选择目录</p>
	 */
	private static final void lastPath(DirectoryChooser chooser) {
		final File file = DownloadConfig.getLastPathFile();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}

}
