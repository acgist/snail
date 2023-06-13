package com.acgist.snail.context.wrapper;

import java.net.URI;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * URI包装器
 * URI里面数据已经缓存不需要自己再做缓存，含有RAW方法没有解码，不含RAW方法已经解码。
 * 
 * @author acgist
 */
public final class URIWrapper {

    /**
     * 链接地址
     */
    private String uri;
    /**
     * URI
     */
    private URI nativeUri;
    /**
     * 默认端口
     */
    private int defaultPort;
    /**
     * 默认用户
     */
    private String defaultUser;
    /**
     * 默认密码
     */
    private String defaultPassword;
    /**
     * 端口
     */
    private int port;
    /**
     * 账号
     */
    private String user;
    /**
     * 密码
     */
    private String password;
    
    /**
     * @param uri 链接地址
     */
    private URIWrapper(String uri) {
        this.uri = uri;
    }
    
    /**
     * @param uri         链接地址
     * @param defaultPort 默认端口
     */
    private URIWrapper(String uri, int defaultPort) {
        this.uri         = uri;
        this.defaultPort = defaultPort;
    }
    
    /**
     * @param uri             链接地址
     * @param defaultPort     默认端口
     * @param defaultUser     默认用户
     * @param defaultPassword 默认密码
     */
    private URIWrapper(String uri, int defaultPort, String defaultUser, String defaultPassword) {
        this.uri             = uri;
        this.defaultPort     = defaultPort;
        this.defaultUser     = defaultUser;
        this.defaultPassword = defaultPassword;
    }
    
    /**
     * @param uri 链接地址
     * 
     * @return {@link URIWrapper}
     */
    public static final URIWrapper newInstance(String uri) {
        return new URIWrapper(uri);
    }
    
    /**
     * @param uri         链接地址
     * @param defaultPort 默认端口
     * 
     * @return {@link URIWrapper}
     */
    public static final URIWrapper newInstance(String uri, int defaultPort) {
        return new URIWrapper(uri, defaultPort);
    }
    
    /**
     * @param uri             链接地址
     * @param defaultPort     默认端口
     * @param defaultUser     默认用户
     * @param defaultPassword 默认密码
     * 
     * @return {@link URIWrapper}
     */
    public static final URIWrapper newInstance(String uri, int defaultPort, String defaultUser, String defaultPassword) {
        return new URIWrapper(uri, defaultPort, defaultUser, defaultPassword);
    }
    
    /**
     * 解析链接地址
     * 
     * @return this
     */
    public URIWrapper decode() {
        // 不能解码：包含空格新建URI抛出异常
        this.nativeUri = URI.create(this.uri);
        // 解析端口
        this.decodePort();
        // 解析用户信息
        this.decodeUserInfo();
        return this;
    }

    /**
     * 解析端口
     */
    private void decodePort() {
        final int port = this.nativeUri.getPort();
        this.port = port < 0 ? this.defaultPort : port;
    }

    /**
     * 解析用户信息
     */
    private void decodeUserInfo() {
        final String userInfo    = this.nativeUri.getUserInfo();
        final String[] userInfos = SymbolConfig.Symbol.COLON.split(userInfo);
        if(userInfos.length <= 0) {
            this.user     = this.defaultUser;
            this.password = this.defaultPassword;
        } else if(userInfos.length == 1) {
            this.user     = userInfos[0];
            this.password = this.defaultPassword;
        } else {
            this.user     = userInfos[0];
            this.password = userInfos[1];
        }
    }
    
    /**
     * @return 协议
     */
    public String getScheme() {
        return this.nativeUri.getScheme();
    }
    
    /**
     * @return 地址和端口
     */
    public String getAuthority() {
        return this.nativeUri.getAuthority();
    }
    
    /**
     * @return 地址
     */
    public String getHost() {
        return this.nativeUri.getHost();
    }
    
    /**
     * @return 端口
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * @return 用户
     */
    public String getUser() {
        return this.user;
    }
    
    /**
     * @return 密码
     */
    public String password() {
        return this.password;
    }
    
    /**
     * @return 路径
     */
    public String getPath() {
        return this.nativeUri.getPath();
    }
    
    /**
     * @return 参数
     */
    public String getQuery() {
        return this.nativeUri.getQuery();
    }
    
    /**
     * @return 参数
     */
    public String[] getQuerys() {
        // https://www.acgist.com/snail?a=a&b=b
        final String query = this.getQuery();
        if(StringUtils.isNotEmpty(query)) {
            return SymbolConfig.Symbol.AND.split(query);
        }
        // magnet:?a=a&b=b
        final String schemeSpecificPart = this.getSchemeSpecificPart();
        if(StringUtils.isNotEmpty(schemeSpecificPart)) {
            final int index = schemeSpecificPart.indexOf(SymbolConfig.Symbol.QUESTION.toChar());
            final int queryIndex = index < 0 ? 0 : index + 1;
            return SymbolConfig.Symbol.AND.split(schemeSpecificPart.substring(queryIndex));
        }
        return new String[0];
    }
    
    /**
     * @return 标识
     */
    public String getFragment() {
        return this.nativeUri.getFragment();
    }
    
    /**
     * @return 特殊部分
     */
    public String getSchemeSpecificPart() {
        return this.nativeUri.getSchemeSpecificPart();
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.uri);
    }
    
}