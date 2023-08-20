package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.entity.TaskEntity;
import com.acgist.snail.utils.Performance;

class IntrospectorTest extends Performance {

    @Test
    void testBeans() throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(TaskEntity.class);
        final PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
        for (final PropertyDescriptor property : properties) {
            this.log(property);
        }
        assertNotNull(beanInfo);
    }
    
}
