package com.acgist.snail.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class MapUtilsTest extends Performance {

    @Test
    void testMapUtils() {
        assertTrue(MapUtils.isEmpty(Map.of()));
        assertFalse(MapUtils.isEmpty(Map.of("1", "2")));
        assertFalse(MapUtils.isNotEmpty(Map.of()));
        assertTrue(MapUtils.isNotEmpty(Map.of("1", "2")));
    }

    @Test
    void testToUrlQuery() {
        Map<String, String> map = null;
        assertNull(MapUtils.toUrlQuery(map));
        map = Map.of();
        assertNull(MapUtils.toUrlQuery(map));
        map = Map.of("1", "2", "3", "测试");
        assertNotNull(MapUtils.toUrlQuery(map));
        this.log(MapUtils.toUrlQuery(map));
    }
    
    @Test
    void testOfUrlQuery() {
        Map<String, String> map = MapUtils.ofUrlQuery("1=1&2=2&3=3");
        assertNotNull(map);
        map.forEach((v, k) -> assertEquals(v, k));
        map = MapUtils.ofUrlQuery("1=1&2==2&3=3&4");
        assertEquals("1", map.get("1"));
        assertEquals("=2", map.get("2"));
        assertEquals("", map.get("4"));
        assertNull(map.get("5"));
        map = MapUtils.ofUrlQuery("1=1&2=2&3=3&4=");
        assertEquals("1", map.get("1"));
        assertEquals("", map.get("4"));
        assertNull(map.get("5"));
        map = MapUtils.ofUrlQuery("");
        assertNotNull(map);
        assertNull(map.get("5"));
        map = MapUtils.ofUrlQuery("4");
        assertNotNull(map);
        assertEquals("", map.get("4"));
        assertNull(map.get("5"));
    }
    
    @Test
    void testCosted() {
        final Map<String, Object> map = Map.of("map", Map.of("key", "value"));
        assertNotNull(MapUtils.getMap(map, "map"));
        this.costed(100000, () -> MapUtils.getMap(map, "map"));
    }
    
}
