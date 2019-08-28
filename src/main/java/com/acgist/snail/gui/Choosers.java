package com.acgist.snail.gui;

import java.io.File;

import com.acgist.snail.system.config.DownloadConfig;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * <p>目录、文件选择器</p>
 * <p>选择时默认都会设置为上一次选择的目录，选择后会修改上次选择目录为当前选择目录。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Choosers {

	/**
	 * 选择文件
	 * 
	 * @param window 当前窗体：模态
	 * @param title 标题
	 * @param description 过滤器描述
	 * @param filters 过滤器：文件类型名称、后缀：*.torrent
	 * 
	 * @return 文件
	 */
	public static final File chooseFile(Window window, String title, String description, String ... filters) {
		final FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		lastPath(chooser);
		chooser.getExtensionFilters().add(new ExtensionFilter(description, filters));
		final File file = chooser.showOpenDialog(window);
		if (file != null) {
			DownloadConfig.setLastPath(file.getParent());
		}
		return file;
	}
	
	/**
	 * 选择文件目录
	 * 
	 * @param window 当前窗体：模态
	 * @param title 标题
	 * 
	 * @return 文件目录
	 */
	public static final File chooseDirectory(Window window, String title) {
		final DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(title);
		lastPath(chooser);
		final File file = chooser.showDialog(window);
		if (file != null) {
			DownloadConfig.setLastPath(file.getPath());
		}
		return file;
	}
	
	/**
	 * 最后一次选择目录
	 */
	private static final void lastPath(FileChooser chooser) {
		final File file = DownloadConfig.getLastPathFile();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
	/**
	 * 最后一次选择目录
	 */
	private static final void lastPath(DirectoryChooser chooser) {
		final File file = DownloadConfig.getLastPathFile();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
}
