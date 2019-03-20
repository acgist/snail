package com.acgist.snail.pojo.session;

import com.acgist.snail.system.interfaces.IStatistical;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Peer：保存peer信息，ip、端口、下载统计等
 */
public class PeerSession implements IStatistical {

	private StatisticsSession statistics;

	private String host;
	private Integer port;

	public PeerSession(StatisticsSession statistics, String host, Integer port) {
		this.statistics = statistics;
		this.host = host;
		this.port = port;
	}

	/**
	 * 统计
	 */
	@Override
	public void statistical(long buffer) {
		statistics.download(buffer);
	}
	
	public StatisticsSession getStatistics() {
		return statistics;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(this.host, this.port);
	}
	
	@Override
	public boolean equals(Object object) {
		if(ObjectUtils.equals(this, object)) {
			return true;
		}
		if(ObjectUtils.equalsClazz(this, object)) {
			PeerSession session = (PeerSession) object;
			return ObjectUtils.equalsBuilder(this.host, this.port)
				.equals(ObjectUtils.equalsBuilder(session.host, session.port));
		}
		return false;
	}
	
}
