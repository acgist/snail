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
		
		connect(1), // 连接
		download(3); // 下载

		private int score;
		
		Type(int score) {
			this.score = score;
		}
		
		public int score() {
			return this.score;
		}
		
	}
	
	/**
	 * 范围序号：IP（int）/步长（2 ^ 16）
	 */
	private Integer index;
	/**
	 * 计分：连接、下载。
	 */
	private Integer score;
	/**
	 * 是否变化：不记录数据库。
	 */
	private transient Boolean change;

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Boolean isChange() {
		return change;
	}

	public void setChange(Boolean change) {
		this.change = change;
	}

}
