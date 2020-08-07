package com.acgist.snail;

import org.junit.jupiter.api.Test;

import com.acgist.snail.system.format.XML;

public class XmlTest extends BaseTest {

	@Test
	public void testWrite() {
		XML xml = XML.build();
		var root = xml.element(xml.document(), "root");
		xml.element(root, "test", "1234");
		xml.elementNS(root, "s:test", "1234");
		xml.elementNS(root, "s:test", "1234", "1234");
		this.log(xml.xml(true));
	}
	
}
