package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.entity.ConfigEntity;
import com.acgist.snail.utils.Performance;

class IntrospectorTest extends Performance {

	@Test
	void testBeans() throws IntrospectionException {
		final var bean = Introspector.getBeanInfo(ConfigEntity.class);
		final var properties = bean.getPropertyDescriptors();
		for (PropertyDescriptor property : properties) {
			this.log(property);
		}
		assertNotNull(bean);
	}
	
}
