package com.vortexwolf.dvach.services.domain;

import org.apache.http.message.BasicHeader;

import android.net.Uri;

import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.services.presentation.DvachUriBuilder;

public class HtmlCaptchaChecker implements IHtmlCaptchaChecker {
	//private static final String CHECK_URI_FORMAT = "http://2ch.so/%s/wakaba.pl?task=captcha&thread=%s&dummy=";
	//private static final String HTML_RESPONSE_SKIP_CAPTCHA = "Вам не надо вводить капчу.";
	
	private final IHttpStringReader mHttpStringReader;
	private final DvachUriBuilder mDvachUriBuilder;
	
	public HtmlCaptchaChecker(IHttpStringReader httpStringReader, DvachUriBuilder dvachUriBuilder) {
		this.mHttpStringReader = httpStringReader;
		this.mDvachUriBuilder = dvachUriBuilder;
	}
	
	@Override
	public boolean canSkipCaptcha(Uri refererUri){
		
		Uri uri = this.mDvachUriBuilder.create2chUri("makaba/captcha?usercode=");
		
		// Add referer, because it always returns the incorrect value CHECK if not to set it
		org.apache.http.Header xRequest = new BasicHeader("Referer", refererUri.toString());
		
		org.apache.http.Header[] extraHeaders = new org.apache.http.Header[] { xRequest };
		String captchaBlock = this.mHttpStringReader.fromUri(uri.toString(), extraHeaders);
		
		return checkHtmlBlock(captchaBlock);
	}
	
	public boolean checkHtmlBlock(String captchaBlock){
		if(captchaBlock == null){
			return false;
		}
		
		if(captchaBlock.contains("OK")) {
			return true;
		}
		
/*		String text = Html.fromHtml(captchaBlock).toString();
		if(text.contains(HTML_RESPONSE_SKIP_CAPTCHA)){
			return true;
		}*/
		
		return false;
	}
}
