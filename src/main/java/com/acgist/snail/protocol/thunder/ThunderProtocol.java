package com.acgist.snail.protocol.thunder;

import java.util.Base64;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>迅雷协议</p>
 * <p>转换为其他协议。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ThunderProtocol extends Protocol {

	public static final String THUNDER_REGEX = "thunder://.+";
	
	public static final String THUNDER_PREFIX = "thunder://";
	
	private static final ThunderProtocol INSTANCE = new ThunderProtocol();
	
	private ThunderProtocol() {
		super(Type.thunder, THUNDER_REGEX);
	}
	
	public static final ThunderProtocol getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String name() {
		return "迅雷";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(TaskSession taskSession) {
		return null;
	}
	
	@Override
	protected Protocol convert() throws DownloadException {
		final String url = this.url.substring(THUNDER_PREFIX.length());
		String newUrl = new String(Base64.getMimeDecoder().decode(url)); // getMimeDecoder防止长度非4的整数倍导致的异常
		newUrl = newUrl.substring(2, newUrl.length() - 2);
		return ProtocolManager.getInstance().protocol(newUrl);
	}
	
	@Override
	protected void cleanMessage(boolean ok) {
	}

}
