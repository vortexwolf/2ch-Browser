package com.vortexwolf.dvach.test;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.services.domain.HtmlCaptchaChecker;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;
import com.vortexwolf.dvach.test.R;

import android.net.Uri;
import android.test.InstrumentationTestCase;

public class HtmlCaptchaCheckerTest extends InstrumentationTestCase {

	private final DvachUriBuilder mDvachUriBuilder = new DvachUriBuilder(Uri.parse("http://2ch.hk"));
	
	public void testCanSkip(){
		
		String responseText = "<style>#captcha_i { display: none };</style>Вам не надо вводить капчу.&nbsp;<a href=\"#\" id=\"capup\" onclick=\"javascript:load('captcha','/test/wakaba.pl?task=captcha&thread=1&dummy=1'); Recaptcha.reload(); return false;\">обновить</a>";
		
		IHtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), mDvachUriBuilder);
		boolean canSkeep = checker.canSkipCaptcha(null);
		
		assertTrue(canSkeep);
	}
	
	public void testMustEnter(){
		
		String responseText = "<style>#captcha_i { display: inline };</style><div id=\"recaptcha_widget\"><div id=\"recaptcha_data\"><div id=\"recaptcha_image\" onclick=\"javascript:Recaptcha.reload()\"></div></div></div>";
		
		IHtmlCaptchaChecker checker = new HtmlCaptchaChecker(new FakeHttpStringReader(responseText), mDvachUriBuilder);
		boolean canSkeep = checker.canSkipCaptcha(null);
		
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

		public String fromUri(String uri, Header[] customHeaders) {
			return null;
		}
	}
}
