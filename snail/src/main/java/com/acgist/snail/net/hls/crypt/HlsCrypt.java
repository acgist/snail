package com.acgist.snail.net.hls.crypt;

/**
 * <p>HLS加密工具</p>
 * 
 * @author acgist
 * @version 1.5.0
 */
public abstract class HlsCrypt {
	
	protected HlsCrypt() {
	}

	/**
	 * <p>加密协议</p>
	 */
	public enum Type {
		
		NONE("NONE"),
		AES_128("AES-128"),
		SAMPLE_AES("SAMPLE-AES");
		
		/**
		 * <p>加密算法名称</p>
		 */
		private final String value;
		
		private Type(String value) {
			this.value = value;
		}
		
		/**
		 * <p>通过加密算法名称获取加密协议</p>
		 * 
		 * @param value 加密算法名称
		 * 
		 * @return 加密协议
		 */
		public static final Type of(String value) {
			final Type[] types = Type.values();
			for (Type type : types) {
				if(type.value.equals(value)) {
					return type;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * <p>解密数据</p>
	 * 
	 * @param source 加密数据
	 * 
	 * @return 原始数据
	 */
	public abstract byte[] decrypt(byte[] source);
	
}
