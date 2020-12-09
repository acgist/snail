package com.acgist.snail.protocol;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.initializer.impl.ProtocolInitializer;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.utils.Base32Utils;
import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.StringUtils;

public class ProtocolTest extends Performance {

	@Test
	public void testSupport() {
		ProtocolInitializer.newInstance().sync();
		var result = ProtocolManager.getInstance().support("https://www.acgist.com");
		this.log(result);
		result = ProtocolManager.getInstance().support("641000d9be79ad8947701c338c06211ba69e1b09");
		this.log(result);
		result = ProtocolManager.getInstance().support(Base32Utils.encode(StringUtils.unhex("641000d9be79ad8947701c338c06211ba69e1b09")));
		this.log(result);
		result = ProtocolManager.getInstance().support("thunder://QUFodHRwOi8vdXBvcy1oei1taXJyb3Jic3l1LmFjZ3ZpZGVvLmNvbS91cGdjeGNvZGUvMjIvNjkvMTI0NDY5MjIvMTI0NDY5MjItMS02NC5mbHY/ZT1pZzhldXhaTTJyTmNOYmhIaGJVVmhvTXpuV05CaHdkRXRvOGc1WDEwdWdOY1hCQl8mZGVhZGxpbmU9MTU2MTAyMTI1NCZnZW49cGxheXVybCZuYnM9MSZvaT0xNzAzMTc4Nzk0Jm9zPWJzeXUmcGxhdGZvcm09aHRtbDUmdHJpZD1kZWIzMTdkMjI0NDc0ZDg5YWI4YmI1ZDgzNWMzMGY3MyZ1aXBrPTUmdXBzaWc9YWY3NmExOTUyYjFlNjZhYmQ0NzBiNmRmOWYyNTA2MWImdXBhcmFtcz1lLGRlYWRsaW5lLGdlbixuYnMsb2ksb3MscGxhdGZvcm0sdHJpZCx1aXBrJm1pZD00NTU5MjY3Wlo==");
		this.log(result);
		result = ProtocolManager.getInstance().support("e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent");
		this.log(result);
	}
	
	@Test
	public void testBuildTaskSession() throws DownloadException {
		ProtocolInitializer.newInstance().sync();
		ITaskSession result;
//		result = ProtocolManager.getInstance().buildTaskSession("https://www.acgist.com");
//		this.log(result);
//		result = ProtocolManager.getInstance().buildTaskSession("641000d9be79ad8947701c338c06211ba69e1b09");
//		this.log(result);
//		result = ProtocolManager.getInstance().buildTaskSession(Base32Utils.encode(StringUtils.unhex("641000d9be79ad8947701c338c06211ba69e1b09")));
//		this.log(result);
		result = ProtocolManager.getInstance().buildTaskSession("thunder://QUFodHRwOi8vdXBvcy1oei1taXJyb3Jic3l1LmFjZ3ZpZGVvLmNvbS91cGdjeGNvZGUvMjIvNjkvMTI0NDY5MjIvMTI0NDY5MjItMS02NC5mbHY/ZT1pZzhldXhaTTJyTmNOYmhIaGJVVmhvTXpuV05CaHdkRXRvOGc1WDEwdWdOY1hCQl8mZGVhZGxpbmU9MTU2MTAyMTI1NCZnZW49cGxheXVybCZuYnM9MSZvaT0xNzAzMTc4Nzk0Jm9zPWJzeXUmcGxhdGZvcm09aHRtbDUmdHJpZD1kZWIzMTdkMjI0NDc0ZDg5YWI4YmI1ZDgzNWMzMGY3MyZ1aXBrPTUmdXBzaWc9YWY3NmExOTUyYjFlNjZhYmQ0NzBiNmRmOWYyNTA2MWImdXBhcmFtcz1lLGRlYWRsaW5lLGdlbixuYnMsb2ksb3MscGxhdGZvcm0sdHJpZCx1aXBrJm1pZD00NTU5MjY3Wlo==");
		this.log(result);
		result = ProtocolManager.getInstance().buildTaskSession("e:/snail/868f1199b18d05bf103aa8a8321f6428854d712e.torrent");
		this.log(result);
	}
	
}
