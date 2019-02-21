package com.acgist.snail.pojo.config;

public class SystemConfig {

	public static final String AIO_HOST = "localhost";
	public static final int AIO_PORT = 28888;
	public static final String DEFAULT_CHARSET = "utf-8";
	
	private String author;
	private String source;
	private String support;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSupport() {
		return support;
	}

	public void setSupport(String support) {
		this.support = support;
	}

}
