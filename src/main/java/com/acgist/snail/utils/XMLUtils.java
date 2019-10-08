package com.acgist.snail.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.acgist.snail.system.config.SystemConfig;

/**
 * <p>XML工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class XMLUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);

	/**
	 * 格式化输出
	 */
	private static final String DOM_FORMAT_PRETTY_PRINT = "format-pretty-print";
	
	/**
	 * 根节点
	 */
	private Document document;
	
	/**
	 * 创建XML
	 */
	public static final XMLUtils build() {
		final XMLUtils utils = new XMLUtils();
		final DocumentBuilderFactory factory = buildFactory();
		try {
			utils.document = factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			LOGGER.error("创建XML异常", e);
		}
		return utils;
	}
	
	/**
	 * 解析XML
	 */
	public static final XMLUtils load(String xml) {
		final XMLUtils utils = new XMLUtils();
		final DocumentBuilderFactory factory = buildFactory();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			utils.document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGGER.info("解析XML异常：{}", xml, e);
		}
		return utils;
	}
	
	/**
	 * 创建XML工厂
	 */
	private static final DocumentBuilderFactory buildFactory() {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// 防止实体注入
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
		} catch (ParserConfigurationException e) {
			LOGGER.error("创建XML工厂异常 ", e);
		}
		return factory;
	}

	/**
	 * 获取节点
	 */
	public Document document() {
		return this.document;
	}
	
	/**
	 * 创建标签
	 */
	public Element element(Node node, String name) {
		return element(node, name, null);
	}
	
	/**
	 * 创建标签
	 */
	public Element element(Node node, String name, String text) {
		return elementNS(node, name, text, null);
	}
	
	/**
	 * 创建标签（命名空间）
	 */
	public Element elementNS(Node node, String name, String namespaceURI) {
		return elementNS(node, name, null, namespaceURI);
	}
	
	/**
	 * 创建标签（命名空间）
	 */
	public Element elementNS(Node node, String name, String text, String namespaceURI) {
		Element element = null;
		if(StringUtils.isEmpty(namespaceURI)) {
			element = this.document.createElement(name);
		} else {
			element = this.document.createElementNS(namespaceURI, name);
		}
		if(StringUtils.isNotEmpty(text)) {
			element.setTextContent(text);
		}
		node.appendChild(element);
		return element;
	}

	/**
	 * 读取节点值：多个节点返回第一个
	 */
	public String elementValue(String name) {
		final NodeList list = this.document.getElementsByTagName(name);
		if(list.getLength() == 0) {
			return null;
		}
		return list.item(0).getTextContent();
	}

	/**
	 * 读取节点值：多个节点返回全部
	 */
	public List<String> elementValues(String name) {
		final NodeList list = this.document.getElementsByTagName(name);
		final int length = list.getLength();
		if(length == 0) {
			return List.of();
		}
		final List<String> values = new ArrayList<>(length);
		for (int index = 0; index < length; index++) {
			values.add(list.item(index).getTextContent());
		}
		return values;
	}
	
	/**
	 * 输出XML（不格式化）
	 */
	public String xml() {
		return xml(false);
	}
	
	/**
	 * 输出XML
	 * 
	 * @param format 是否格式化
	 */
	public String xml(boolean format) {
		try {
			final Writer writer = new StringWriter();
			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS ementation = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSOutput output = ementation.createLSOutput();
			final LSSerializer serializer = ementation.createLSSerializer();
			output.setEncoding(SystemConfig.DEFAULT_CHARSET);
			output.setCharacterStream(writer);
			if(format) {
				final DOMConfiguration configuration = serializer.getDomConfig();
				if (configuration.canSetParameter(DOM_FORMAT_PRETTY_PRINT, true)) {
					configuration.setParameter(DOM_FORMAT_PRETTY_PRINT, true);
				}
			}
			serializer.write(this.document, output);
			return writer.toString();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			LOGGER.error("输出XML异常", e);
		}
		return null;
	}

}
