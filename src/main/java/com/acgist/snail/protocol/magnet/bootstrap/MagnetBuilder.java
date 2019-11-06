package com.acgist.snail.protocol.magnet.bootstrap;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.bean.InfoHash;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.bean.Magnet.Type;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>磁力链接创建</p>
 * <p>现在解析只支持BT下载，其他下载连接均不支持，并且只支持单个文件，不支持多文件下载。</p>
 * <p>参考文章：https://www.cnblogs.com/linuxws/p/10166685.html</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class MagnetBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetBuilder.class);
	
	public static final String QUERY_XT = "xt";
	public static final String QUERY_DN = "dn";
	public static final String QUERY_TR = "tr";
	public static final String QUERY_AS = "as";
	public static final String QUERY_XS = "xs";
	public static final String QUERY_XL = "xl";
	public static final String QUERY_MT = "mt";
	public static final String QUERY_KT = "kt";
	
	private final String url;
	
	private Magnet magnet;
	
	private MagnetBuilder(String url) {
		this.url = UrlUtils.decode(url);
	}

	public static final MagnetBuilder newInstance(String url) {
		return new MagnetBuilder(url);
	}
	
	/**
	 * <p>解析磁力链接磁力链接信息</p>
	 */
	public Magnet build() throws DownloadException {
		if(!Protocol.Type.MAGNET.verify(this.url)) {
			throw new DownloadException("磁力链接格式错误：" + this.url);
		}
		this.magnet = new Magnet();
		if(Protocol.Type.verifyMagnetHash32(this.url)) {
			this.magnet.setType(Type.BTIH);
			final InfoHash infoHash = InfoHash.newInstance(this.url);
			this.magnet.setHash(infoHash.infoHashHex());
			return this.magnet;
		}
		if(Protocol.Type.verifyMagnetHash40(this.url)) {
			this.magnet.setType(Type.BTIH);
			this.magnet.setHash(this.url);
			return this.magnet;
		}
		int index;
		String key, value;
		final URI uri = URI.create(this.url);
		final String[] querys = uri.getSchemeSpecificPart().substring(1).split("&");
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
					LOGGER.debug("磁力链接错误（参数不支持）：{}-{}，磁力链接：{}", key, value, this.url);
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
	 * 解析XT：支持BT下载
	 */
	private void xt(String value) throws DownloadException {
		if(StringUtils.isEmpty(value)) {
			return;
		}
		final String xt = Magnet.Type.BTIH.xt();
		if(!value.startsWith(xt)) {
			LOGGER.debug("磁力链接不支持的XT：{}", value);
			return;
		}
		String hash = value.substring(xt.length());
		// 32位磁力链接转为40位磁力链接
		if(Protocol.Type.verifyMagnetHash32(hash)) {
			final InfoHash infoHash = InfoHash.newInstance(hash);
			hash = infoHash.infoHashHex();
		}
		this.magnet.setHash(hash);
		this.magnet.setType(Magnet.Type.BTIH);
	}
	
	private void dn(String value) {
		this.magnet.setDn(value);
	}
	
	private void tr(String value) {
		this.magnet.addTr(value);
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
