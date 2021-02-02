package com.acgist.snail.config;

/**
 * <p>符号配置</p>
 * 
 * @author acgist
 */
public final class SymbolConfig {

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
		 * <>零</p>
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
		CARRIAGE_RETURN('\r');
		
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
