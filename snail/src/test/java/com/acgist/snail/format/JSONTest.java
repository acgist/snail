package com.acgist.snail.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.utils.Performance;

class JSONTest extends Performance {
    
    private static final char[] CHARS = new char[] {
        '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005',
        '\u0006', '\u0007', '\b', '\t', '\n', '\u000b', '\f', '\r',
        '\u000e', '\u000f', '\u0010', '\u0011', '\u0012', '\u0013',
        '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019',
        '\u001a', '\u001b', '\u001c', '\u001d', '\u001e', '\u001f',
        '\"', '\\'
    };

    @Test
    void testChars() {
        boolean match = false;
        for (char value : CHARS) {
            this.log(Integer.toHexString(value));
            if(value > 0x1F && value != 0x22 && value != 0x5C) {
                match = true;
                break;
            }
        }
        assertFalse(match);
    }
    
    @Test
    void testJSON() {
        final String escape = new String(CHARS);
        final var like = List.of("动\"漫", "洞\\箫");
        final var list = List.of(List.of(1, 2), 2, 3);
        final Map<Object, Object> map = new HashMap<>(Map.of(
            "age", 30L,
            "money", 100.100D,
            "like", like,
            "list", list,
            "name", "\b你,:{}\"好",
            "marry", true,
            "escape", escape,
            "json-a", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":[\"动漫\",\"洞箫\"]}",
            "json-b", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":\"[\"动漫\",\"洞箫\"]\"}"
        ));
        map.put("wife", null);
        final JSON mapJson = JSON.ofMap(map);
        this.log(mapJson.toJSON());
        final JSON json = JSON.ofString(mapJson.toJSON());
        assertEquals(mapJson.get("age"), map.get("age"));
        assertEquals(mapJson.get("money"), map.get("money"));
        assertEquals(mapJson.get("name"), map.get("name"));
        assertEquals(mapJson.get("wife"), map.get("wife"));
        assertEquals(mapJson.get("marry"), map.get("marry"));
        assertEquals(mapJson.get("escape"), map.get("escape"));
        assertEquals(json.get("age"), map.get("age"));
        assertEquals(json.get("money"), map.get("money"));
        assertEquals(json.get("name"), map.get("name"));
        assertEquals(json.get("wife"), map.get("wife"));
        assertEquals(json.get("marry"), map.get("marry"));
        assertEquals(json.get("escape"), map.get("escape"));
        assertEquals(json.getJSON("like").getList().toString().replace(" ", ""), map.get("like").toString().replace(" ", ""));
        assertEquals(json.getJSON("list").getList().toString().replace(" ", ""), map.get("list").toString().replace(" ", ""));
//      JSON.eager();
        this.log(json.getJSON("like"));
        this.log(json.getJSON("list"));
        this.log(json.getJSON("json-a"));
        this.log(json.getJSON("json-a").get("like"));
        this.log(json.getJSON("json-a").getJSON("like"));
        assertEquals(2L, json.getJSON("list").get(1));
        assertEquals(String.class, json.getJSON("json-a").get("like").getClass());
        assertEquals(JSON.class, json.getJSON("json-a").getJSON("like").getClass());
        this.log(json.getJSON("json-b"));
        this.log(json.getJSON("json-b").get("like"));
        this.log(json.getJSON("json-b").getJSON("like"));
        assertEquals(String.class, json.getJSON("json-b").get("like").getClass());
        assertEquals(JSON.class, json.getJSON("json-b").getJSON("like").getClass());
        JSON.eager(); // 禁用懒加载
        this.log(json.getJSON("json-a"));
        this.log(json.getJSON("json-a").get("like"));
        this.log(json.getJSON("json-a").getJSON("like"));
        assertEquals(JSON.class, json.getJSON("json-a").get("like").getClass());
        assertEquals(JSON.class, json.getJSON("json-a").getJSON("like").getClass());
        this.log("json-b");
        this.log(json.getJSON("json-b"));
        this.log(json.getJSON("json-b").get("like"));
        this.log(json.getJSON("json-b").getJSON("like"));
        assertEquals(String.class, json.getJSON("json-b").get("like").getClass());
        assertEquals(JSON.class, json.getJSON("json-b").getJSON("like").getClass());
    }
    
    @Test
    void testEscape() {
//      {"name":"{\"name\":\"\\\"\"}"}
        final String name = "\"";
        final Map<Object, Object> a = Map.of("name", name);
        final Map<Object, Object> b = Map.of("name", JSON.ofMap(a).toJSON());
        final String content = JSON.ofMap(b).toJSON();
        this.log(content);
        assertEquals("{\"name\":\"{\\\"name\\\":\\\"\\\\\\\"\\\"}\"}", content);
        final JSON json = JSON.ofString(content);
        assertEquals(name, json.getJSON("name").getString("name"));
    }
    
    @Test
    void testCosted() {
        LoggerConfig.off();
        final var like = List.of("动\"漫", "洞\\箫");
        final var list = List.of(List.of(1, 2), 2, 3);
        final Map<Object, Object> map = new HashMap<>(Map.of(
            "age", 30,
            "like", like,
            "list", list,
            "name", "\b你,:{}\"好",
            "marry", true,
            "json-a", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":[\"动漫\",\"洞箫\"]}",
            "json-b", "{\"name\":\"你,:{}好\",\"age\":30,\"marry\":true,\"wife\":null,\"like\":\"[\"动漫\",\"洞箫\"]\"}"
        ));
        map.put("wife", null);
//      final Map<Object, Object> map = Map.of("name", "acgist");
        final String json = JSON.ofMap(map).toJSON();
        // FastJSON：550毫秒
        long costed = this.costed(100000, () -> {
            JSON.ofMap(map).toJSON();
        });
        assertTrue(costed < 3000);
        // FastJSON：550毫秒
        costed = this.costed(100000, () -> {
            JSON.ofString(json);
        });
        assertTrue(costed < 3000);
    }
    
}
