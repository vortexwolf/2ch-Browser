package com.vortexwolf.dvach.services.domain;

import android.text.Html;

import com.vortexwolf.dvach.common.utils.StringUtils;
import com.vortexwolf.dvach.interfaces.IHtmlCaptchaChecker;
import com.vortexwolf.dvach.interfaces.IHttpStringReader;

public class HtmlCaptchaChecker implements IHtmlCaptchaChecker {
	//private static final String CHECK_URI_FORMAT = "http://2ch.so/%s/wakaba.pl?task=captcha&thread=%s&dummy=";
	//private static final String HTML_RESPONSE_SKIP_CAPTCHA = "Вам не надо вводить капчу.";
	
	private final IHttpStringReader mHttpStringReader;
	
	public HtmlCaptchaChecker(IHttpStringReader httpStringReader) {
		this.mHttpStringReader = httpStringReader;
	}
	
	@Override
	public boolean canSkipCaptcha(String boardName, String threadNumber){
		
		//String uri = String.format(CHECK_URI_FORMAT, boardName, StringUtils.emptyIfNull(threadNumber));
		String uri = "http://2ch.so/makaba/captcha?code=";
		String captchaBlock = this.mHttpStringReader.fromUri(uri);
		
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
