package com.acgist.snail.protocol.thunder;

import java.util.Base64;

import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.ProtocolManager;

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
	protected Protocol convert() throws DownloadException {
		final String url = this.url.substring(THUNDER_PREFIX.length());
		String newUrl = new String(Base64.getDecoder().decode(url));
		newUrl = newUrl.substring(2, newUrl.length() - 2);
		return ProtocolManager.getInstance().protocol(newUrl);
	}

	@Override
	protected boolean buildTaskEntity() throws DownloadException {
		return false;
	}

	@Override
	protected void cleanMessage() {
	}

}
