package com.acgist.snail.gui.javafx.window.statistics;

import java.util.BitSet;
import java.util.Objects;

import com.acgist.snail.utils.NumberUtils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * <p>位图工具</p>
 * <p>使用画布实现</p>
 * 
 * TODO：观察者实时更新
 * 
 * @author acgist
 */
public final class CanvasPainter {
	
	/**
	 * <p>默认填充高宽：{@value}</p>
	 */
	private static final int DEFAULT_WH = 16;
	/**
	 * <p>默认列数：{@value}</p>
	 */
	private static final int DEFAULT_COL = 50;
	/**
	 * <p>边框高宽：{@value}</p>
	 */
	private static final int BORDER_WH = 1;
	
	/**
	 * <p>填充高宽</p>
	 */
	private final int wh;
	/**
	 * <p>列数</p>
	 */
	private final int col;
	/**
	 * <p>行数</p>
	 */
	private final int row;
	/**
	 * <p>数据长度</p>
	 */
	private final int length;
	/**
	 * <p>数据</p>
	 */
	private final BitSet bitSet;
	/**
	 * <p>选择数据</p>
	 */
	private final BitSet selectBitSet;
	/**
	 * <p>存在填充颜色</p>
	 */
	private final Color fillColor;
	/**
	 * <p>选择填充颜色</p>
	 */
	private final Color selectColor;
	/**
	 * <p>没有填充颜色</p>
	 */
	private final Color noneColor;
	/**
	 * <p>鼠标位置颜色</p>
	 */
	private final Color mouseColor;
	/**
	 * <p>边框颜色</p>
	 */
	private final Color borderColor;
	/**
	 * <p>背景颜色</p>
	 */
	private final Color background;
	/**
	 * <p>鼠标位置</p>
	 */
	private int index;
	/**
	 * <p>图片宽度</p>
	 */
	private int width;
	/**
	 * <p>图片高度</p>
	 */
	private int height;
	/**
	 * <p>画布</p>
	 */
	private Canvas canvas;
	/**
	 * <p>画笔</p>
	 */
	private GraphicsContext graphics;
	
	/**
	 * @param bitSet 数据
	 */
	private CanvasPainter(BitSet bitSet) {
		this(bitSet.size(), bitSet, bitSet);
	}

	/**
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param selectBitSet 选择数据
	 */
	private CanvasPainter(int length, BitSet bitSet, BitSet selectBitSet) {
		this(DEFAULT_WH, DEFAULT_COL, length, bitSet, selectBitSet);
	}
	
	/**
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param selectBitSet 选择数据
	 */
	private CanvasPainter(int wh, int col, int length, BitSet bitSet, BitSet selectBitSet) {
		this(
			wh, col, length, bitSet, selectBitSet,
			Color.rgb(0, 153, 204),
			Color.rgb(255, 232, 159),
			Color.rgb(200, 200, 200),
			Color.rgb(220, 78, 65),
			Color.BLACK, Color.WHITE
		);
	}
	
