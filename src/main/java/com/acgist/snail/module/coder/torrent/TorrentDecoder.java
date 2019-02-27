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

	/**
	 * B编码：http://blog.sina.com.cn/s/blog_ec8c9eae0102wa9p.html
	 * i e：long
	 * l e：list
	 * d e：map
	 */
	private TorrentInfo decode(InputStream input) throws NumberFormatException, IOException, Exception {
		int index;
		String key = null;
		TorrentInfo info = new TorrentInfo();
		StringBuilder lengthBuilder = new StringBuilder();
		while ((index = input.read()) != -1) {
			char indexChar = (char) index;
			switch (indexChar) {
				case 'i':
					char tmpChar;
					StringBuilder tmpBuilder = new StringBuilder();
					while ((tmpChar = (char) input.read()) != 'e') {
						tmpBuilder.append(tmpChar);
					}
					info.setValue(key, tmpBuilder.toString());
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
					String tmpValue = new String(bytes);
					if (TorrentInfo.infoKeys().contains(tmpValue)) { // 初始化
						key = tmpValue;
						if (tmpValue.equals("info")) {
							info.setInfo(new TorrentFiles());
						} else if (tmpValue.equals("files")) {
							info.getInfo().getFiles().add(new TorrentFile());
						} else if (tmpValue.equals("length")) {
							if (info.hasFiles() && info.lastTorrentFile().getLength() != 0) {
								info.getInfo().getFiles().add(new TorrentFile());
							}
						}
					} else {
						if(key.equals("ed2k") || key.equals("pieces") || key.equals("filehash")) {
							info.setValue(key, bytes);
						} else {
							info.setValue(key, tmpValue);
						}
					}
					break;
			}
		}
		return info;
	}

}
