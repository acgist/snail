package com.acgist.snail.module.decoder.torrent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 种子解码器
 */
public class TorrentDecoder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDecoder.class);
	
	private TorrentDecoder() {
	}
	
	/**
	 * 解析种子文件
	 */
	public static final TorrentInfo decode(String filePath) throws Exception {
		try(InputStream input = new FileInputStream(filePath)) {
			TorrentDecoder decoder = new TorrentDecoder();
			return decoder.decode(input);
		} catch (Exception e) {
			LOGGER.error("解析种子异常", e);
		}
		return null;
	}

	private TorrentInfo decode(InputStream input) throws NumberFormatException, IOException, Exception {
		int index;
		String key = null;
		TorrentInfo info = new TorrentInfo();
		StringBuilder builder = new StringBuilder();
		while ((index = input.read()) != -1) {
			char indexChar = (char) index;
			switch (indexChar) {
				case 'i':
					StringBuilder iBuilder = new StringBuilder();
					char iTmp;
					while ((iTmp = (char) input.read()) != 'e') {
						iBuilder.append(iTmp);
					}
					info.setValue(key, iBuilder.toString());
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					builder.append(indexChar);
					break;
				case ':':
					int tmpLength = Integer.parseInt(builder.toString());
					builder.setLength(0);
					byte[] bytes = new byte[tmpLength];
					input.read(bytes);
					if (key != null && key.equals("pieces")) {
						info.setValue(key, bytes);
					} else {
						String tmpValue = new String(bytes);
						if (TorrentInfo.infoKeys().contains(tmpValue)) {
							key = tmpValue;
							if (tmpValue.equals("info")) {
								info.setInfo(new TorrentFileInfo());
							} else if (tmpValue.equals("path")) {
								List<TorrentFile> tmpFiles = info.getInfo().getFiles();
								if (tmpFiles.isEmpty()) {
									TorrentFile files = new TorrentFile();
									files.setPath(new LinkedList<String>());
									tmpFiles.add(files);
								} else {
									TorrentFile files = tmpFiles.get(tmpFiles.size() - 1);
									if (files.getPath() == null) {
										files.setPath(new LinkedList<String>());
									}
								}
							} else if (tmpValue.equals("files")) {
								info.getInfo().setFiles(new LinkedList<TorrentFile>());
								info.getInfo().getFiles().add(new TorrentFile());
							} else if (tmpValue.equals("length")) {
								List<TorrentFile> tmpFiles = info.getInfo().getFiles();
								if (tmpFiles != null) {
									if (tmpFiles.isEmpty() || tmpFiles.get(tmpFiles.size() - 1).getLength() != 0) {
										tmpFiles.add(new TorrentFile());
									}
								}
							} else if (tmpValue.equals("md5sum")) {
								List<TorrentFile> tempFiles = info.getInfo().getFiles();
								if (tempFiles != null) {
									if (tempFiles.isEmpty() || tempFiles.get(tempFiles.size() - 1).getMd5sum() != null) {
										tempFiles.add(new TorrentFile());
									}
								}
							} else if (tmpValue.equals("announce-list")) {
								info.setAnnounceList(new LinkedList<String>());
							}
						} else {
							info.setValue(key, tmpValue);
						}
					}
					break;
			}
		}
		return info;
	}

	public static void main(String[] args) throws Exception {
		TorrentInfo info = decode("F:/迅雷下载/我的大叔/0B156834B59B0FF64EE0C9305D4D6EDE421196E6.torrent");
		System.out.println("信息:" + info.getAnnounce() + "\t" + info.getComment() + "\t" + info.getCreateBy() + "\t" + new Date(info.getCreationDate()));
		TorrentFileInfo it = info.getInfo();
		System.out.println("信息:" + it.getName() + "\t" + it.getPiecesLength() + "\t" + it.getLength() + "\t" + it.getMd5sum() + "\t" + it.getPieces());
		if (info.getAnnounceList().size() > 0) {
			for (String str : info.getAnnounceList()) {
				System.out.println("信息2:" + str);
			}
		}
		if (it.getFiles().size() > 0) {
			for (TorrentFile file : it.getFiles()) {
				System.out.println("信息3:" + file.getLength() + "\t" + file.getMd5sum());
				if (file.getPath().size() > 0) {
					for (String str : file.getPath()) {
						System.out.println("信息4：" + str);
					}
				}
			}
		}
	}
}
