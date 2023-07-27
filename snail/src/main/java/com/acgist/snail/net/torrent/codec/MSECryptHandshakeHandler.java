package com.acgist.snail.net.torrent.codec;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

import com.acgist.snail.config.CryptConfig;
import com.acgist.snail.config.CryptConfig.CryptAlgo;
import com.acgist.snail.config.CryptConfig.Strategy;
import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.torrent.InfoHash;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.net.torrent.codec.MSEKeyPairBuilder.MSEPrivateKey;
import com.acgist.snail.net.torrent.peer.PeerSubMessageHandler;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.DigestUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * MSE握手代理
 * 加密算法：ARC4
 * 密钥交换算法：DH（Diffie-Hellman）
 * 协议链接：https://wiki.vuze.com/w/Message_Stream_Encryption
 * 协议链接：https://wiki.openssl.org/index.php/Diffie_Hellman
 * 
 * 步骤：
 * <pre>
 * 1 A->B: Diffie Hellman Ya, PadA
 * 2 B->A: Diffie Hellman Yb, PadB
 * 3 A->B:
 *     HASH('req1', S),
 *     HASH('req2', SKEY) xor HASH('req3', S),
 *     ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
 *     ENCRYPT(IA)
 * 4 B->A:
 *     ENCRYPT(VC, crypto_select, len(padD), padD),
 *     ENCRYPT2(Payload Stream)
 * 5 A->B: ENCRYPT2(Payload Stream)
 * 
 * SKEY：InfoHash
 * </pre>
 * 
 * @author acgist
 */
