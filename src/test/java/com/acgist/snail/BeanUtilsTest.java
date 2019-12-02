package com.acgist.snail;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.utils.BeanUtils;

public class BeanUtilsTest extends BaseTest {

	@Test
	public void testRead() {
		TaskEntity entity = new TaskEntity();
		entity.setId("1234");
		this.log(BeanUtils.propertyValue(entity, "id"));
	}
	
	@Test
	public void testBuildSQL() {
		TaskEntity entity = new TaskEntity();
		entity.setName("测试");
		entity.setCreateDate(new Date());
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
		
		this.log("属性名：" + sqlProperty);
		this.log("SQL:" + sqlValue);
		this.log("属性值：" + Arrays.asList(parameters));
	}
	
	@Test
	public void testEnumProperty() throws IntrospectionException {
		PropertyDescriptor descriptor = new PropertyDescriptor("status", TaskEntity.class);
		if(descriptor.getPropertyType().isEnum()) {
			final var enums = descriptor.getPropertyType().getEnumConstants();
			for (Object object : enums) {
				if(object.toString().equals("PAUSE")) {
					this.log(object);
					this.log(object.getClass());
				}
			}
//			this.log(Enum.valueOf(((Class<Enum>) descriptor.getPropertyType()), "PAUSE"));
		}
	}
	
	@Test
	public void testUnpack() {
		final var value = BeanUtils.unpack(FileType.class, "VIDEO");
		this.log(value);
		this.log(value.getClass());
	}
	
}
