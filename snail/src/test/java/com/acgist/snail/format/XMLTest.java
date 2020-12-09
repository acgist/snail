package com.acgist.snail.format;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

public class XMLTest extends Performance {

	@Test
	public void testBuild() {
		XML xml = XML.build();
		var root = xml.element(xml.document(), "root");
		xml.element(root, "test", "1234");
		xml.elementNS(root, "s:test", "1234");
		var element = xml.elementNS(root, "s:test", "1234", "1234");
		element.setAttribute("color", "#FFF");
		this.log(xml.xml(true));
		XML readXml = XML.load(xml.xml(true));
		this.log(readXml.elementValue("root"));
		this.log(readXml.elementValue("test"));
		this.log(readXml.elementValues("s:test"));
	}
	
}
