package com.acgist.snail.protocol.magnet;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.wrapper.URIWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.net.torrent.Magnet;
import com.acgist.snail.net.torrent.Magnet.Type;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>磁力链接Builder</p>
 * <p>磁力链接解析只支持单条BT类型磁力链接</p>
 * 
 * @author acgist
 */
public final class MagnetBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetBuilder.class);
	
	/**
	 * <p>显示名称：{@value}</p>
	 */
	public static final String QUERY_DN = "dn";
	/**
	 * <p>文件大小：{@value}</p>
	 */
	public static final String QUERY_XL = "xl";
	/**
	 * <p>资源URN：{@value}</p>
	 */
	public static final String QUERY_XT = "xt";
	/**
	 * <p>文件链接：{@value}</p>
	 */
	public static final String QUERY_AS = "as";
	/**
	 * <p>绝对资源：{@value}</p>
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
	 * <p>磁力链接</p>
	 */
	private Magnet magnet;
	
	/**
	 * @param url 磁力链接
	 * 
	 * @throws DownloadException 下载异常
	 */
	private MagnetBuilder(String url) throws DownloadException {
		if(!Protocol.Type.MAGNET.verify(url)) {
			throw new DownloadException("磁力链接格式错误：" + url);
		}
		this.url = url;
	}

	/**
	 * <p>获取磁力链接Builder</p>
	 * 
	 * @param url 磁力链接
	 * 
	 * @return 磁力链接Builder
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final MagnetBuilder newInstance(String url) throws DownloadException {
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
		String key;
		String value;
		final String[] querys = URIWrapper.newInstance(this.url).decode().querys();
		for (String query : querys) {
			index = query.indexOf(SymbolConfig.Symbol.EQUALS.toChar());
			if(index >= 0 && query.length() > index) {
				key = query.substring(0, index);
				// 不用URL解码：URI已经解码
				value = query.substring(index + 1);
				switch (key) {
					case QUERY_DN -> this.dn(value);
					case QUERY_XL -> this.xl(value);
					case QUERY_XT -> this.xt(value);
					case QUERY_AS -> this.as(value);
					case QUERY_XS -> this.xs(value);
					case QUERY_TR -> this.tr(value);
					default -> LOGGER.debug("磁力链接不支持的参数：{}-{}-{}", key, value, this.url);
				}
			} else {
				LOGGER.debug("磁力链接错误参数：{}-{}", query, this.url);
			}
		}
		if(this.magnet.supportDownload()) {
			return this.magnet;
		}
		throw new DownloadException("磁力链接新建失败：" + this.url);
	}
	
	/**
	 * <p>设置显示名称</p>
	 * 
	 * @param value 显示名称
	 * 
	 * @see Magnet#setDn(String)
	 */
	private void dn(String value) {
		this.magnet.setDn(value);
	}

	/**
	 * <p>设置文件大小</p>
	 * 
	 * @param value 文件大小
	 */
	private void xl(String value) {
		if(StringUtils.isNumeric(value)) {
			this.magnet.setXl(Long.valueOf(value));
		}
	}
	
	/**
	 * <p>设置资源URN</p>
	 * 
	 * @param value 资源URN
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void xt(final String value) throws DownloadException {
		if(StringUtils.isEmpty(value)) {
			return;
		}
		final String prefix = Magnet.Type.BTIH.prefix();
		if(!value.startsWith(prefix)) {
			LOGGER.debug("磁力链接不支持的XT：{}", value);
			return;
		}
		String hash = value.substring(prefix.length());
		// 32位磁力链接转为40位磁力链接
		if(Protocol.Type.verifyMagnetHash32(hash)) {
			hash = InfoHash.newInstance(hash).infoHashHex();
		}
		this.magnet.setXt(value);
		this.magnet.setHash(hash);
		this.magnet.setType(Magnet.Type.BTIH);
	}
	
	/**
	 * <p>设置文件链接</p>
	 * 
	 * @param value 文件链接
	 * 
	 * @see Magnet#setAs(String)
	 */
	private void as(String value) {
		this.magnet.setAs(value);
	}
	
	/**
	 * <p>设置绝对资源</p>
	 * 
	 * @param value 绝对资源
	 * 
	 * @see Magnet#setXs(String)
	 */
	private void xs(String value) {
		this.magnet.setXs(value);
	}
	
	/**
	 * <p>设置Tracker服务器</p>
	 * 
	 * @param value Tracker服务器
	 * 
	 * @see Magnet#addTr(String)
	 */
	private void tr(String value) {
		this.magnet.addTr(value);
	}
	
}
