package com.acgist.snail.module.coder.torrent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 种子解码器
 */
public class TorrentDecoder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDecoder.class);
	
	private String hash = null;
	private TorrentInfo torrentInfo = null;
	
	private TorrentDecoder() {
	}
	
	/**
	 * 解析种子文件
	 */
	public static final TorrentDecoder newInstance(String filePath) throws Exception {
		TorrentDecoder decoder = new TorrentDecoder();
		try(InputStream input = new FileInputStream(filePath)) {
			decoder.decode(input);
		} catch (Exception e) {
			LOGGER.error("解析种子异常", e);
		}
		return decoder;
	}
	
	/**
	 * 获取hash
	 */
	public String hash() {
		return hash;
	}
	
	/**
	 * 获取种子信息
	 */
	public TorrentInfo torrentInfo() {
		return torrentInfo;
	}

	/**
	 * 编码：http://blog.sina.com.cn/s/blog_ec8c9eae0102wa9p.html
	 * i e：long
	 * l e：list
	 * d e：map
	 */
	private void decode(InputStream input) throws NumberFormatException, IOException, Exception {
		int index;
		String key = null;
		TorrentInfo torrentInfo = new TorrentInfo();
		StringBuilder lengthBuilder = new StringBuilder();
		TorrentHashBuilder hashBuilder = TorrentHashBuilder.newInstance();
		while ((index = input.read()) != -1) {
			char indexChar = (char) index;
			hashBuilder.build(key, index);
			switch (indexChar) {
				case 'i':
					int valueIndex;
					char valueChar;
					StringBuilder valueBuilder = new StringBuilder();
					while((valueIndex = input.read()) != -1) {
						valueChar = (char) valueIndex;
						hashBuilder.build(key, valueIndex);
						if(valueChar == 'e') {
							break;
						} else {
							valueBuilder.append(valueChar);
						}
					}
					torrentInfo.setValue(key, valueBuilder.toString());
					break;
				case 'l':
				case 'd':
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
					lengthBuilder.append(indexChar);
					break;
				case ':':
					int valueLength = Integer.parseInt(lengthBuilder.toString());
					lengthBuilder.setLength(0);
					byte[] bytes = new byte[valueLength];
					input.read(bytes);
					hashBuilder.build(key, bytes);
					String value = new String(bytes);
					if (TorrentInfo.infoKeys().contains(value)) { // 初始化
						key = value;
						if (value.equals("info")) {
							torrentInfo.setInfo(new TorrentFiles());
						} else if (value.equals("files")) {
							torrentInfo.getInfo().getFiles().add(new TorrentFile());
						} else if (value.equals("length")) {
							if (torrentInfo.hasFiles() && torrentInfo.lastTorrentFile().getLength() != null) {
								torrentInfo.getInfo().getFiles().add(new TorrentFile());
							}
						}
					} else {
						if(key.equals("ed2k") || key.equals("pieces") || key.equals("filehash")) {
							torrentInfo.setValue(key, bytes);
						} else {
							torrentInfo.setValue(key, value);
						}
					}
					break;
			}
		}
		this.torrentInfo = torrentInfo;
		this.hash = hashBuilder.hash();
	}
	
}
