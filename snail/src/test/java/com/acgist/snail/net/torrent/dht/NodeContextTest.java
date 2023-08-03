package com.acgist.snail.net.torrent.dht;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

class NodeContextTest extends Performance {

    @Test
    void testNewNodeSession() {
        LoggerConfig.off();
//      this.costed(1000, () -> {
//          NodeContext.getInstance().newNodeSession(StringUtils.unhex(this.buildId()), "0", 0);
//      });
//      this.costed(1000, () -> {
//          NodeContext.getInstance().sortNodes();
//      });
        this.costed(10000, () -> {
            NodeContext.getInstance().newNodeSession(StringUtils.unhex(this.buildId()), "0", 0);
        });
        var oldNodes = NodeContext.getInstance().nodes();
        var newNodes = NodeContext.getInstance().nodes();
        this.log(oldNodes.size());
        this.log(newNodes.size());
        Collections.sort(newNodes);
        assertTrue(oldNodes != newNodes);
        assertEquals(oldNodes.size(), newNodes.size());
//      oldNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
//      newNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
        for (int index = 0; index < oldNodes.size(); index++) {
            assertEquals(oldNodes.get(index), newNodes.get(index));
        }
    }

    @Test
    void testFindNode() {
        LoggerConfig.off();
        this.costed(10000, () -> {
            NodeContext.getInstance().newNodeSession(StringUtils.unhex(this.buildId()), "0", 0);
        });
        long size = NodeContext.getInstance().nodes().stream().filter(NodeSession::useable).count();
        this.log("可用节点：{}", size);
        final var target = this.buildId();
//      final var target = "0".repeat(40);
//      final var target = "f".repeat(40);
//      final var target = StringUtils.hex(NodeContext.getInstance().nodes().get(0).getId());
//      NodeContext.getInstance().nodes().forEach(node -> this.log(StringUtils.hex(node.getId())));
//      this.log("----");
        final var nodes = NodeContext.getInstance().findNode(target);
        nodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
//      this.log("----");
        final var newNodes = new ArrayList<>(nodes);
        Collections.sort(newNodes);
//      newNodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
        for (int index = 0; index < nodes.size(); index++) {
            assertEquals(nodes.get(index), newNodes.get(index));
        }
        this.log(nodes.size());
        this.log(target);
        this.costed(10000, () -> NodeContext.getInstance().findNode(target));
//      this.costed(10000, 10, () -> NodeContext.getInstance().findNode(target));
        size = NodeContext.getInstance().nodes().stream().filter(NodeSession::useable).count();
        this.log("可用节点：{}", size);
    }
    
    @Test
    void testResize() {
        LoggerConfig.off();
        this.costed(10000, () -> {
            NodeContext.getInstance().newNodeSession(StringUtils.unhex(this.buildId()), "0", 0);
        });
        long size = NodeContext.getInstance().nodes().stream().filter(NodeSession::useable).count();
        this.log("可用节点：{}", size);
        size = NodeContext.getInstance().resize().stream().filter(NodeSession::useable).count();
        this.log("可用节点：{}", size);
        this.costed(100000, () -> NodeContext.getInstance().resize());
        this.log("可用节点：{}", size);
        assertTrue(size < 10000);
    }

    @Test
    void testMinFindNode() {
        LoggerConfig.off();
        this.costed(2, () -> {
            NodeContext.getInstance().newNodeSession(StringUtils.unhex(this.buildId()), "0", 0);
        });
        final var target = this.buildId();
        final var nodes = NodeContext.getInstance().findNode(target);
        nodes.forEach(node -> this.log(StringUtils.hex(node.getId())));
        assertTrue(8 > nodes.size());
        this.log(nodes.size());
        this.log(target);
    }

    @Test
    void testNodeId() {
        LoggerConfig.off();
        this.cost();
        final var listA = new ArrayList<String>();
        final var listB = new ArrayList<String>();
        final var listC = new ArrayList<String>();
        final var listD = new ArrayList<String>();
        final var listE = new ArrayList<String>();
        for (int index = 0; index < Short.MAX_VALUE; index++) {
            listA.add(StringUtils.hex(NodeContext.getInstance().buildNodeId("124.31.75.21")));
            listB.add(StringUtils.hex(NodeContext.getInstance().buildNodeId("21.75.31.124")));
            listC.add(StringUtils.hex(NodeContext.getInstance().buildNodeId("65.23.51.170")));
            listD.add(StringUtils.hex(NodeContext.getInstance().buildNodeId("84.124.73.14")));
            listE.add(StringUtils.hex(NodeContext.getInstance().buildNodeId("43.213.53.83")));
        }
        this.costed();
        assertTrue(
            listA.stream().filter(value -> value.endsWith("01")).count() > 0L &&
            listB.stream().filter(value -> value.endsWith("56")).count() > 0L &&
            listC.stream().filter(value -> value.endsWith("16")).count() > 0L &&
            listD.stream().filter(value -> value.endsWith("41")).count() > 0L &&
            listE.stream().filter(value -> value.endsWith("5a")).count() > 0L
        );
//      IP           rand  example node ID
//      ============ ===== ==========================================
//      124.31.75.21   1   5fbfbf  f10c5d6a4ec8a88e4c6ab4c28b95eee4 01
        assertTrue(listA.stream().filter(value -> value.endsWith("01")).allMatch(value -> value.startsWith("5fbfb")));
//      21.75.31.124  86   5a3ce9  c14e7a08645677bbd1cfe7d8f956d532 56
        assertTrue(listB.stream().filter(value -> value.endsWith("56")).allMatch(value -> value.startsWith("5a3ce")));
//      65.23.51.170  22   a5d432  20bc8f112a3d426c84764f8c2a1150e6 16
        assertTrue(listC.stream().filter(value -> value.endsWith("16")).allMatch(value -> value.startsWith("a5d43")));
//      84.124.73.14  65   1b0321  dd1bb1fe518101ceef99462b947a01ff 41
        assertTrue(listD.stream().filter(value -> value.endsWith("41")).allMatch(value -> value.startsWith("1b032")));
//      43.213.53.83  90   e56f6c  bf5b7c4be0237986d5243b87aa6d5130 5a
        assertTrue(listE.stream().filter(value -> value.endsWith("5a")).allMatch(value -> value.startsWith("e56f6")));
        NodeContext.getInstance().buildNodeId("::FFFF");
    }
    
    private String buildId() {
        final byte[] bytes = new byte[20];
        final Random random = new Random();
        random.nextBytes(bytes);
        return StringUtils.hex(bytes);
    }
    
}
