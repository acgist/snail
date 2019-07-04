package com.acgist.snail.pojo.entity;

/**
 * Entity - IP范围
 * 
 * @author acgist
 * @since 1.1.0
 */
public class RangeEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "tb_range";
	
	/**
	 * 计分类型
	 */
	public enum Type {
		
		connect, // 连接
		download; // 下载
		
	}
	
	/**
	 * 起始地址，结束范围=下一个范围开始（begin + 65536）。
	 */
	private int begin;
	/**
	 * 计分：连接、下载。
	 */
	private int score;
	/**
	 * 是否变化：不记录数据库。
	 */
	private transient boolean change;

	public void setBegin(int begin) {
		this.begin = begin;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getBegin() {
		return begin;
	}

	public boolean isChange() {
		return change;
	}

	public void setChange(boolean change) {
		this.change = change;
	}
	
}
