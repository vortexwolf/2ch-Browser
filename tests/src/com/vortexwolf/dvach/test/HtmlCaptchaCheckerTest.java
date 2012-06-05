package com.vortexwolf.dvach.test;

import org.apache.http.HttpResponse;

import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker;
import com.vortexwolf.dvach.test.R;

import android.test.InstrumentationTestCase;

public class HtmlCaptchaCheckerTest extends InstrumentationTestCase {

	public void testCanSkip(){
		
		String responseText = "<style>#captcha_i { display: none };</style>Вам не надо вводить капчу.&nbsp;<a href=\"#\" id=\"capup\" onclick=\"javascript:load('captcha','/test/wakaba.pl?task=captcha&thread=1&dummy=1'); Recaptcha.reload(); return false;\">обновить</a>";
		
		IHtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText));
		boolean canSkeep = checker.canSkipCaptcha(null, null);
		
		assertTrue(canSkeep);
	}
	
	public void testMustEnter(){
		
		String responseText = "<style>#captcha_i { display: inline };</style><div id=\"recaptcha_widget\"><div id=\"recaptcha_data\"><div id=\"recaptcha_image\" onclick=\"javascript:Recaptcha.reload()\"></div></div></div>";
		
		IHtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText));
		boolean canSkeep = checker.canSkipCaptcha(null, null);
		
		assertFalse(canSkeep);
	}
	
	private class FakeHttpStringReader implements IHttpStringReader{

		private final String mResponse;
		public FakeHttpStringReader(String response){
			this.mResponse = response;
		}
		
		@Override
		public String fromUri(String uri) {
			return mResponse;
		}

		@Override
		public String fromResponse(HttpResponse response) {
			return mResponse;
		}
	}
}
