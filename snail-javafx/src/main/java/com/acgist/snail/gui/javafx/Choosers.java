package com.acgist.snail.gui.javafx;

import java.io.File;
import java.util.List;

import com.acgist.snail.config.DownloadConfig;
import com.acgist.snail.utils.CollectionUtils;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * <p>文件、目录选择助手</p>
 * <p>文件选择默认打开上次选择目录，选择成功更新上次选择目录（当前选择目录）。</p>
 * 
 * @author acgist
 */
public final class Choosers {

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
		// 设置上次选择目录
		Choosers.lastPath(chooser);
		chooser.getExtensionFilters().add(new ExtensionFilter(description, filters));
		final File file = chooser.showOpenDialog(window);
		if (file != null) {
			// 更新上次选择目录
			DownloadConfig.setLastPath(file.getParentFile().getAbsolutePath());
		}
		return file;
	}
	
	/**
	 * <p>选择多个文件</p>
	 * 
	 * @param window 当前窗体：模态
	 * @param title 标题
	 * @param description 过滤器描述
	 * @param filters 过滤器类型（文件类型后缀）：*.torrent
	 * 
	 * @return 选择文件集合
	 */
	public static final List<File> chooseMultipleFile(Window window, String title, String description, String ... filters) {
		final FileChooser chooser = new FileChooser();
		chooser.setTitle(title);
		// 设置上次选择目录
		Choosers.lastPath(chooser);
		chooser.getExtensionFilters().add(new ExtensionFilter(description, filters));
		final List<File> files = chooser.showOpenMultipleDialog(window);
		if (CollectionUtils.isNotEmpty(files)) {
			// 更新上次选择目录
			DownloadConfig.setLastPath(files.get(0).getParentFile().getAbsolutePath());
		}
		return files;
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
		// 设置上次选择目录
		Choosers.lastPath(chooser);
		final File file = chooser.showDialog(window);
		if (file != null) {
			// 更新上次选择目录
			DownloadConfig.setLastPath(file.getAbsolutePath());
		}
		return file;
	}
	
	/**
	 * <p>设置上次选择目录</p>
	 * 
	 * @param chooser 文件选择器
	 */
	private static final void lastPath(FileChooser chooser) {
		final File file = DownloadConfig.getLastPathFile();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}
	
	/**
	 * <p>设置上次选择目录</p>
	 * 
	 * @param chooser 目录选择器
	 */
	private static final void lastPath(DirectoryChooser chooser) {
		final File file = DownloadConfig.getLastPathFile();
		if(file != null && file.exists()) {
			chooser.setInitialDirectory(file);
		}
	}

}
