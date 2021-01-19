package com.acgist.snail.gui.javafx.window.statistics;

import java.util.BitSet;
import java.util.Objects;
import java.util.function.Consumer;

import com.acgist.snail.gui.javafx.ITheme;
import com.acgist.snail.utils.NumberUtils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * <p>位图工具</p>
 * <p>使用画布实现</p>
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
	 * <p>数据数组</p>
	 */
	private final BitSet[] bitSets;
	/**
	 * <p>颜色数组</p>
	 */
	private final Color[] colors;
	/**
	 * <p>没有填充颜色</p>
	 */
	private final Color noneColor;
	/**
	 * <p>边框颜色</p>
	 */
	private final Color borderColor;
	/**
	 * <p>背景颜色</p>
	 */
	private final Color backgroundColor;
	/**
	 * <p>鼠标位置颜色</p>
	 */
	private final Color mouseColor;
	/**
	 * <p>鼠标选择范围</p>
	 */
	private final BitSet mouseBitSet;
	/**
	 * <p>鼠标选择事件</p>
	 */
	private final Consumer<Integer> mouseSelect;
	/**
	 * <p>鼠标位置</p>
	 */
	private int mouseIndex;
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
		this(bitSet.size(), new BitSet[] { bitSet }, new Color[] { ITheme.COLOR_GREEN });
	}

	/**
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 */
	private CanvasPainter(int length, BitSet[] bitSets, Color[] colors) {
		this(DEFAULT_WH, DEFAULT_COL, length, bitSets, colors);
	}
	
	/**
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 */
	private CanvasPainter(int wh, int col, int length, BitSet[] bitSets, Color[] colors) {
		this(wh, col, length, bitSets, colors, null, null);
	}
	
	/**
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * @param mouseSelect 鼠标选择事件
	 */
	private CanvasPainter(int wh, int col, int length, BitSet[] bitSets, Color[] colors, Consumer<Integer> mouseSelect) {
		this(wh, col, length, bitSets, colors, null, mouseSelect);
	}
	
	/**
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * @param mouseBitSet 鼠标选择范围
	 * @param mouseSelect 鼠标选择事件
	 */
	private CanvasPainter(int wh, int col, int length, BitSet[] bitSets, Color[] colors, BitSet mouseBitSet, Consumer<Integer> mouseSelect) {
		this(
			wh, col, length, bitSets, colors,
			ITheme.COLOR_GRAY,
			Color.BLACK,
			Color.WHITE,
			ITheme.COLOR_RED,
			mouseBitSet,
			mouseSelect
		);
	}
	
	/**
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * @param noneColor 没有填充颜色
	 * @param borderColor 边框颜色
	 * @param backgroundColor 背景颜色
	 * @param mouseColor 鼠标位置颜色
	 * @param mouseBitSet 鼠标选择范围
	 * @param mouseSelect 鼠标选择事件
	 */
	private CanvasPainter(
		int wh, int col, int length,
		BitSet[] bitSets, Color[] colors,
		Color noneColor, Color borderColor, Color backgroundColor,
		Color mouseColor, BitSet mouseBitSet, Consumer<Integer> mouseSelect
	) {
		if(bitSets.length != colors.length) {
			throw new IllegalArgumentException("参数长度错误");
		}
		this.wh = wh;
		this.col = col;
		this.length = length;
		this.row = NumberUtils.ceilDiv(length, col);
		this.bitSets = bitSets;
		this.colors = colors;
		this.noneColor = noneColor;
		this.borderColor = borderColor;
		this.backgroundColor = backgroundColor;
		this.mouseColor = mouseColor;
		this.mouseBitSet = mouseBitSet;
		this.mouseSelect = mouseSelect;
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
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(int length, BitSet[] bitSets, Color[] colors) {
		return new CanvasPainter(length, bitSets, colors);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(int wh, int col, int length, BitSet[] bitSets, Color[] colors) {
		return new CanvasPainter(wh, col, length, bitSets, colors);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * @param mouseSelect 鼠标选择事件
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(int wh, int col, int length, BitSet[] bitSets, Color[] colors, Consumer<Integer> mouseSelect) {
		return new CanvasPainter(wh, col, length, bitSets, colors, mouseSelect);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * @param mouseBitSet 鼠标选择范围
	 * @param mouseSelect 鼠标选择事件
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(int wh, int col, int length, BitSet[] bitSets, Color[] colors, BitSet mouseBitSet, Consumer<Integer> mouseSelect) {
		return new CanvasPainter(wh, col, length, bitSets, colors, mouseBitSet, mouseSelect);
	}
	
	/**
	 * <p>创建工具</p>
	 * 
	 * @param wh 填充高宽
	 * @param col 列数
	 * @param length 数据长度
	 * @param bitSets 数据数组
	 * @param colors 颜色数组
	 * @param noneColor 没有填充颜色
	 * @param borderColor 边框颜色
	 * @param backgroundColor 背景颜色
	 * @param mouseColor 鼠标位置颜色
	 * @param mouseBitSet 鼠标选择范围
	 * @param mouseSelect 鼠标选择事件
	 * 
	 * @return CanvasPainter
	 */
	public static final CanvasPainter newInstance(
		int wh, int col, int length,
		BitSet[] bitSets, Color[] colors,
		Color noneColor, Color borderColor, Color backgroundColor,
		Color mouseColor, BitSet mouseBitSet, Consumer<Integer> mouseSelect
	) {
		return new CanvasPainter(wh, col, length, bitSets, colors, noneColor, borderColor, backgroundColor, mouseColor, mouseBitSet, mouseSelect);
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
	 * @param index 数据数组索引
	 * @param data 数据
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter draw(int index, BitSet data) {
		if(index < 0 || this.bitSets.length <= index || data == null) {
			return this;
		}
		final BitSet oldBitSet = this.bitSets[index];
		oldBitSet.or(data);
		// 没有数据增加
		if(oldBitSet.cardinality() == data.cardinality()) {
			return this;
		}
		this.drawFill();
		return this;
	}
	
	/**
	 * <p>开始画图</p>
	 * 
	 * @param dataIndex 数据数组索引
	 * @param dataIndex 数据索引
	 * 
	 * @return CanvasPainter
	 */
	public CanvasPainter draw(int index, int dataIndex) {
		if(index < 0 || this.bitSets.length <= index || dataIndex < 0) {
			return this;
		}
		final BitSet oldBitSet = this.bitSets[index];
		// 已经包含数据
		if(oldBitSet.get(dataIndex)) {
			return this;
		}
		oldBitSet.set(dataIndex);
		this.graphics.save();
		this.drawFill(dataIndex);
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
			if(this.mouseSelect != null) {
				this.canvas.setOnMouseMoved(event -> this.moved(event.getX(), event.getY()));
				this.canvas.setOnMouseExited(event -> this.exited());
				this.canvas.setOnMouseClicked(event -> this.clicked());
			}
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
		this.graphics.setFill(this.backgroundColor);
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
		BitSet bitSet;
		boolean none = true;
		for (int jndex = 0; jndex < this.bitSets.length; jndex++) {
			bitSet = this.bitSets[jndex];
			if(bitSet.get(index)) {
				none = false;
				this.graphics.setFill(this.colors[jndex]);
				break;
			}
		}
		if(none) {
			this.graphics.setFill(this.noneColor);
		}
		final int row = index / this.col;
		final int col = index % this.col;
		final int wh = this.wh + BORDER_WH;
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
		if(this.mouseIndex == index) {
			// 没有变化
			return;
		}
		final int oldIndex = this.mouseIndex;
		this.graphics.save();
		this.drawFill(oldIndex);
		if(this.mouseSelect(index)) {
			this.mouseIndex = index;
			final int width = col * wh + BORDER_WH;
			final int height = row * wh + BORDER_WH;
			this.graphics.setFill(this.mouseColor);
			this.graphics.fillRect(width, height, this.wh, this.wh);
		} else {
			this.mouseIndex = -1;
		}
		this.graphics.restore();
	};
	
	/**
	 * <p>鼠标退出</p>
	 */
	private void exited() {
		this.graphics.save();
		this.drawFill(this.mouseIndex);
		this.graphics.restore();
		// 删除位置
		this.mouseIndex = -1;
	}
	
	/**
	 * <p>鼠标点击</p>
	 */
	private void clicked() {
		if(
			this.mouseSelect != null &&
			this.mouseSelect(this.mouseIndex)
		) {
			this.mouseSelect.accept(this.mouseIndex);
		}
	};
	
	/**
	 * <p>判断当前位置是否可以选择</p>
	 * 
	 * @param index 索引
	 * 
	 * @return 是否可以选择
	 */
	private boolean mouseSelect(int index) {
		return
			index >= 0  &&
			index < this.length &&
			(this.mouseBitSet == null || this.mouseBitSet.get(index));
	}
	
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
