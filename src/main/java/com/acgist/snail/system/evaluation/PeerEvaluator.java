package com.acgist.snail.system.evaluation;

import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.pojo.entity.RangeEntity;
import com.acgist.snail.pojo.session.PeerSession;

/**
 * <p>Peer评估器</p>
 * <p>根据IP地址评估，插入Peer队列的头部还是尾部。</p>
 * <p>将所有IP（2^32个）分为65536（2^16）个区域，然后可以连接和可以下载的均给予评分，然后计算插入Peer队列位置。</p>
 * <p>系统启动时初始化分数，关闭时保存分数，得分=0的记录不保存数据库。</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class PeerEvaluator {

	private boolean available; // 初始完成，可用状态。
	
	private final List<RangeEntity> list;
	
	private static final PeerEvaluator INSTANCE = new PeerEvaluator();
	
	private PeerEvaluator() {
		this.list = new ArrayList<>();
	}
	
	public static final PeerEvaluator getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 初始化：初始数据，加载分数。
	 */
	public void init() {
		this.buildRange();
		this.loadScore();
		this.available = true;
	}

	/**
	 * 判断Peer插入头部还是尾部。
	 * 
	 * @param peerSession Peer
	 * 
	 * @return true：尾部（优先使用）；false：头部；
	 */
	public boolean eval(PeerSession peerSession) {
		if(!this.available) { // 没有初始化直接返回插入头部
			return false;
		}
		return false;
	}

	/**
	 * 计分
	 */
	public void score(PeerSession peerSession, RangeEntity.Type type) {
		if(!this.available) { // 没有初始化不计分
			return;
		}
	}
	
	/**
	 * 记录数据库：只记录分值大于0的数据。
	 */
	public void store() {
		if(!this.available) { // 没有初始化不保存
			return;
		}
	}

	/**
	 * 初始数据
	 */
	private void buildRange() {
		
	}

	/**
	 * 加载分数
	 */
	private void loadScore() {
	}
	
}
