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
		 * <p>点号</p>
		 */
		DOT('.'),
		/**
		 * <p>逗号</p>
		 */
		COMMA(','),
		/**
		 * <p>冒号</p>
		 */
		COLON(':'),
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
		EQUALS_SIGN('='),
		/**
		 * <p>百分号</p>
		 */
		PERCENT_SIGN('%'),
		/**
		 * <p>问号</p>
		 */
		QUESTION_MARK('?'),
		/**
		 * <p>双引号</p>
		 */
		DOUBLE_QUOTE('\"'),
		/**
		 * <p>单引号</p>
		 */
		SINGLE_QUOTE('\''),
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