	/**
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param selectBitSet 选择数据
	 * @param fillColor 存在填充颜色
	 * @param selectColor 选择填充颜色
	 * @param noneColor 没有填充颜色
	 * @param noneColor 鼠标位置颜色
	 * @param borderColor 边框颜色
	 * @param background 背景颜色
	 */
	private CanvasPainter(
		int wh, int col,
		int length, BitSet bitSet, BitSet selectBitSet,
		Color fillColor, Color selectColor, Color noneColor,
		Color mouseColor, Color borderColor, Color background
	) {
		this.wh = wh;
		this.col = col;
		this.row = NumberUtils.ceilDiv(length, col);
		this.fillColor = fillColor;
		this.selectColor = selectColor;
		this.noneColor = noneColor;
		this.mouseColor = mouseColor;
		this.borderColor = borderColor;
		this.background = background;
		this.length = length;
		this.bitSet = bitSet;
		this.selectBitSet = selectBitSet;
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param bitSet 数据
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(BitSet bitSet) {
		return new CanvasPainter(bitSet);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param selectBitSet 选择数据
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(int length, BitSet bitSet, BitSet selectBitSet) {
		return new CanvasPainter(length, bitSet, selectBitSet);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param selectBitSet 选择数据
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(int wh, int col, int length, BitSet bitSet, BitSet selectBitSet) {
		return new CanvasPainter(wh, col, length, bitSet, selectBitSet);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSet 数据
	 * @param selectBitSet 选择数据
	 * @param fillColor 存在填充颜色
	 * @param selectColor 选择填充颜色
	 * @param noneColor 没有填充颜色
	 * @param noneColor 鼠标位置颜色
	 * @param borderColor 边框颜色
	 * @param background 背景颜色
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(
		int wh, int col,
		int length, BitSet bitSet, BitSet selectBitSet,
		Color fillColor, Color selectColor, Color noneColor,
		 Color mouseColor, Color borderColor, Color background
	) {
		return new CanvasPainter(
			wh, col,
			length, bitSet, selectBitSet,
			fillColor, selectColor, noneColor,
			mouseColor, borderColor, background
		);
	}

	/**
	 * <p>开始画图</p>
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter draw() {
		this.drawFill();
		return this;
	}
	
	/**
	 * <p>开始画图</p>
	 * 
	 * @param bitSet 数据
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter draw(BitSet bitSet) {
		this.bitSet.or(bitSet);
		// 没有数据增加
		if(this.bitSet.cardinality() == bitSet.cardinality()) {
			return this;
		}
		this.drawFill();
		return this;
	}
	
	/**
	 * <p>开始画图</p>
	 * 
	 * @param index 数据索引
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter draw(int index) {
		// 已经包含数据
		if(this.bitSet.get(index)) {
			return this;
		}
		this.bitSet.set(index);
		this.graphics.save();
		this.drawFill(index);
		this.graphics.restore();
		return this;
	}
	
	/**
	 * <p>创建画布、画笔，画出背景和边框。</p>
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter build() {
		return this.build(null);
	}
	
	/**
	 * <p>创建画布、画笔，画出背景和边框。</p>
	 * 
	 * @param canvas 画布
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter build(Canvas canvas) {
		if(canvas == null) {
			// 计算高宽
			this.width = this.col * (this.wh + BORDER_WH) + BORDER_WH; // 列数 * (宽度 + 边框) + 右边框
			this.height = this.row * (this.wh + BORDER_WH) + BORDER_WH; // 行数 * (高度 + 边框) + 底边框
			// 创建画布
			this.canvas = new Canvas(this.width, this.height);
			this.canvas.setOnMouseMoved(event -> this.moved(event.getX(), event.getY()));
			this.canvas.setOnMouseExited(event -> this.exited());
			this.canvas.setOnMouseClicked(event -> this.clicked());
		} else {
			this.canvas = canvas;
		}
		// 创建画笔
		this.graphics = this.canvas.getGraphicsContext2D();
		this.drawBackground();
		this.drawBorder();
		return this;
	}

	/**
	 * <p>背景</p>
	 */
	private void drawBackground() {
		this.graphics.save();
		// 清空背景
		this.graphics.clearRect(0, 0, this.width, this.height);
		// 背景
		this.graphics.setFill(this.background);
		this.graphics.fillRect(0, 0, this.width, this.height);
		this.graphics.restore();
	}
	
	/**
	 * <p>边框</p>
	 */
	private void drawBorder() {
		this.graphics.save();
		this.graphics.setStroke(this.borderColor);
		this.graphics.setLineWidth(BORDER_WH);
		// 列
		final int width = this.wh + BORDER_WH;
		int x = 0;
		for (int index = 0; index < this.col; index++) {
			x = index * width;
			this.graphics.strokeLine(x, 0, x, this.height);
		}
		// 右边框
		final int right = this.width - BORDER_WH;
		this.graphics.strokeLine(right, 0, right, this.height);
		// 行
		final int height = this.wh + BORDER_WH;
		int y = 0;
		for (int index = 0; index < this.row; index++) {
			y = index * height;
			this.graphics.strokeLine(0, y, this.width, y);
		}
		// 底边框
		final int bottom = this.height - BORDER_WH;
		this.graphics.strokeLine(0, bottom, this.width, bottom);
		this.graphics.restore();
	}
	
	/**
	 * <p>填充数据</p>
	 */
	private void drawFill() {
		this.graphics.save();
		for (int index = 0; index < this.length; index++) {
			this.drawFill(index);
		}
		this.graphics.restore();
	}
	
	/**
	 * <p>填充数据</p>
	 * 
	 * @param index 数据索引
	 */
	private void drawFill(int index) {
		if(index < 0) {
			return;
		}
		final int row = index / this.col;
		final int col = index % this.col;
		final int wh = this.wh + BORDER_WH;
		if(this.bitSet.get(index)) {
			this.graphics.setFill(this.fillColor);
		} else if(this.selectBitSet.get(index)) {
			this.graphics.setFill(this.selectColor);
		} else {
			this.graphics.setFill(this.noneColor);
		}
		final int width = col * wh + BORDER_WH;
		final int height = row * wh + BORDER_WH;
		this.graphics.fillRect(width, height, this.wh, this.wh);
	}

	/**
	 * <p>鼠标移动</p>
	 * 
	 * @param x x
	 * @param y y 
	 */
	private void moved(double x, double y) {
		final int wh = this.wh + BORDER_WH;
		final int col = (int) (x / wh);
		final int row = (int) (y / wh);
		final int index = row * this.col + col;
		if(this.index == index) {
			// 没有变化
			return;
		}
		final int oldIndex = this.index;
		this.graphics.save();
		this.drawFill(oldIndex);
		if(this.selectBitSet.get(index)) {
			this.index = index;
			final int width = col * wh + BORDER_WH;
			final int height = row * wh + BORDER_WH;
			this.graphics.setFill(this.mouseColor);
			this.graphics.fillRect(width, height, this.wh, this.wh);
		} else {
			this.index = -1;
		}
		this.graphics.restore();
	};
	
	/**
	 * <p>鼠标退出</p>
	 */
	private void exited() {
		this.graphics.save();
		this.drawFill(this.index);
		this.graphics.restore();
		// 删除位置
		this.index = -1;
	}
	
	/**
	 * <p>鼠标点击</p>
	 */
	private void clicked() {
		if(
			this.index >= 0 &&
			// 没有下载
			!this.bitSet.get(this.index) &&
			// 选择下载
			this.selectBitSet.get(this.index)
		) {
			StatisticsWindow.getInstance().piecePos(this.index);
		}
	};
	
	/**
	 * <p>获取画布</p>
	 * 
	 * @return 画布
	 */
	public Canvas canvas() {
		Objects.requireNonNull(this.canvas, "没有创建画布");
		return this.canvas;
	}
	
}
