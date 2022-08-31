package com.acgist.snail.pojo;

import java.util.List;

import javax.crypto.Cipher;

import com.acgist.snail.utils.BeanUtils;

/**
 * <p>M3U8信息</p>
 * 
 * @author acgist
 */
public final class M3u8 {
	
	/**
	 * <p>类型</p>
	 * 
	 * @author acgist
	 */
	public enum Type {
		
		/**
		 * <p>文件列表</p>
		 */
		FILE,
		/**
		 * <p>M3U8列表</p>
		 */
		M3U8,
		/**
		 * <p>流媒体列表</p>
		 */
		STREAM;
		
	}
	
	/**
	 * <p>加密协议</p>
	 * 
	 * @author acgist
	 */
	public enum Protocol {
		
		/**
		 * <p>明文</p>
		 */
		NONE("NONE"),
		/**
		 * <p>AES-128</p>
		 */
		AES_128("AES-128"),
		/**
		 * <p>SAMPLE-AES</p>
		 */
		SAMPLE_AES("SAMPLE-AES");
		
		/**
		 * <p>加密算法名称</p>
		 */
		private final String value;
		
		/**
		 * @param value 加密算法名称
		 */
		private Protocol(String value) {
			this.value = value;
		}
		
		/**
		 * <p>通过加密算法名称获取加密协议</p>
		 * 
		 * @param value 加密算法名称
		 * 
		 * @return 加密协议
		 */
		public static final Protocol of(String value) {
			final Protocol[] protocols = Protocol.values();
			for (Protocol protocol : protocols) {
				if(protocol.value.equals(value)) {
					return protocol;
				}
			}
			return null;
		}
		
	}
	
	/**
	 * <p>类型</p>
	 */
	private final Type type;
	/**
	 * <p>加密套件</p>
	 * <p>为空：明文</p>
	 */
	private final Cipher cipher;
	/**
	 * <p>文件列表</p>
	 * <p>多级M3U8列表：按照码率从小到大排序</p>
	 */
	private final List<String> links;
	
	/**
	 * @param type 类型
	 * @param cipher 加密套件
	 * @param links 文件列表
	 */
	public M3u8(Type type, Cipher cipher, List<String> links) {
		this.type = type;
		this.cipher = cipher;
		this.links = links;
	}
	
	/**
	 * <p>获取类型</p>
	 * 
	 * @return 获取类型
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * <p>获取加密套件</p>
	 * 
	 * @return 加密套件
	 */
	public Cipher getCipher() {
		return this.cipher;
	}
	
	/**
	 * <p>获取文件列表</p>
	 * 
	 * @return 文件列表
	 */
	public List<String> getLinks() {
		return this.links;
	}
	
	/**
	 * <p>获取码率最大的链接</p>
	 * 
	 * @return 码率最大的链接
	 */
	public String maxRateLink() {
		return this.links.get(this.links.size() - 1);
	}

	@Override
	public String toString() {
		return BeanUtils.toString(this, this.type, this.cipher, this.links);
	}
	
}