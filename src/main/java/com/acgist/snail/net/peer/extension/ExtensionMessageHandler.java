package com.acgist.snail.net.peer.extension;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.net.peer.extension.EMType.Type;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.BCodeBuilder;
import com.acgist.snail.utils.CollectionUtils;

/**
 * http://www.bittorrent.org/beps/bep_0009.html
 * http://www.bittorrent.org/beps/bep_0010.html
 * http://www.bittorrent.org/beps/bep_0011.html
 * https://www.cnblogs.com/LittleHann/p/6180296.html
 */
public class ExtensionMessageHandler {

	/**
	 * 支持的扩展协议
	 */
	private static final List<EMType> SUPPORT_EM_TYPES;
	
	static {
		SUPPORT_EM_TYPES = new ArrayList<>();
		SUPPORT_EM_TYPES.add(EMType.newInstance(Type.ut_pex, 1));
		SUPPORT_EM_TYPES.add(EMType.newInstance(Type.ut_metadata, 2));
	}
	
	/**
	 * 扩展消息
	 */
	public byte[] extension() {
		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("e", 0); // 加密
		final Map<String, Object> mData = new LinkedHashMap<>();
		if(CollectionUtils.isNotEmpty(SUPPORT_EM_TYPES)) {
			for (EMType type : SUPPORT_EM_TYPES) {
				mData.put(type.getType().name(), type.getValue());
			}
		}
		data.put("m", mData); // 扩展协议以及编号
		data.put("metadata_size", mData); // infoHash长度
		data.put("reqq", 100); // TODO：详细意思
		data.put("v", SystemConfig.getNameAndVersion()); // 客户端信息（名称、版本）
//		final String ipAddress = UpnpService.getInstance().externalIpAddress();
//		if(StringUtils.isNotEmpty(ipAddress)) {
//			data.put("p", SystemConfig.getPeerPort()); // 本机监听TCP端口
//			ByteBuffer ipBuffer = ByteBuffer.allocate(4);
//			ipBuffer.putInt(NetUtils.ipToInt(UpnpService.getInstance().externalIpAddress()));
//			data.put("yourip", ipBuffer.array()); // 本机的IP地址
//		}
		BCodeBuilder builder = BCodeBuilder.newInstance();
		return builder.build(data).bytes();
	}

	public void utMetadata() {
	}
	
}
