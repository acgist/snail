package com.acgist.snail.logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class TupleTest {

    @Test
    void testTuple() {
        Tuple tuple = new Tuple("1234");
        assertEquals("1234", tuple.format("a", "b"));
        tuple = new Tuple("1234{}4321{}");
        assertEquals("1234a4321{}", tuple.format("a"));
        assertEquals("1234a4321b", tuple.format("a", "b"));
        assertEquals("1234a4321b", tuple.format("a", "b", "c"));
        tuple = new Tuple("{}1234{}4321{}");
        assertEquals("a1234{}4321{}", tuple.format("a"));
        assertEquals("a1234b4321{}", tuple.format("a", "b"));
        assertEquals("a1234b4321c", tuple.format("a", "b", "c"));
        tuple = new Tuple("{}1234{}4321");
        assertEquals("a1234{}4321", tuple.format("a"));
        assertEquals("a1234b4321", tuple.format("a", "b"));
        assertEquals("a1234b4321", tuple.format("a", "b", "c"));
        tuple = new Tuple("{}1234{}{}4321");
        assertEquals("a1234{}{}4321", tuple.format("a"));
        assertEquals("a1234b{}4321", tuple.format("a", "b"));
        assertEquals("a1234bc4321", tuple.format("a", "b", "c"));
        assertEquals("a1234b14321", tuple.format("a", "b", 1));
        assertEquals("a1234btrue4321", tuple.format("a", "b", true));
        assertEquals("a1234bc4321", tuple.format("a", "b", "c", "4"));
        for (int index = 0; index < 100; index++) {
            tuple = new Tuple("{}".repeat(index));
            assertEquals(
                IntStream.range(0, index).boxed().map(String::valueOf).collect(Collectors.joining()),
                tuple.format(IntStream.range(0, index).boxed().toArray())
            );
        }
    }
    
}
