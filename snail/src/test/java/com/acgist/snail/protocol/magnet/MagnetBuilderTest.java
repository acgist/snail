package com.acgist.snail.protocol.magnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.logger.LoggerConfig;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.Magnet;
import com.acgist.snail.utils.Performance;

class MagnetBuilderTest extends Performance {

    @Test
    void testBuild() throws DownloadException {
        Magnet magnet = MagnetBuilder.newInstance("magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel%20%e6%b5%8b%e8%af%95&tr=udp%3A%2F%2Fexplodie.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Ftracker.empire-js.us%3A1337&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337&tr=wss%3A%2F%2Ftracker.btorrent.xyz&tr=wss%3A%2F%2Ftracker.fastcast.nz&tr=wss%3A%2F%2Ftracker.openwebtorrent.com&ws=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2F&xs=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2Fsintel.torrent").build();
        assertEquals("Sintel 测试", magnet.getDn());
        assertEquals("08ada5a7a6183aae1e09d831df6748d566095a10", magnet.getHash());
        this.log(magnet);
        magnet = MagnetBuilder.newInstance("magnet:?xt=urn:btih:5PO7CEU2532ZTXYFCTYNMS5VLGO7QVYA&dn=%e5%a4%8d%e4%bb%87%e8%80%85%2e1080p%2eBD%e4%b8%ad%e8%8b%b1%e5%8f%8c%e5%ad%97&tr=udp%3a%2f%2f9%2erarbg%2eto%3a2710%2fannounce&tr=udp%3a%2f%2f9%2erarbg%2eme%3a2710%2fannounce&tr=http%3a%2f%2ftr%2ecili001%2ecom%3a8070%2fannounce&tr=http%3a%2f%2ftracker%2etrackerfix%2ecom%3a80%2fannounce&tr=udp%3a%2f%2fopen%2edemonii%2ecom%3a1337&tr=udp%3a%2f%2ftracker%2eopentrackr%2eorg%3a1337%2fannounce&tr=udp%3a%2f%2fp4p%2earenabg%2ecom%3a1337").build();
        assertEquals("ebddf1129aeef599df0514f0d64bb5599df85700", magnet.getHash());
        this.log(magnet);
        magnet = MagnetBuilder.newInstance("magnet:?xt=urn:ed2k:354B15E68FB8F36D7CD88FF94116CDC1&xl=10826029&dn=mediawiki-1.15.1.tar.gz&xt=urn:tree:tiger:7N5OAMRNGMSSEUE3ORHOKWN4WWIQ5X4EBOOTLJY&xt=urn:btih:QHQXPYWMACKDWKP47RRVIV7VOURXFE5Q&tr=http%3A%2F%2Ftracker.example.org%2Fannounce.php&as=http%3A%2F%2Fdownload.wikimedia.org%2Fmediawiki%2F1.15%2Fmediawiki-1.15.1.tar.gz&xs=http%3A%2F%2Fcache.example.org%2FXRX2PEFXOOEJFRVUCX6HMZMKS5TWG4K5&xs=dchub://example.org").build();
        assertEquals("81e177e2cc00943b29fcfc635457f575237293b0", magnet.getHash());
        this.log(magnet);
    }

    @Test
    void testCosted() {
        LoggerConfig.off();
        final long costed = this.costed(100000, () -> {
            try {
                MagnetBuilder.newInstance("magnet:?xt=urn:ed2k:354B15E68FB8F36D7CD88FF94116CDC1&xl=10826029&dn=mediawiki-1.15.1.tar.gz&xt=urn:tree:tiger:7N5OAMRNGMSSEUE3ORHOKWN4WWIQ5X4EBOOTLJY&xt=urn:btih:QHQXPYWMACKDWKP47RRVIV7VOURXFE5Q&tr=http%3A%2F%2Ftracker.example.org%2Fannounce.php&as=http%3A%2F%2Fdownload.wikimedia.org%2Fmediawiki%2F1.15%2Fmediawiki-1.15.1.tar.gz&xs=http%3A%2F%2Fcache.example.org%2FXRX2PEFXOOEJFRVUCX6HMZMKS5TWG4K5&xs=dchub://example.org").build();
            } catch (DownloadException e) {
                // 忽略
            }
        });
        assertTrue(costed < 4000);
    }
    
}
