/**
 * Sanil（蜗牛）
 * 基于Java开发的下载工具，支持下载协议：BT（BitTorrent、磁力链接、种子文件）、HLS（M3U8）、FTP、HTTP。
 * 
 * @author acgist
 */
open module com.acgist.snail {

    exports com.acgist.snail;
    exports com.acgist.snail.config;
    exports com.acgist.snail.context;
    exports com.acgist.snail.context.entity;
    exports com.acgist.snail.context.session;
    exports com.acgist.snail.context.wrapper;
    exports com.acgist.snail.downloader;
    exports com.acgist.snail.downloader.ftp;
    exports com.acgist.snail.downloader.hls;
    exports com.acgist.snail.downloader.http;
    exports com.acgist.snail.downloader.magnet;
    exports com.acgist.snail.downloader.torrent;
    exports com.acgist.snail.format;
    exports com.acgist.snail.gui;
    exports com.acgist.snail.gui.event;
    exports com.acgist.snail.gui.event.adapter;
    exports com.acgist.snail.gui.recycle;
    exports com.acgist.snail.logger;
    exports com.acgist.snail.net;
    exports com.acgist.snail.net.application;
    exports com.acgist.snail.net.codec;
    exports com.acgist.snail.net.ftp;
    exports com.acgist.snail.net.hls;
    exports com.acgist.snail.net.http;
    exports com.acgist.snail.net.stun;
    exports com.acgist.snail.net.torrent;
    exports com.acgist.snail.net.torrent.codec;
    exports com.acgist.snail.net.torrent.dht;
    exports com.acgist.snail.net.torrent.lsd;
    exports com.acgist.snail.net.torrent.peer;
    exports com.acgist.snail.net.torrent.tracker;
    exports com.acgist.snail.net.torrent.utp;
    exports com.acgist.snail.net.upnp;
    exports com.acgist.snail.protocol;
    exports com.acgist.snail.protocol.ftp;
    exports com.acgist.snail.protocol.hls;
    exports com.acgist.snail.protocol.http;
    exports com.acgist.snail.protocol.magnet;
    exports com.acgist.snail.protocol.thunder;
    exports com.acgist.snail.protocol.torrent;
    exports com.acgist.snail.utils;
    
    requires java.base;
    requires transitive java.xml;

}