/**
 * <h1>Sanil（蜗牛）</h1>
 * <p>基于Java开发的下载工具，支持下载协议：BT（BitTorrent、磁力链接、种子）、FTP、HTTP。</p>
 * <p>官网地址：https://gitee.com/acgist/snail</p>
 * 
 * TODO：稀有块、阻塞算法（Peer）
 * TODO：代码优化：添加测试、优化注释、日志优化、异常处理
 * 
 * @author acgist
 * @since 1.0.0
 */
open module com.acgist.snail {

	//================导出================//
	exports com.acgist.snail.downloader;
	exports com.acgist.snail.gui;
	exports com.acgist.snail.gui.event;
	exports com.acgist.snail.gui.event.adapter;
	exports com.acgist.snail.net;
	exports com.acgist.snail.net.application;
	exports com.acgist.snail.net.codec;
	exports com.acgist.snail.net.ftp;
	exports com.acgist.snail.net.hls;
	exports com.acgist.snail.net.http;
	exports com.acgist.snail.net.stun;
	exports com.acgist.snail.net.torrent;
	exports com.acgist.snail.net.torrent.bootstrap;
	exports com.acgist.snail.net.torrent.dht.bootstrap;
	exports com.acgist.snail.net.torrent.peer.bootstrap;
	exports com.acgist.snail.net.torrent.tracker.bootstrap;
	exports com.acgist.snail.net.upnp;
	exports com.acgist.snail.net.ws;
	exports com.acgist.snail.pojo;
	exports com.acgist.snail.pojo.bean;
	exports com.acgist.snail.pojo.entity;
	exports com.acgist.snail.pojo.message;
	exports com.acgist.snail.pojo.session;
	exports com.acgist.snail.pojo.wrapper;
	exports com.acgist.snail.system;
	exports com.acgist.snail.system.config;
	exports com.acgist.snail.system.format;
	exports com.acgist.snail.system.context;
	exports com.acgist.snail.system.exception;
	exports com.acgist.snail.protocol;
	exports com.acgist.snail.repository;
	exports com.acgist.snail.utils;
	
	//================Java================//
	requires java.base; // 不能使用transitive修饰
	requires transitive java.sql;
	requires transitive java.xml;
	requires transitive java.naming;
	requires transitive java.logging;
	requires transitive java.net.http;
	requires transitive java.compiler;
	requires transitive java.scripting;
	requires transitive java.instrument;
	requires transitive java.management;
	requires transitive java.transaction.xa;
	
	//================JDK================//
	requires transitive jdk.crypto.ec;
	requires transitive jdk.unsupported;
	
	//================依赖================//
	requires transitive org.slf4j;
	requires transitive com.h2database;
	requires transitive ch.qos.logback.core;
	requires transitive ch.qos.logback.classic;

}