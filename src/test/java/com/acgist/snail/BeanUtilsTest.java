package com.acgist.snail;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.BeanUtils;

public class BeanUtilsTest {

	@Test
	public void read() {
		TaskEntity entity = new TaskEntity();
		entity.setId("1234");
		System.out.println(BeanUtils.propertyValue(entity, "id"));
	}
	
	@Test
	public void buildSQL() {
		TaskEntity entity = new TaskEntity();
		entity.setName("测试");
		final String[] properties = BeanUtils.properties(entity.getClass());
		final String sqlProperty = Stream.of(properties)
			.map(property -> "`" + property + "`")
			.collect(Collectors.joining(",", "(", ")"));
		final String sqlValue = Stream.of(properties)
			.map(property -> "?")
			.collect(Collectors.joining(",", "(", ")"));
		final Object[] parameters = Stream.of(properties)
			.map(property -> BeanUtils.propertyValue(entity, property))
			.toArray();
		
		System.out.println(sqlProperty);
		System.out.println(sqlValue);
		System.out.println(Arrays.asList(parameters));
	}
	
	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void enumProperty() throws IntrospectionException {
		PropertyDescriptor descriptor = new PropertyDescriptor("status", TaskEntity.class);
		if(descriptor.getPropertyType().isEnum()) {
			System.out.println(Enum.valueOf(((Class<Enum>) descriptor.getPropertyType()), "pause"));
		}
	}
	
}
