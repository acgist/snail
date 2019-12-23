package com.acgist.snail;

import java.util.Locale;

import org.junit.Test;

public class LocaleTest extends BaseTest {

	@Test
	public void testLocale() {
//		final Locale locale = Locale.US;
		final Locale locale = Locale.CHINA;
//		final Locale locale = Locale.TAIWAN;
//		final Locale locale = Locale.getDefault();
		this.log(locale.getLanguage());
		this.log(locale.getDisplayLanguage());
		this.log(locale.getCountry());
		this.log(locale.getDisplayCountry());
		this.log(locale.getScript());
		this.log(locale.getVariant());
		this.log(locale.getExtensionKeys());
	}
	
}
