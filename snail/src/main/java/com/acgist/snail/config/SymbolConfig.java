package com.acgist.snail.config;

import java.util.StringJoiner;

/**
 * <p>符号配置</p>
 * 
 * @author acgist
 */
public final class SymbolConfig {

	/**
	 * <p>分隔符号保留类型</p>
	 * 
	 * @author acgist
	 */
	public enum FullType {
		
		/**
		 * <p>过滤</p>
		 */
		FILTER,
		/**
		 * <p>前缀</p>
		 */
		PREFIX,
		/**
		 * <p>前缀</p>
		 */
		SUFFIX
		
	}
	
	/**
	 * <p>符号</p>
	 * 
	 * @author acgist
	 */
	public enum Symbol {
		
		/**
		 * <p>或者</p>
		 */
		OR('|'),
		/**
		 * <p>并且</p>
		 */
		AND('&'),
		/**
		 * <p>点号</p>
		 */
		DOT('.'),
		/**
		 * <p>零</p>
		 */
		ZERO('0'),
		/**
		 * <p>加号</p>
		 */
		PLUS('+'),
		/**
		 * <p>减号</p>
		 */
		MINUS('-'),
		/**
		 * <p>逗号</p>
		 */
		COMMA(','),
		/**
		 * <p>井号</p>
		 */
		POUND('#'),
		/**
		 * <p>冒号</p>
		 */
		COLON(':'),
		/**
		 * <p>空格</p>
		 */
		SPACE(' '),
		/**
		 * <p>斜杠</p>
		 */
		SLASH('/'),
		/**
		 * <p>反斜杠</p>
		 */
		BACKSLASH('\\'),
		/**
		 * <p>等号</p>
		 */
		EQUALS('='),
		/**
		 * <p>百分号</p>
		 */
		PERCENT('%'),
		/**
		 * <p>问号</p>
		 */
		QUESTION('?'),
		/**
		 * <p>分号</p>
		 */
		SEMICOLON(';'),
		/**
		 * <p>单引号</p>
		 */
		SINGLE_QUOTE('\''),
		/**
		 * <p>双引号</p>
		 */
		DOUBLE_QUOTE('\"'),
		/**
		 * <p>换行符</p>
		 */
		LINE_SEPARATOR('\n'),
		/**
		 * <p>回车符</p>
		 */
		CARRIAGE_RETURN('\r'),
		/**
		 * <p>左花括号</p>
		 */
		OPEN_BRACE('{'),
		/**
		 * <p>右花括号</p>
		 */
		CLOSE_BRACE('}'),
		/**
		 * <p>左方括号</p>
		 */
		OPEN_BRACKET('['),
		/**
		 * <p>右方括号</p>
		 */
		CLOSE_BRACKET(']'),
		/**
		 * <p>左圆括号</p>
		 */
		OPEN_PARENTHESIS('('),
		/**
		 * <p>右圆括号</p>
		 */
		CLOSE_PARENTHESIS(')');
		
		/**
		 * <p>字符值</p>
		 */
		private final char charValue;
		/**
		 * <p>字符串值</p>
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
		 * <p>连接参数字符串</p>
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
		 * <p>连接参数字符串</p>
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
		 * <p>获取字符值</p>
		 * 
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
	 * <p>回车换行符（兼容）</p>
	 * 
	 * @see Symbol#CARRIAGE_RETURN
	 * @see Symbol#LINE_SEPARATOR
	 */
	public static final String LINE_SEPARATOR_COMPAT = Symbol.CARRIAGE_RETURN.toString() + Symbol.LINE_SEPARATOR.toString();
	
	private SymbolConfig() {
	}
	
}
