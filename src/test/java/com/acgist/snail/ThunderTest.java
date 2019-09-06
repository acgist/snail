package com.acgist.snail;

import java.util.Base64;

import org.junit.Test;

import com.acgist.snail.protocol.thunder.ThunderProtocol;

public class ThunderTest {

	@Test
	public void read() {
		String url = "thunder://QUFodHRwOi8vdXBvcy1oei1taXJyb3Jic3l1LmFjZ3ZpZGVvLmNvbS91cGdjeGNvZGUvMjIvNjkvMTI0NDY5MjIvMTI0NDY5MjItMS02NC5mbHY/ZT1pZzhldXhaTTJyTmNOYmhIaGJVVmhvTXpuV05CaHdkRXRvOGc1WDEwdWdOY1hCQl8mZGVhZGxpbmU9MTU2MTAyMTI1NCZnZW49cGxheXVybCZuYnM9MSZvaT0xNzAzMTc4Nzk0Jm9zPWJzeXUmcGxhdGZvcm09aHRtbDUmdHJpZD1kZWIzMTdkMjI0NDc0ZDg5YWI4YmI1ZDgzNWMzMGY3MyZ1aXBrPTUmdXBzaWc9YWY3NmExOTUyYjFlNjZhYmQ0NzBiNmRmOWYyNTA2MWImdXBhcmFtcz1lLGRlYWRsaW5lLGdlbixuYnMsb2ksb3MscGxhdGZvcm0sdHJpZCx1aXBrJm1pZD00NTU5MjY3Wlo==";
		url = url.substring(ThunderProtocol.THUNDER_PREFIX.length());
		String newUrl = new String(Base64.getMimeDecoder().decode(url));
//		String newUrl = new String(Base64.getDecoder().decode(url));
		newUrl = newUrl.substring(2, newUrl.length() - 2);
		System.out.println(newUrl);
	}
	
}
