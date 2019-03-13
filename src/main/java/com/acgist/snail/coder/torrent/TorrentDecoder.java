package com.acgist.snail.coder.torrent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acgist.snail.coder.torrent.pojo.Torrent;
import com.acgist.snail.coder.torrent.pojo.TorrentInfo;
import com.acgist.snail.pojo.wrapper.TorrentWrapper;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子解析器
 */
public class TorrentDecoder {
	
	public static final String TORRENT_SUFFIX = ".torrent"; // 文件后缀
	public static final String TORRENT_REGEX = ".+\\.torrent"; // 正则表达式
	
	private String hash = null; // 磁力链接HASH
	private Torrent torrent = null; // 种子文件信息
	
	private TorrentDecoder() {
	}
	
	/**
	 * 解析种子文件
	 */
	public static final TorrentDecoder newInstance(String filePath) throws DownloadException {
		TorrentDecoder decoder = new TorrentDecoder();
		try(InputStream input = new FileInputStream(filePath)) {
			decoder.decode(input);
		} catch (Exception e) {
			throw new DownloadException("解析种子异常", e);
		}
		return decoder;
	}
	
	/**
	 * 获取磁力链接HASH
	 */
	public String hash() {
		return hash;
	}
	
	/**
	 * 获取种子信息
	 */
	public TorrentWrapper torrentWrapper() throws DownloadException {
		return new TorrentWrapper(torrent);
	}
	
	/**
	 * 编码：http://blog.sina.com.cn/s/blog_ec8c9eae0102wa9p.html
	 * i e：long
	 * l e：list
	 * d e：map
	 */
	private void decode(InputStream input) throws NumberFormatException, IOException, Exception {
		int index;
		char indexChar;
		String key = null;
		Torrent torrent = new Torrent();
		StringBuilder lengthBuilder = new StringBuilder();
		TorrentHashBuilder hashBuilder = TorrentHashBuilder.newInstance();
		while ((index = input.read()) != -1) {
			hashBuilder.build(key, index);
			indexChar = (char) index;
			switch (indexChar) {
				case 'i':
					StringBuilder valueBuilder = new StringBuilder();
					while((index = input.read()) != -1) {
						hashBuilder.build(key, index);
						indexChar = (char) index;
						if(indexChar == 'e') {
							break;
						} else {
							valueBuilder.append(indexChar);
						}
					}
					torrent.setValue(key, valueBuilder.toString());
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
					int length = Integer.parseInt(lengthBuilder.toString());
					lengthBuilder.setLength(0);
					byte[] bytes = new byte[length];
					input.read(bytes);
					hashBuilder.build(key, bytes);
					String value = new String(bytes);
					if (Torrent.infoKeys().contains(value)) { // 初始化
						key = value;
						if (value.equals("info")) {
							torrent.setInfo(new TorrentInfo());
						} else if (value.equals("files")) {
							torrent.newTorrentFile();
						}
					} else {
						if(key.equals("ed2k") || key.equals("pieces") || key.equals("filehash")) {
							torrent.setValue(key, bytes);
						} else {
							torrent.setValue(key, value);
						}
					}
					break;
			}
		}
		this.torrent = torrent;
		this.hash = hashBuilder.hash();
	}
	
	/**
	 * 验证BT种子
	 */
	public static final boolean verify(String url) {
		return StringUtils.endsWith(url.toLowerCase(), TORRENT_SUFFIX);
	}
	
}
