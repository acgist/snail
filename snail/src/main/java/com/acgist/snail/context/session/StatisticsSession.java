package com.acgist.snail.context.session;

import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.context.IStatisticsSession;

/**
 * <p>统计信息</p>
 * <p>速度、限速、统计等</p>
 * <p>统计下载大小必须统计有效下载数据，因为任务大小获取需要使用。</p>
 * <p>速度统计和速度限制使用实时下载数据，这样数据平滑具有实时性。</p>
 * <p>由于下载大小统计有效数据，但是速度统计可能存在无效数据，所以下载时间预估不会非常准确。</p>
 * <p>无效数据：Piece按照Slice下载和速度统计，但是Piece可能下载失败和验证失败，导致产生无效数据。</p>
 * 
 * @author acgist
 */
public final class StatisticsSession implements IStatisticsSession {

	/**
	 * <p>限速开关</p>
	 */
	private final boolean limit;
	/**
	 * <p>速度统计开关</p>
	 */
	private final boolean speed;
	/**
	 * <p>上级统计信息</p>
	 * <p>统计信息时如果存在上级需要优先更新上级</p>
	 */
	private final IStatisticsSession parent;
	/**
	 * <p>累计上传大小</p>
	 */
	private final AtomicLong uploadSize;
	/**
	 * <p>累计下载大小</p>
	 */
	private final AtomicLong downloadSize;
	/**
	 * <p>上传限速</p>
	 */
	private final LimitSession uploadLimit;
	/**
	 * <p>下载限速</p>
	 */
	private final LimitSession downloadLimit;
	/**
	 * <p>上传速度</p>
	 */
	private final SpeedSession uploadSpeed;
	/**
	 * <p>下载速度</p>
	 */
	private final SpeedSession downloadSpeed;
	
	/**
	 * <p>统计信息</p>
	 */
	public StatisticsSession() {
		this(false, true, null);
	}
	
	/**
	 * <p>统计信息</p>
	 * 
	 * @param limit 是否限速
	 * @param parent 上级统计信息
	 */
	public StatisticsSession(boolean limit, IStatisticsSession parent) {
		this(limit, true, parent);
	}
	
	/**
	 * <p>统计信息</p>
	 * 
	 * @param limit 是否限速
	 * @param speed 是否统计速度
	 * @param parent 上级统计信息
	 */
	public StatisticsSession(boolean limit, boolean speed, IStatisticsSession parent) {
		this.limit = limit;
		this.speed = speed;
		this.parent = parent;
		this.uploadSize = new AtomicLong(0);
		this.downloadSize = new AtomicLong(0);
		if(limit) {
			this.uploadLimit = new LimitSession(LimitSession.Type.UPLOAD);
			this.downloadLimit = new LimitSession(LimitSession.Type.DOWNLOAD);
		} else {
			this.uploadLimit = null;
			this.downloadLimit = null;
		}
		if(speed) {
			this.uploadSpeed = new SpeedSession();
			this.downloadSpeed = new SpeedSession();
		} else {
			this.uploadSpeed = null;
			this.downloadSpeed = null;
		}
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this;
	}

	@Override
	public void upload(int buffer) {
		if(this.parent != null) {
			this.parent.upload(buffer);
		}
		this.uploadSize.addAndGet(buffer);
	}
	
	@Override
	public void download(int buffer) {
		if(this.parent != null) {
			this.parent.download(buffer);
		}
		this.downloadSize.addAndGet(buffer);
	}

	@Override
	public void uploadLimit(int buffer) {
		if(this.parent != null) {
			this.parent.uploadLimit(buffer);
		}
		if(this.limit) {
			this.uploadLimit.limit(buffer);
		}
		if(this.speed) {
			this.uploadSpeed.buffer(buffer);
		}
	}
	
	@Override
	public void downloadLimit(int buffer) {
		if(this.parent != null) {
			this.parent.downloadLimit(buffer);
		}
		if(this.limit) {
			this.downloadLimit.limit(buffer);
		}
		if(this.speed) {
			this.downloadSpeed.buffer(buffer);
		}
	}
	
	@Override
	public long uploadSpeed() {
		if(this.speed) {
			return this.uploadSpeed.speed();
		}
		return 0L;
	}

	@Override
	public long downloadSpeed() {
		if(this.speed) {
			return this.downloadSpeed.speed();
		}
		return 0L;
	}
	
	@Override
	public long uploadSize() {
		return this.uploadSize.get();
	}
	
	@Override
	public void uploadSize(long size) {
		this.uploadSize.set(size);
	}
	
	@Override
	public long downloadSize() {
		return this.downloadSize.get();
	}
	
	@Override
	public void downloadSize(long size) {
		this.downloadSize.set(size);
	}

	@Override
	public void resetUploadSpeed() {
		if(this.speed) {
			this.uploadSpeed.reset();
		}
	}
	
	@Override
	public void resetDownloadSpeed() {
		if(this.speed) {
			this.downloadSpeed.reset();
		}
	}
	
}
