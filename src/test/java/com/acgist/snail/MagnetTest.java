package com.acgist.snail;

import org.junit.Test;

import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetBuilder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.StringUtils;

public class MagnetTest extends BaseTest {

	@Test
	public void testRead() throws DownloadException {
		Magnet magnet = MagnetBuilder.newInstance("magnet:?xt=urn:btih:08ada5a7a6183aae1e09d831df6748d566095a10&dn=Sintel%20%e6%b5%8b%e8%af%95&tr=udp%3A%2F%2Fexplodie.org%3A6969&tr=udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969&tr=udp%3A%2F%2Ftracker.empire-js.us%3A1337&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337&tr=wss%3A%2F%2Ftracker.btorrent.xyz&tr=wss%3A%2F%2Ftracker.fastcast.nz&tr=wss%3A%2F%2Ftracker.openwebtorrent.com&ws=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2F&xs=https%3A%2F%2Fwebtorrent.io%2Ftorrents%2Fsintel.torrent").build();
//		Magnet magnet = MagnetBuilder.newInstance("magnet:?xt=urn:btih:5PO7CEU2532ZTXYFCTYNMS5VLGO7QVYA&dn=%e5%a4%8d%e4%bb%87%e8%80%85%2e1080p%2eBD%e4%b8%ad%e8%8b%b1%e5%8f%8c%e5%ad%97&tr=udp%3a%2f%2f9%2erarbg%2eto%3a2710%2fannounce&tr=udp%3a%2f%2f9%2erarbg%2eme%3a2710%2fannounce&tr=http%3a%2f%2ftr%2ecili001%2ecom%3a8070%2fannounce&tr=http%3a%2f%2ftracker%2etrackerfix%2ecom%3a80%2fannounce&tr=udp%3a%2f%2fopen%2edemonii%2ecom%3a1337&tr=udp%3a%2f%2ftracker%2eopentrackr%2eorg%3a1337%2fannounce&tr=udp%3a%2f%2fp4p%2earenabg%2ecom%3a1337").build();
//		Magnet magnet = MagnetBuilder.newInstance("magnet:?xt=urn:ed2k:354B15E68FB8F36D7CD88FF94116CDC1&xl=10826029&dn=mediawiki-1.15.1.tar.gz&xt=urn:tree:tiger:7N5OAMRNGMSSEUE3ORHOKWN4WWIQ5X4EBOOTLJY&xt=urn:btih:QHQXPYWMACKDWKP47RRVIV7VOURXFE5Q&tr=http%3A%2F%2Ftracker.example.org%2Fannounce.php&as=http%3A%2F%2Fdownload.wikimedia.org%2Fmediawiki%2F1.15%2Fmediawiki-1.15.1.tar.gz&xs=http%3A%2F%2Fcache.example.org%2FXRX2PEFXOOEJFRVUCX6HMZMKS5TWG4K5&xs=dchub://example.org").magnet();
		this.log(magnet);
		this.log(magnet.getDn());
		this.log(magnet.getHash().length());
	}
	
	@Test
	public void testHash32() {
		var content = Base32Utils.encode(StringUtils.unhex("fa493c8add6d907a0575631831033dcf94ba5217"));
		this.log(content);
		this.log(content.length());
//		this.log(new String(Base32Utils.decode(content)));
		this.log(StringUtils.hex(Base32Utils.decode(content)));
		this.log(StringUtils.hex(Base32Utils.decode("6HYDB4YE7ZNA5CGN5QP76PNH6O7VK7YX")));
	}
	
}
