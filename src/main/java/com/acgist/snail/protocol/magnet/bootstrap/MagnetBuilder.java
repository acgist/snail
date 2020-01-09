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
 * <p>磁力链接Builder</p>
 * <p>磁力链接解析只支持BT类型磁力链接，其他磁力链接均不支持。</p>
 * <p>BT类型磁力链接中也只支持单文件下载，不支持多文件下载。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class MagnetBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetBuilder.class);
	
	/**
	 * <p>显示名称：{@value}</p>
	 */
	public static final String QUERY_DN = "dn";
	/**
	 * <p>资源URN：{@value}</p>
	 */
	public static final String QUERY_XT = "xt";
	/**
	 * <p>文件链接（经过编码）：{@value}</p>
	 */
	public static final String QUERY_AS = "as";
	/**
	 * <p>绝对资源（经过编码）：{@value}</p>
	 */
	public static final String QUERY_XS = "xs";
	/**
	 * <p>Tracker服务器列表：{@value}</p>
	 */
	public static final String QUERY_TR = "tr";
	
	/**
	 * <p>磁力链接</p>
	 */
	private final String url;
	/**
	 * <p>磁力链接信息</p>
	 */
	private Magnet magnet;
	
	private MagnetBuilder(String url) {
		this.url = UrlUtils.decode(url);
	}

	public static final MagnetBuilder newInstance(String url) {
		return new MagnetBuilder(url);
	}
	
	/**
	 * <p>解析磁力链接信息</p>
	 * 
	 * @return 磁力链接信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public Magnet build() throws DownloadException {
		if(!Protocol.Type.MAGNET.verify(this.url)) {
			throw new DownloadException("磁力链接格式错误：" + this.url);
		}
		this.magnet = new Magnet();
		// 32位磁力链接
		if(Protocol.Type.verifyMagnetHash32(this.url)) {
			this.magnet.setType(Type.BTIH);
			this.magnet.setHash(InfoHash.newInstance(this.url).infoHashHex());
			return this.magnet;
		}
		// 40位磁力链接
		if(Protocol.Type.verifyMagnetHash40(this.url)) {
			this.magnet.setType(Type.BTIH);
			this.magnet.setHash(this.url);
			return this.magnet;
		}
		// 完整磁力链接
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
				case QUERY_DN:
					dn(value);
					break;
				case QUERY_XT:
					xt(value);
					break;
				case QUERY_AS:
					as(value);
					break;
				case QUERY_XS:
					xs(value);
					break;
				case QUERY_TR:
					tr(value);
					break;
				default:
					LOGGER.debug("磁力链接不支持的参数：{}-{}，磁力链接：{}", key, value, this.url);
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
	 * <p>解析XT：支持BT下载</p>
	 * 
	 * @param value XT
	 * 
	 * @throws DownloadException 下载异常
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
			hash = InfoHash.newInstance(hash).infoHashHex();
		}
		this.magnet.setHash(hash);
		this.magnet.setType(Magnet.Type.BTIH);
	}
	
	private void dn(String value) {
		this.magnet.setDn(value);
	}
	
	private void as(String value) {
		this.magnet.setAs(value);
	}
	
	private void xs(String value) {
		this.magnet.setXs(value);
	}
	
	private void tr(String value) {
		this.magnet.addTr(value);
	}
	
}
