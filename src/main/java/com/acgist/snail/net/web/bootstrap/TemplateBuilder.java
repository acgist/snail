package com.acgist.snail.net.web.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>模板Builder</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class TemplateBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateBuilder.class);
	
	private static final TemplateBuilder INSTANCE = new TemplateBuilder();

	/**
	 * <p>HTML模板路径</p>
	 */
	private static final String TEMPATE_PATH = "/html/index.html";
	/**
	 * <p>HTML模板替换标签</p>
	 */
	private static final String TOKEN_TAG = "{{}}";
	
	/**
	 * <p>HTML模板</p>
	 */
	private final String template;
	
	private TemplateBuilder() {
		this.template = buildTemplate();
	}

	public static final TemplateBuilder getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>加载HTML模板</p>
	 * 
	 * @return HTML模板
	 */
	private String buildTemplate() {
		try {
			final var uri = Paths.get(this.getClass().getResource(TEMPATE_PATH).toURI());
			LOGGER.debug("加载HTML模板：{}", uri);
			final var bytes = Files.readAllBytes(uri);
			return new String(bytes);
		} catch (IOException | URISyntaxException e) {
			LOGGER.error("加载HTML模板异常", e);
		}
		return null;
	}
	
	/**
	 * <p>创建所有任务HTML</p>
	 * <pre>
	 * <xmp>
	 *	<ul>
	 *		<li>
	 *			<a href="/TaskId">snail-v1.0.0.zip</a>
	 *		</li>
	 *	</ul>
	 * </xmp>
	 * </pre>
	 * 
	 * @return 所有任务HTML
	 */
	public String buildTasks() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<ul>");
		DownloaderManager.getInstance().allTask().forEach(taskSession -> {
			builder.append("<li>");
			builder.append("<a href=\"/" + taskSession.getId() + "\">" + taskSession.getName() + "</a>");
			builder.append("<li>");
		});
		builder.append("</ul>");
		return this.template.replace(TOKEN_TAG, builder.toString());
	}

	/**
	 * <p>创建任务文件HTML</p>
	 * <p>列出所有文件，排除文件夹。</p>
	 * <pre>
	 * <xmp>
	 *	<ul>
	 *		<li>
	 *			<a href="/TaskId/FilePath">snail-v1.0.0.zip</a>
	 *			<div class="plan"></div>
	 *		</li>
	 *	</ul>
	 * </xmp>
	 * </pre>
	 * 
	 * @param id 任务ID
	 * 
	 * @return 任务文件HTML
	 */
	public String buildFiles(String id) {	
		final StringBuilder builder = new StringBuilder();
		final var optional = DownloaderManager.getInstance().allTask().stream()
			.filter(taskSession -> taskSession.getId().equals(id))
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		final var taskSession = optional.get();
		final var taskFile = new File(taskSession.getFile());
		if(!taskFile.exists()) {
			return null;
		}
		String prefix; // 文件目录
		if(taskFile.isFile()) {
			prefix = taskFile.getParent();
		} else {
			prefix = taskFile.getPath();
		}
		final List<String> files = new ArrayList<>();
		// 排序
		listFile(prefix.length(), taskFile, files);
		builder.append("<ul>");
		files.stream()
			.sorted((a, b) -> a.compareTo(b))
			.forEach((path) -> {
				builder.append("<li>");
				builder.append("<a href=\"/" + taskSession.getId() + "/" + UrlUtils.encode(path) + "\">" + FileUtils.fileNameFromUrl(path) + "</a>");
				builder.append("<li>");
			});
		builder.append("</ul>");
		return this.template.replace(TOKEN_TAG, builder.toString());
	}
	
	/**
	 * <p>列出所有文件</p>
	 * <p>如果{@code file}是目录递归所有文件</p>
	 * 
	 * @param begin 目录截取开始位置
	 * @param file 文件或目录
	 * @param list 所有文件列表
	 */
	private void listFile(final int begin, File file, List<String> list) {
		if(file.isFile()) {
			list.add(file.getPath().substring(begin));
		} else {
			final var files = file.listFiles();
			for (File children : files) {
				listFile(begin, children, list);
			}
		}
	}
	
}