public final class MSECryptHandshakeHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MSECryptHandshakeHandler.class);

    /**
     * 缓冲区大小：{@value}
     */
    private static final int BUFFER_LENGTH = 4 * SystemConfig.ONE_KB;
    /**
     * req1
     */
    private static final byte[] REQ1 = "req1".getBytes();
    /**
     * req2
     */
    private static final byte[] REQ2 = "req2".getBytes();
    /**
     * req3
     */
    private static final byte[] REQ3 = "req3".getBytes();
    
    /**
     * 加密握手步骤
     * 
     * @author acgist
     */
    public enum Step {
        
        /**
         * 发送公钥
         */
        SEND_PUBLIC_KEY,
        /**
         * 接收公钥
         */
        RECEIVE_PUBLIC_KEY,
        /**
         * 发送加密协议协商
         */
        SEND_PROVIDE,
        /**
         * 接收加密协议协商
         */
        RECEIVE_PROVIDE,
        /**
         * 接收加密协议协商Padding
         */
        RECEIVE_PROVIDE_PADDING,
        /**
         * 发送确认加密协议
         */
        SEND_CONFIRM,
        /**
         * 接收确认加密协议
         */
        RECEIVE_CONFIRM,
        /**
         * 接收确认加密协议Padding
         */
        RECEIVE_CONFIRM_PADDING;
        
    }
    
    /**
     * 当前步骤
     * 默认：接收公钥
     * 
     * @see Step
     */
    private Step step = Step.RECEIVE_PUBLIC_KEY;
    /**
     * 是否加密
     * 默认：明文
     */
    private volatile boolean crypt = false;
    /**
     * 握手是否完成
     */
    private volatile boolean completed = false;
    /**
     * 加密握手锁
     */
    private final Object handshakeLock = new Object();
    /**
     * 加密套件
     */
    private MSECipher cipher;
    /**
     * VC加密套件
     */
    private MSECipher cipherVC;
    /**
     * 密钥对
     */
    private KeyPair keyPair;
    /**
     * 加密策略
     */
    private Strategy strategy;
    /**
     * 数据缓冲
     */
    private ByteBuffer buffer;
    /**
     * S：DH Secret
     */
    private BigInteger dhSecret;
    /**
     * Padding数据同步工具
     */
    private MSEPaddingSync msePaddingSync;
    /**
     * Peer消息代理
     */
    private final PeerSubMessageHandler peerSubMessageHandler;
    /**
     * Peer消息处理器
     */
    private final PeerUnpackMessageCodec peerUnpackMessageCodec;

    /**
     * @param peerUnpackMessageCodec Peer消息处理器
     * @param peerSubMessageHandler  Peer消息代理
     */
    private MSECryptHandshakeHandler(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
        final MSEKeyPairBuilder mseKeyPairBuilder = MSEKeyPairBuilder.newInstance();
        this.buffer  = ByteBuffer.allocate(BUFFER_LENGTH);
        this.keyPair = mseKeyPairBuilder.buildKeyPair();
        this.peerSubMessageHandler  = peerSubMessageHandler;
        this.peerUnpackMessageCodec = peerUnpackMessageCodec;
    }

    /**
     * 新建加密代理
     * 
     * @param peerUnpackMessageCodec Peer消息处理器
     * @param peerSubMessageHandler  Peer消息代理
     * 
     * @return 加密代理
     */
    public static final MSECryptHandshakeHandler newInstance(PeerUnpackMessageCodec peerUnpackMessageCodec, PeerSubMessageHandler peerSubMessageHandler) {
        return new MSECryptHandshakeHandler(peerUnpackMessageCodec, peerSubMessageHandler);
    }
    
    /**
     * 设置明文
     */
    public void plaintext() {
        this.completed(false);
    }
    
    /**
     * 判断握手是否完成
     * 
     * @return 是否完成
     */
    public boolean completed() {
        return this.completed;
    }
    
    /**
     * 发送握手消息
     */
    public void handshake() {
        this.step = Step.SEND_PUBLIC_KEY;
        this.sendPublicKey();
    }

    /**
     * 处理握手消息
     * 
     * @param message 握手消息
     * 
     * @throws NetException 网络异常
     */
    public void handshake(ByteBuffer message) throws NetException {
        try {
            if(this.checkPlaintextPeerHandshake(message)) {
                LOGGER.debug("跳过加密握手：收到Peer明文握手消息");
                this.peerUnpackMessageCodec.decode(message);
                return;
            }
            synchronized (this.buffer) {
                switch (this.step) {
                case SEND_PUBLIC_KEY:
                case RECEIVE_PUBLIC_KEY:
                    this.buffer.put(message);
                    this.receivePublicKey();
                    break;
                case RECEIVE_PROVIDE:
                    this.buffer.put(message);
                    this.receiveProvide();
                    break;
                case RECEIVE_PROVIDE_PADDING:
                    this.cipher.decrypt(message);
                    this.buffer.put(message);
                    this.receiveProvidePadding();
                    break;
                case RECEIVE_CONFIRM:
                    this.buffer.put(message);
                    this.receiveConfirm();
                    break;
                case RECEIVE_CONFIRM_PADDING:
                    this.cipher.decrypt(message);
                    this.buffer.put(message);
                    this.receiveConfirmPadding();
                    break;
                default:
                    LOGGER.warn("加密握手失败（步骤未适配）：{}", this.step);
                    break;
                }
            }
        } catch (NetException e) {
            this.plaintext();
            throw e;
        } catch (Exception e) {
            this.plaintext();
            throw new NetException("加密握手失败", e);
        }
    }
    
    /**
     * 判断是否可用
     * 
     * @return 是否可用
     * 
     * @see PeerSubMessageHandler#available()
     */
    public boolean available() {
        return this.peerSubMessageHandler.available();
    }
    
    /**
     * 判断是否需要加密
     * 
     * @return 是否需要加密
     * 
     * @see PeerSubMessageHandler#needEncrypt()
     */
    public boolean needEncrypt() {
        return this.peerSubMessageHandler.needEncrypt();
    }
    
    /**
     * 数据加密
     * 
     * @param buffer 数据
     */
    public void encrypt(ByteBuffer buffer) {
        if(this.crypt) {
            this.cipher.encrypt(buffer);
        }
    }
    
    /**
     * 数据解密
     * 
     * @param buffer 数据
     */
    public void decrypt(ByteBuffer buffer) {
        if(this.crypt) {
            this.cipher.decrypt(buffer);
        }
    }
    
    /**
     * 添加加密握手锁
     */
    public void lockHandshake() {
        if(!this.completed) {
            synchronized (this.handshakeLock) {
                if(!this.completed) {
                    try {
                        // 加密握手超时时间不能超过Peer连接超时时间
                        this.handshakeLock.wait(SystemConfig.CONNECT_TIMEOUT_MILLIS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.debug("线程等待异常", e);
                    }
                }
            }
        }
        // 加密没有完成设置明文
        if(!this.completed) {
            LOGGER.debug("加密握手失败：使用明文");
            this.plaintext();
        }
    }
    
    /**
     * 释放加密握手锁
     */
    private void unlockHandshake() {
        synchronized (this.handshakeLock) {
            this.handshakeLock.notifyAll();
        }
    }

    /**
     * 发送公钥
     * 
     * <pre>
     * A->B: Diffie Hellman Ya, PadA
     * B->A: Diffie Hellman Yb, PadB
     * </pre>
     */
    private void sendPublicKey() {
        LOGGER.debug("加密握手（发送公钥）步骤：{}", this.step);
        final byte[] publicKey   = this.keyPair.getPublic().getEncoded();
        final byte[] padding     = this.buildPadding(CryptConfig.PADDING_MAX_LENGTH);
        final ByteBuffer message = ByteBuffer.allocate(publicKey.length + padding.length);
        message.put(publicKey);
        message.put(padding);
        this.peerSubMessageHandler.send(message);
    }

    /**
     * 公钥消息最小长度：{@value}
     */
    private static final int PUBLIC_KEY_MIN_LENGTH = CryptConfig.PUBLIC_KEY_LENGTH;
    /**
     * 公钥消息最大长度：{@value}
     */
    private static final int PUBLIC_KEY_MAX_LENGTH = PUBLIC_KEY_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
    
    /**
     * 接收公钥
     * 
     * @throws NetException 网络异常
     */
    private void receivePublicKey() throws NetException {
        LOGGER.debug("加密握手（接收公钥）步骤：{}", this.step);
        if(this.buffer.position() < PUBLIC_KEY_MIN_LENGTH) {
            return;
        }
        if(this.buffer.position() > PUBLIC_KEY_MAX_LENGTH) {
            throw new NetException("加密握手失败（公钥长度错误）");
        }
//      Diffie Hellman Ya, PadA
//      Diffie Hellman Yb, PadB
        this.buffer.flip();
        final BigInteger publicKey = NumberUtils.decodeBigInteger(this.buffer, CryptConfig.PUBLIC_KEY_LENGTH);
        this.buffer.compact();
        this.dhSecret = ((MSEPrivateKey) this.keyPair.getPrivate()).buildDHSecret(publicKey);
        if(this.step == Step.RECEIVE_PUBLIC_KEY) {
            // 客户端接收连接
            this.sendPublicKey();
            this.step = Step.RECEIVE_PROVIDE;
        } else if(this.step == Step.SEND_PUBLIC_KEY) {
            // 客户端发起连接
            this.sendProvide();
            this.step = Step.RECEIVE_CONFIRM;
        } else {
            LOGGER.warn("加密握手失败（接收公钥未知步骤）：{}", this.step);
        }
    }
    
    /**
     * 发送加密协议协商
     * 
     * <pre>
     * HASH('req1', S),
     * HASH('req2', SKEY) xor HASH('req3', S),
     * ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA)),
     * ENCRYPT(IA)
     * </pre>
     */
    private void sendProvide() throws NetException {
        LOGGER.debug("加密握手（发送加密协议协商）步骤：{}", this.step);
        final TorrentSession torrentSession = this.peerSubMessageHandler.torrentSession();
        if(torrentSession == null) {
            throw new NetException("加密握手失败（种子信息不存在）");
        }
        final byte[] dhSecretBytes = NumberUtils.encodeBigInteger(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
        final InfoHash infoHash    = torrentSession.infoHash();
        this.cipher        = MSECipher.newSender(dhSecretBytes, infoHash);
        this.cipherVC      = MSECipher.newSender(dhSecretBytes, infoHash);
        ByteBuffer message = ByteBuffer.allocate(40);
        final MessageDigest digest = DigestUtils.sha1();
//      HASH('req1', S)
        digest.update(REQ1);
        digest.update(dhSecretBytes);
        message.put(digest.digest());
//      HASH('req2', SKEY) xor HASH('req3', S)
        digest.reset();
        digest.update(REQ2);
        digest.update(infoHash.infoHash());
        final byte[] req2 = digest.digest();
        digest.reset();
        digest.update(REQ3);
        digest.update(dhSecretBytes);
        final byte[] req3 = digest.digest();
        message.put(ArrayUtils.xor(req2, req3));
        this.peerSubMessageHandler.send(message);
//      ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
        final byte[] padding    = this.buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
        final int paddingLength = padding.length;
        message = ByteBuffer.allocate(16 + paddingLength);
        message.put(CryptConfig.VC);
        message.putInt(CryptConfig.STRATEGY.getProvide());
        message.putShort((short) paddingLength);
        message.put(padding);
        message.putShort((short) 0);
//      ENCRYPT(IA)
//      没有IA数据
        this.cipher.encrypt(message);
        this.peerSubMessageHandler.send(message);
    }

    /**
     * 加密协商消息最小长度：{@value}
     */
    private static final int PROVIDE_MIN_LENGTH = 20 + 20 + 8 + 4 + 2 + 0 + 2 + 0;
    /**
     * 加密协商消息最大长度：{@value}
     */
    private static final int PROVIDE_MAX_LENGTH = PROVIDE_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
    
    /**
     * 接收加密协议协商
     * 
     * @throws NetException 网络异常
     */
    private void receiveProvide() throws NetException {
        LOGGER.debug("加密握手（接收加密协议协商）步骤：{}", this.step);
        final byte[] dhSecretBytes = NumberUtils.encodeBigInteger(this.dhSecret, CryptConfig.PUBLIC_KEY_LENGTH);
        final MessageDigest digest = DigestUtils.sha1();
//      HASH('req1', S)
        digest.update(REQ1);
        digest.update(dhSecretBytes);
        final byte[] req1 = digest.digest();
        // 匹配数据
        if(!this.match(req1)) {
            return;
        }
        if(this.buffer.position() < PROVIDE_MIN_LENGTH) {
            return;
        }
        if(this.buffer.position() > PROVIDE_MAX_LENGTH) {
            throw new NetException("加密握手失败（加密协商长度错误）");
        }
        this.buffer.flip();
        final byte[] req1Peer = new byte[20];
        this.buffer.get(req1Peer);
        // 数据匹配成功不再验证req1和req1Peer是否相等
//      HASH('req2', SKEY) xor HASH('req3', S)
        final byte[] req2x3Peer = new byte[20];
        this.buffer.get(req2x3Peer);
        digest.reset();
        digest.update(REQ3);
        digest.update(dhSecretBytes);
        final byte[] req3 = digest.digest();
        InfoHash infoHash = null;
        // 获取种子信息
        for (InfoHash infoHashMatch : TorrentContext.getInstance().allInfoHash()) {
            digest.reset();
            digest.update(REQ2);
            digest.update(infoHashMatch.infoHash());
            final byte[] req2 = digest.digest();
            if (Arrays.equals(ArrayUtils.xor(req2, req3), req2x3Peer)) {
                infoHash = infoHashMatch;
                break;
            }
        }
        if(infoHash == null) {
            throw new NetException("加密握手失败（种子信息不存在）");
        }
        this.cipher = MSECipher.newRecver(dhSecretBytes, infoHash);
//      ENCRYPT(VC, crypto_provide, len(PadC), PadC, len(IA))
//      ENCRYPT(IA)
        this.buffer.compact();
        this.buffer.flip();
        this.cipher.decrypt(this.buffer);
        final byte[] vc = new byte[CryptConfig.VC_LENGTH];
        this.buffer.get(vc);
        final int provide = this.buffer.getInt();
        this.strategy     = this.selectStrategy(provide);
        this.step         = Step.RECEIVE_PROVIDE_PADDING;
        LOGGER.debug("加密握手（接收加密协议协商-确认加密协议）：{} - {}", provide, this.strategy);
        // PadC IA
        this.msePaddingSync = MSEPaddingSync.newInstance(2);
        this.receiveProvidePadding();
    }
    
    /**
     * 接收加密协议协商Padding
     * 
     * @throws PacketSizeException 数据包大小异常
     */
    private void receiveProvidePadding() throws PacketSizeException {
        LOGGER.debug("加密握手（接收加密协议协商Padding）步骤：{}", this.step);
        final boolean success = this.msePaddingSync.sync(this.buffer);
        if(success) {
            LOGGER.debug("加密握手（接收加密协议协商Padding）：{}", this.msePaddingSync);
            this.sendConfirm();
        }
    }
    
    /**
     * 发送确认加密协议
     * 
     * <pre>
     * ENCRYPT(VC, crypto_select, len(padD), padD),
     * ENCRYPT2(Payload Stream)
     * </pre>
     */
    private void sendConfirm() {
        LOGGER.debug("加密握手（发送确认加密协议）步骤：{}", this.step);
        final byte[] padding     = this.buildZeroPadding(CryptConfig.PADDING_MAX_LENGTH);
        final int paddingLength  = padding.length;
        final ByteBuffer message = ByteBuffer.allocate(14 + paddingLength);
        message.put(CryptConfig.VC);
        message.putInt(this.strategy.getProvide());
        message.putShort((short) paddingLength);
        message.put(padding);
        this.cipher.encrypt(message);
        this.peerSubMessageHandler.send(message);
        this.completed(this.strategy.getCrypt());
    }
    
    /**
     * 确认加密消息最小长度：{@value}
     */
    private static final int CONFIRM_MIN_LENGTH = 8 + 4 + 2 + 0;
    /**
     * 确认加密消息最大长度：{@value}
     */
    private static final int CONFIRM_MAX_LENGTH = CONFIRM_MIN_LENGTH + CryptConfig.PADDING_MAX_LENGTH;
    
    /**
     * 接收确认加密协议
     * 
     * @throws NetException 网络异常
     */
    private void receiveConfirm() throws NetException {
        LOGGER.debug("加密握手（接收确认加密协议）步骤：{}", this.step);
        // 相同数据发送方加密和接收方解密结果是一样的
        final byte[] vcMatch = this.cipherVC.decrypt(CryptConfig.VC);
        if(!this.match(vcMatch)) {
            return;
        }
        if(this.buffer.position() < CONFIRM_MIN_LENGTH) {
            return;
        }
        if(this.buffer.position() > CONFIRM_MAX_LENGTH) {
            throw new NetException("加密握手失败（确认加密长度错误）");
        }
//      ENCRYPT(VC, crypto_select, len(padD), padD)
        this.buffer.flip();
        this.cipher.decrypt(this.buffer);
        final byte[] vc = new byte[CryptConfig.VC_LENGTH];
        this.buffer.get(vc);
        final int provide = this.buffer.getInt();
        this.strategy     = this.selectStrategy(provide);
        this.step         = Step.RECEIVE_CONFIRM_PADDING;
        LOGGER.debug("加密握手（接收确认加密协议-确认加密协议）：{} - {}", provide, this.strategy);
        this.msePaddingSync = MSEPaddingSync.newInstance(1);
        this.receiveConfirmPadding();
    }
    
    /**
     * 接收确认加密协议Padding
     * 
     * @throws PacketSizeException 网络包大小异常
     */
    private void receiveConfirmPadding() throws PacketSizeException {
        LOGGER.debug("加密握手（接收确认加密协议Padding）步骤：{}", this.step);
        final boolean success = this.msePaddingSync.sync(this.buffer);
        if(success) {
            LOGGER.debug("加密握手（接收确认加密协议Padding）：{}", this.msePaddingSync);
            this.completed(this.strategy.getCrypt());
        }
    }
    
    /**
     * 选择加密策略
     * 协商：根据接入客户端和本地加密策略综合选择加密策略
     * 
     * @param provide 加密协商
     * 
     * @return 加密策略
     */
    private CryptConfig.Strategy selectStrategy(int provide) throws NetException {
        // 客户端是否支持明文
        final boolean plaintext = (provide & CryptAlgo.PLAINTEXT.getProvide()) == CryptAlgo.PLAINTEXT.getProvide();
        // 客户端是否支持加密
        final boolean crypt     = (provide & CryptAlgo.ARC4.getProvide())      == CryptAlgo.ARC4.getProvide();
        Strategy selected = null;
        if (plaintext || crypt) {
            // 本地策略
            selected = switch (CryptConfig.STRATEGY) {
                case PLAINTEXT        -> plaintext ? Strategy.PLAINTEXT : null;
                case PREFER_PLAINTEXT -> plaintext ? Strategy.PLAINTEXT : Strategy.ENCRYPT;
                case PREFER_ENCRYPT   -> crypt ? Strategy.ENCRYPT : Strategy.PLAINTEXT;
                case ENCRYPT          -> crypt ? Strategy.ENCRYPT : null;
                default               -> CryptConfig.STRATEGY.getCrypt() ? Strategy.ENCRYPT : Strategy.PLAINTEXT;
            };
        }
        if (selected == null) {
            throw new NetException("加密握手失败（未知加密协商）：" + provide);
        }
        return selected;
    }
    
    /**
     * 填充：随机值
     * 
     * @param maxLength 最大填充长度
     * 
     * @return 填充数据
     */
    private byte[] buildPadding(int maxLength) {
        return ArrayUtils.random(this.randomLength(maxLength));
    }
    
    /**
     * 填充：0
     * 
     * @param maxLength 最大填充长度
     * 
     * @return 填充数据
     */
    private byte[] buildZeroPadding(int maxLength) {
        return new byte[this.randomLength(maxLength)];
    }
    
    /**
     * @param maxLength 最大填充长度
     * 
     * @return 随机填充长度
     */
    private int randomLength(int maxLength) {
        final Random random = NumberUtils.random();
        return random.nextInt(maxLength + 1);
    }
    
    /**
     * 判断是否是明文Peer握手消息
     * 
     * @param message 消息
     * 
     * @return 是否是明文Peer握手消息
     */
    private boolean checkPlaintextPeerHandshake(ByteBuffer message) {
        final byte first = message.get();
        // 判断首个字符
        if(
            first               == PeerConfig.PROTOCOL_NAME_LENGTH &&
            message.remaining() >= PeerConfig.PROTOCOL_NAME_LENGTH
        ) {
            // 判断协议名称
            final byte[] name = new byte[PeerConfig.PROTOCOL_NAME_LENGTH];
            message.get(name);
            if(Arrays.equals(name, PeerConfig.PROTOCOL_NAME_BYTES)) {
                // 握手消息直接使用明文
                this.plaintext();
                // 重置长度
                message.rewind();
                return true;
            }
        }
        // 重置长度
        message.rewind();
        return false;
    }
    
    /**
     * 判断数据是否匹配成功
     * 
     * @param bytes 匹配数据
     * 
     * @return 是否匹配成功
     */
    private boolean match(byte[] bytes) {
        final int length = bytes.length;
        this.buffer.flip();
        if(this.buffer.remaining() < length) {
            this.buffer.compact();
            return false;
        }
        int index = 0;
        while(length > index) {
            if(this.buffer.get() != bytes[index]) {
                // 最开始的位置移动一位继续匹配
                this.buffer.position(this.buffer.position() - index);
                // 注意重置索引位置
                index = 0;
                if(this.buffer.remaining() < length) {
                    // 剩余数据不足跳出：防止丢弃匹配数据
                    break;
                }
            } else {
                index++;
            }
        }
        if(index == length) {
            // 丢弃填充数据
            this.buffer.position(this.buffer.position() - length);
            this.buffer.compact();
            return true;
        } else {
            // 丢弃填充数据
            this.buffer.compact();
            return false;
        }
    }
    
    /**
     * 设置握手完成
     * 
     * @param crypt 是否加密
     */
    private void completed(boolean crypt) {
        LOGGER.debug("加密握手完成：{}", crypt);
        this.crypt          = crypt;
        this.completed      = true;
        this.buffer         = null;
        this.keyPair        = null;
        this.strategy       = null;
        this.dhSecret       = null;
        this.cipherVC       = null;
        this.msePaddingSync = null;
        this.unlockHandshake();
    }

}
