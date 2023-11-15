package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.WeakHashMap;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.context.entity.TaskEntity;

class PropertyDescriptorTest extends Performance {

    @Test
    void testGetter() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String id = "1234";
        final TaskEntity task = new TaskEntity();
        task.setId(id);
        assertEquals(id, PropertyDescriptor.newInstance(task).get("id"));
        assertEquals(task.hashCode(), PropertyDescriptor.newInstance(task).get("hashCode"));
    }
    
    @Test
    void testSetter() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String id = "1234";
        final TaskEntity task = new TaskEntity();
        assertNull(task.getId());
        PropertyDescriptor.newInstance(task).set("id", id);
        assertEquals(id, task.getId());
    }
    
    @Test
    void testEnumProperty() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        boolean find = false;
        final TaskEntity task = new TaskEntity();
        task.setStatus(Status.AWAIT);
        final PropertyDescriptor descriptor = PropertyDescriptor.newInstance(task);
        if(descriptor.getPropertyType("status").isEnum()) {
            final Object[] enums = descriptor.getPropertyType("status").getEnumConstants();
            for (final Object object : enums) {
                if(object == descriptor.get("status")) {
                    find = true;
                    break;
                }
            }
        }
        assertTrue(find);
    }

    @Test
    void testWeakMap() {
        TaskEntity task = new TaskEntity();
        final Map<TaskEntity, Map<Integer, Integer>> taskMap = new WeakHashMap<>();
        taskMap.put(task, Map.of(1, 2));
        this.log("{}", taskMap);
        task = null;
        System.gc();
        this.log("{}", taskMap);
        Class<TaskEntity> clazz = TaskEntity.class;
        final Map<Class<?>, Map<Integer, Integer>> clazzMap = new WeakHashMap<>();
        clazzMap.put(clazz, Map.of(1, 2));
        this.log("{}", clazzMap);
        clazz = null;
        System.gc();
        this.log("{}", clazzMap);
    }
    
    @Test
    void testCosted() {
        final TaskEntity task = new TaskEntity();
        final PropertyDescriptor descriptor = PropertyDescriptor.newInstance(task);
        this.costed(100000, () -> descriptor.get("id"));
        this.costed(100000, () -> descriptor.set("id", "1234"));
        assertNotNull(task);
        this.costed(1000, () -> {
            final PropertyDescriptor costed = PropertyDescriptor.newInstance(new TaskEntity());
            costed.get("id");
            costed.set("id", "1234");
//          System.gc();
        });
        System.gc();
    }
    
}
