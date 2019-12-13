package com.acgist.snail.net.web.bootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>Html Builder</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class HtmlBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlBuilder.class);
	
	private static final HtmlBuilder INSTANCE = new HtmlBuilder();

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
	
	private HtmlBuilder() {
		this.template = buildTemplate();
	}

	public static final HtmlBuilder getInstance() {
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
	 *			<a href="/id">snail-v1.0.0.zip</a>
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
	 *	<ul>
	 *		<li>
	 *			<a>snail-v1.0.0.zip</a>
	 *			<div class="plan"></div>
	 *		</li>
	 *	</ul>
	 * 
	 * @param id 任务ID
	 * 
	 * @return 任务文件HTML
	 * 
	 * @throws NetException 网络异常
	 */
	public String buildFiles(String id) {	
		final StringBuilder builder = new StringBuilder();
		final var optional = DownloaderManager.getInstance().allTask().stream()
			.filter(taskSession -> taskSession.getId().equals(id))
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
//		builder.append("<ul>");
//		optional.get().selectTorrentFiles().forEach(taskSession -> {
//			builder.append("<li>");
//			builder.append("<a href=\"/" + taskSession.getId() + "\">" + taskSession.getName() + "</a>");
//			builder.append("<li>");
//		});
//		builder.append("</ul>");
		return this.template.replace(TOKEN_TAG, builder.toString());
	}
	
}
