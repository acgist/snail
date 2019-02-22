package com.acgist.snail.pojo.message;

import com.acgist.snail.module.config.FileTypeConfig.FileType;

/**
 * 下载任务信息
 */
public class TaskMessage {

	private String name;
	private FileType fileType;
	private String status;
	private String progress;
	private String begin;
	private String end;

	public TaskMessage() {
	}

	public TaskMessage(String name, FileType fileType, String status, String progress, String begin, String end) {
		this.name = name;
		this.fileType = fileType;
		this.status = status;
		this.progress = progress;
		this.begin = begin;
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getBegin() {
		return begin;
	}

	public void setBegin(String begin) {
		this.begin = begin;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

}
