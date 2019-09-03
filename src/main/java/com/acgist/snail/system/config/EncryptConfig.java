package com.acgist.snail.system.config;

/**
 * <p>加密配置</p>
 * <p>PE、MSE、<del>PHE</del></p>
 * <p>参考链接：http://wiki.bitcomet.com/protocol_encryption</p>
 * <p>参考链接：http://wiki.vuze.com/w/Message_Stream_Encryption</p>
 * <p>参考链接：https://baike.baidu.com/item/BitTorrent协议加密/22911780</p>
 * <p>参考链接：https://en.wikipedia.org/wiki/BitTorrent_protocol_encryption</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public class EncryptConfig {

	/**
	 * 加密策略
	 */
	public enum Strategy {
		
		plaintext, // 文本
		compatible, // 兼容：文本（优先使用）、加密
		enrtypt; // 加密
		
	}
	
	/**
	 * 加密模式
	 */
	public enum CryptoProvide {
		
		plaintext(0x01), // 明文
		rc4(	  0x02); // RC4加密
		
		int value;
		
		CryptoProvide(int value) {
			this.value = value;
		}

	}
	
}
