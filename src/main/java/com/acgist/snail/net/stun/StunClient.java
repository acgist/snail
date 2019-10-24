package com.acgist.snail.net.stun;

/**
 * Stun客户端
 * 
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc3489.txt</p>
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc5389.txt</p>
 * 
 * <p>注：简单的STUN客户端，并没有实现所有的功能。</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public class StunClient {
	
    public static final int TYPE_REQUEST = 0b00;

    public static final int TYPE_INDICATION = 0b01;

    public static final int TYPE_RESPONSE_SUCCESS = 0b10;

    public static final int TYPE_RESPONSE_ERROR = 0b11;

}
