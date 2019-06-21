package com.acgist.snail.protocol.magnet.bootstrap;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.bean.Magnet.Type;
import com.acgist.snail.protocol.magnet.MagnetProtocol;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>磁力链接解析器</p>
 * <p>现在解析只支持BT下载，其他下载连接均不支持，并且只支持单个文件，不支持参数组。</p>
 * <p>参考链接：https://www.cnblogs.com/linuxws/p/10166685.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MagnetReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetReader.class);
	
	private final String url;
	
	private Magnet magnet;
	
	public static final String QUERY_XT = "xt";
	public static final String QUERY_DN = "dn";
	public static final String QUERY_TR = "tr";
	public static final String QUERY_AS = "as";
	public static final String QUERY_XS = "xs";
	public static final String QUERY_XL = "xl";
	public static final String QUERY_MT = "mt";
	public static final String QUERY_KT = "kt";
	
	private MagnetReader(String url) {
		this.url = UrlUtils.decode(url);
	}

	public static final MagnetReader newInstance(String url) {
		return new MagnetReader(url);
	}
	
	/**
	 * 解析磁力链接获取hash
	 */
	public Magnet magnet() throws DownloadException {
		if(!MagnetProtocol.verify(this.url)) {
			throw new DownloadException("不支持的磁力链接：" + this.url);
		}
		this.magnet = new Magnet();
		if(MagnetProtocol.verifyMagnetHash32(this.url)) {
			this.magnet.setType(Type.btih);
			final InfoHash infoHash = InfoHash.newInstance(this.url);
			this.magnet.setHash(infoHash.infoHashHex());
			return this.magnet;
		}
		if(MagnetProtocol.verifyMagnetHash40(this.url)) {
			this.magnet.setType(Type.btih);
			this.magnet.setHash(this.url);
			return this.magnet;
		}
		int index;
		String key, value;
		final URI uri = URI.create(this.url);
		String[] querys = uri.getSchemeSpecificPart().substring(1).split("&");
		for (String query : querys) {
			index = query.indexOf("=");
			if(index >= 0 && query.length() > index) {
				key = query.substring(0, index);
				value = query.substring(index + 1);
				switch (key) {
				case QUERY_XT:
					xt(value);
					break;
				case QUERY_DN:
					dn(value);
					break;
				case QUERY_TR:
					tr(value);
					break;
				case QUERY_AS:
					as(value);
					break;
				case QUERY_XS:
					xs(value);
					break;
				case QUERY_XL:
					xl(value);
					break;
				case QUERY_MT:
					mt(value);
					break;
				case QUERY_KT:
					kt(value);
					break;
				default:
					LOGGER.debug("不支持的磁力链接参数：{}-{}，磁力链接：{}", key, value, this.url);
					break;
				}
			}
		}
		if(this.magnet.supportDownload()) {
			return this.magnet;
		}
		throw new DownloadException("磁力链接不支持下载：" + this.url);
	}
	
	/**
	 * 解析XT，支持BT下载。
	 */
	private void xt(String value) throws DownloadException {
		if(StringUtils.isEmpty(value)) {
			return;
		}
		final String xt = Magnet.Type.btih.xt();
		if(!value.startsWith(xt)) {
			return;
		}
		String hash = value.substring(xt.length());
		if(MagnetProtocol.verifyMagnetHash32(hash)) {
			final InfoHash infoHash = InfoHash.newInstance(hash);
			hash = infoHash.infoHashHex();
		}
		this.magnet.setHash(hash);
		this.magnet.setType(Magnet.Type.btih);
	}
	
	private void dn(String value) {
		this.magnet.setDn(value);
	}
	
	private void tr(String value) {
		this.magnet.setTr(value);
	}
	
	private void as(String value) {
		this.magnet.setAs(value);
	}
	
	private void xs(String value) {
		this.magnet.setXs(value);
	}
	
	private void xl(String value) {
		this.magnet.setXl(value);
	}
	
	private void mt(String value) {
		this.magnet.setMt(value);
	}
	
	private void kt(String value) {
		this.magnet.setKt(value);
	}
	
}
