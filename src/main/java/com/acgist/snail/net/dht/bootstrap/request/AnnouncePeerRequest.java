package com.acgist.snail.net.dht.bootstrap.request;

import com.acgist.snail.net.dht.bootstrap.DhtService;
import com.acgist.snail.net.dht.bootstrap.Request;
import com.acgist.snail.system.config.DhtConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.manager.NodeManager;

public class AnnouncePeerRequest extends Request {

	private AnnouncePeerRequest() {
		super(DhtService.getInstance().id(), DhtConfig.QType.announce_peer);
		this.put(DhtConfig.KEY_ID, NodeManager.getInstance().nodeId());
	}
	
	public static final AnnouncePeerRequest newRequest(byte[] token, String infoHashHex) {
		final AnnouncePeerRequest request = new AnnouncePeerRequest();
		request.put(DhtConfig.KEY_PORT, SystemConfig.getPeerPort());
		request.put(DhtConfig.KEY_TOKEN, token);
		request.put(DhtConfig.KEY_INFO_HASH, infoHashHex);
		request.put(DhtConfig.KEY_IMPLIED_PORT, 0); // TODO：实现uTP，修改：1
		return request;
	}
	
	public Integer getPort() {
		return getInteger(DhtConfig.KEY_PORT);
	}
	
	public Integer getImpliedPort() {
		return getInteger(DhtConfig.KEY_IMPLIED_PORT);
	}
	
	public String getToken() {
		return getString(DhtConfig.KEY_TOKEN);
	}
	
	public String getInfoHash() {
		return getString(DhtConfig.KEY_INFO_HASH);
	}
	
}
