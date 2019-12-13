package com.acgist.snail.net.web.bootstrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public String buildTasks() {
		return this.template;
	}

	public String buildFiles(String id) {		
		return this.template;
	}
	
}
