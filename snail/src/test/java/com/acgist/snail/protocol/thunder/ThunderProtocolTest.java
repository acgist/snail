package com.acgist.snail.protocol.thunder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class ThunderProtocolTest extends Performance {

    @Test
    void testThunderProtocol() {
        final String url  = "thunder://QUFodHRwOi8vdXBvcy1oei1taXJyb3Jic3l1LmFjZ3ZpZGVvLmNvbS91cGdjeGNvZGUvMjIvNjkvMTI0NDY5MjIvMTI0NDY5MjItMS02NC5mbHY/ZT1pZzhldXhaTTJyTmNOYmhIaGJVVmhvTXpuV05CaHdkRXRvOGc1WDEwdWdOY1hCQl8mZGVhZGxpbmU9MTU2MTAyMTI1NCZnZW49cGxheXVybCZuYnM9MSZvaT0xNzAzMTc4Nzk0Jm9zPWJzeXUmcGxhdGZvcm09aHRtbDUmdHJpZD1kZWIzMTdkMjI0NDc0ZDg5YWI4YmI1ZDgzNWMzMGY3MyZ1aXBrPTUmdXBzaWc9YWY3NmExOTUyYjFlNjZhYmQ0NzBiNmRmOWYyNTA2MWImdXBhcmFtcz1lLGRlYWRsaW5lLGdlbixuYnMsb2ksb3MscGxhdGZvcm0sdHJpZCx1aXBrJm1pZD00NTU5MjY3Wlo=";
        String sourceUrl  = ThunderProtocol.getInstance().sourceUrl(url);
        String thunderUrl = ThunderProtocol.getInstance().thunderUrl(sourceUrl);
        this.log(sourceUrl);
        this.log(thunderUrl);
        assertEquals(url, thunderUrl);
        sourceUrl  = "ftp://localhost/VS2012中文旗舰版/license.htm";
        thunderUrl = ThunderProtocol.getInstance().thunderUrl(sourceUrl);
        this.log(sourceUrl);
        this.log(thunderUrl);
        assertEquals(sourceUrl, ThunderProtocol.getInstance().sourceUrl(thunderUrl));
    }
    
}
