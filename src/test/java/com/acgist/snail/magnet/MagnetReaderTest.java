package com.acgist.snail.magnet;

import org.junit.Test;

import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetReader;
import com.acgist.snail.system.exception.DownloadException;

public class MagnetReaderTest {

	@Test
	public void magnet() throws DownloadException {
		Magnet magnet = MagnetReader.newInstance("magnet:?xt=urn:ed2k:354B15E68FB8F36D7CD88FF94116CDC1&xl=10826029&dn=mediawiki-1.15.1.tar.gz&xt=urn:tree:tiger:7N5OAMRNGMSSEUE3ORHOKWN4WWIQ5X4EBOOTLJY&xt=urn:btih:QHQXPYWMACKDWKP47RRVIV7VOURXFE5Q&tr=http%3A%2F%2Ftracker.example.org%2Fannounce.php&as=http%3A%2F%2Fdownload.wikimedia.org%2Fmediawiki%2F1.15%2Fmediawiki-1.15.1.tar.gz&xs=http%3A%2F%2Fcache.example.org%2FXRX2PEFXOOEJFRVUCX6HMZMKS5TWG4K5&xs=dchub://example.org").magnet();
		System.out.println(magnet);
		System.out.println(magnet.getDn());
		System.out.println(magnet.getHash().length());
	}
	
}
