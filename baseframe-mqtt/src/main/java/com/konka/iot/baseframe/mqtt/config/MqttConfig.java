package com.konka.iot.baseframe.mqtt.config;

/**
 * @author xiexinyuan
 * @version 1.0
 * @createTime 2019-09-24 11:43
 * @Description mqtt配置类
 */

public class MqttConfig {

    // 连接地址
    private String url;
    // 用户名
    private String userName;
    // 密码
    private String passWord;
    // 客户端ID
    private String clientId;
    // 服务质量
    private int qos;
    // 设置会话心跳时间 单位为秒 服务器会每隔(1.5*keepTime)秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
    private int keepAliveInterval;
    // 连接超时时间
    private int connectionTimeout;
    // mqtt 版本
    private int version;
    // 自动重连
    private Boolean automaticReconnect;
    // 设置是否清空session,false表示服务器会保留客户端的连接记录，true表示每次连接到服务器都以新的身份连接
    private Boolean cleanSession;

    public String getUrl( ) {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName( ) {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord( ) {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getClientId( ) {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getQos( ) {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public int getKeepAliveInterval( ) {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    public int getConnectionTimeout( ) {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getVersion( ) {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Boolean getAutomaticReconnect( ) {
        return automaticReconnect;
    }

    public void setAutomaticReconnect(Boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    public Boolean getCleanSession( ) {
        return cleanSession;
    }

    public void setCleanSession(Boolean cleanSession) {
        this.cleanSession = cleanSession;
    }
}
