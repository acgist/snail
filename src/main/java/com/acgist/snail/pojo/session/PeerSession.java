package com.acgist.snail.pojo.session;

import com.acgist.snail.system.interfaces.IStatistics;
import com.acgist.snail.utils.ObjectUtils;

/**
 * Peer：保存peer信息，ip、端口、下载统计等
 */
public class PeerSession implements IStatistics {

	private StatisticsSession statistics;

	private String host;
	private Integer port;

	public PeerSession(StatisticsSession parent, String host, Integer port) {
		this.statistics = new StatisticsSession(parent);
		this.host = host;
		this.port = port;
	}

	/**
	 * 判断是否存在
	 */
	public boolean exist(String host, Integer port) {
		return ObjectUtils.equalsBuilder(this.host, this.port)
			.equals(ObjectUtils.equalsBuilder(host, port));
	}
	
	/**
	 * 统计
	 */
	@Override
	public void statistics(long buffer) {
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
