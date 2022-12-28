package com.acgist.snail.config;

import java.util.StringJoiner;

/**
 * 符号配置
 * 
 * @author acgist
 */
public final class SymbolConfig {

	/**
	 * 分隔符号保留类型
	 * 
	 * @author acgist
	 */
	public enum FullType {
		
		/**
		 * 过滤
		 */
		FILTER,
		/**
		 * 前缀
		 */
		PREFIX,
		/**
		 * 后缀
		 */
		SUFFIX
		
	}
	
	/**
	 * 符号
	 * 
	 * @author acgist
	 */
	public enum Symbol {
		
		/**
		 * 或者
		 */
		OR('|'),
		/**
		 * 并且
		 */
		AND('&'),
		/**
		 * 点号
		 */
		DOT('.'),
		/**
		 * 零
		 */
		ZERO('0'),
		/**
		 * 加号
		 */
		PLUS('+'),
		/**
		 * 减号
		 */
		MINUS('-'),
		/**
		 * 冒号
		 */
		COLON(':'),
		/**
		 * 逗号
		 */
		COMMA(','),
		/**
		 * 井号
		 */
		POUND('#'),
		/**
		 * 空格
		 */
		SPACE(' '),
		/**
		 * 斜杠
		 */
		SLASH('/'),
		/**
		 * 反斜杠
		 */
		BACKSLASH('\\'),
		/**
		 * 等号
		 */
		EQUALS('='),
		/**
		 * 百分号
		 */
		PERCENT('%'),
		/**
		 * 问号
		 */
		QUESTION('?'),
		/**
		 * 分号
		 */
		SEMICOLON(';'),
		/**
		 * 单引号
		 */
		SINGLE_QUOTE('\''),
		/**
		 * 双引号
		 */
		DOUBLE_QUOTE('"'),
		/**
		 * 回车符
		 */
		CARRIAGE_RETURN('\r'),
		/**
		 * 换行符
		 */
		LINE_SEPARATOR('\n'),
		/**
		 * 左花括号
		 */
		OPEN_BRACE('{'),
		/**
		 * 右花括号
		 */
		CLOSE_BRACE('}'),
		/**
		 * 左方括号
		 */
		OPEN_BRACKET('['),
		/**
		 * 右方括号
		 */
		CLOSE_BRACKET(']'),
		/**
		 * 左圆括号
		 */
		OPEN_PARENTHESIS('('),
		/**
		 * 右圆括号
		 */
		CLOSE_PARENTHESIS(')');
		
		/**
		 * 字符值
		 */
		private final char charValue;
		/**
		 * 字符串值
		 */
		private final String stringValue;
		
		/**
		 * @param value 字符值
		 */
		private Symbol(char value) {
			this.charValue = value;
			this.stringValue = Character.toString(value);
		}
		
		/**
		 * 连接参数字符串
		 * 
		 * @param args 参数
		 * 
		 * @return 字符串
		 */
		public String join(String ... args) {
			if(args == null) {
				return null;
			}
			final StringJoiner joiner = new StringJoiner(this.stringValue);
			for (String object : args) {
				joiner.add(object);
			}
			return joiner.toString();
		}
		
		/**
		 * 连接参数字符串
		 * 
		 * @param args 参数
		 * 
		 * @return 字符串
		 */
		public String join(Object ... args) {
			if(args == null) {
				return null;
			}
			final StringJoiner joiner = new StringJoiner(this.stringValue);
			for (Object object : args) {
				joiner.add(object == null ? null : object.toString());
			}
			return joiner.toString();
		}
		
		/**
		 * 字符串分隔
		 * 
		 * @param source 原始字符串
		 * 
		 * @return 字符串数组
		 */
		public final String[] split(String source) {
			return split(source, FullType.FILTER);
		}
		
		/**
		 * 字符串分隔
		 * 
		 * @param source 原始字符串
		 * @param type 分隔符号保留类型
		 * 
		 * @return 字符串数组
		 */
		public final String[] split(String source, FullType type) {
			if (source == null) {
				return new String[0];
			}
			int size = 0;
			int left = 0;
			int index = 0;
			final int length = this.stringValue.length();
			String[] array = new String[Byte.SIZE];
			do {
				index = source.indexOf(this.stringValue, left);
				if (index < 0) {
					if (FullType.FILTER == type) {
						array[size] = source.substring(left);
					} else if(FullType.PREFIX == type) {
						array[size] = source.substring(left == 0 ? left : left);
					} else {
						array[size] = source.substring(left == 0 ? left : left - length);
					}
				} else {
					if (FullType.FILTER == type) {
						array[size] = source.substring(left, index);
					} else if(FullType.PREFIX == type) {
						array[size] = source.substring(left == 0 ? left : left, index + length);
					} else {
						array[size] = source.substring(left == 0 ? left : left - length, index);
					}
					left = index + length;
				}
				size++;
				if (size >= array.length) {
					final String[] newArray = new String[size + Byte.SIZE];
					System.arraycopy(array, 0, newArray, 0, size);
					array = newArray;
				}
			} while (index >= 0);
			final String[] result = new String[size];
			System.arraycopy(array, 0, result, 0, size);
			return result;
		}
		
		/**
		 * @return 字符值
		 */
		public char toChar() {
			return this.charValue;
		}

		@Override
		public String toString() {
			return this.stringValue;
		}
		
	}
	
	/**
	 * 回车换行符（兼容）
	 * 
	 * @see Symbol#CARRIAGE_RETURN
	 * @see Symbol#LINE_SEPARATOR
	 */
	public static final String LINE_SEPARATOR_COMPAT = Symbol.CARRIAGE_RETURN.toString() + Symbol.LINE_SEPARATOR.toString();
	
	private SymbolConfig() {
	}
	
}
