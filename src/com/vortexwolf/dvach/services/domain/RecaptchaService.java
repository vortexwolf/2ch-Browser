package com.vortexwolf.dvach.services.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.DefaultHttpClient;

import com.vortexwolf.dvach.interfaces.IHttpStringReader;
import com.vortexwolf.dvach.models.domain.CaptchaEntity;

public class RecaptchaService {
	
	  public static final String apiBaseUrl = "http://api.recaptcha.net/";
      private static final Pattern imgReg = Pattern.compile("(<img .*src\\=\")([^\"]*)");
      private static final Pattern chalReg = Pattern.compile("(id=\"recaptcha_challenge_field\" value=\")([^\"]*)");
	  
      public static CaptchaEntity loadCaptcha(IHttpStringReader httpStringReader){
    	  String uri = "http://api.recaptcha.net/noscript?k=6LdOEMMSAAAAAIGhmYodlkflEb2C-xgPjyATLnxx";
    	  String html = httpStringReader.fromUri(uri);
    	  
    	  CaptchaEntity captcha = getCaptcha(html);
    	  
    	  return captcha;
      }
	  
	  private static CaptchaEntity getCaptcha(String html) {
	        try
	        {
	          // test for regex match
	          Matcher challengeMatch = chalReg.matcher(html);
	          Matcher imageMatch = imgReg.matcher(html);
	          boolean bChal = challengeMatch.find();
	          boolean bImg = imageMatch.find();
	  
	          // make sure we got regex matches
	          if (bChal && bImg)
	          {
	              // get challenge
	              String challenge = challengeMatch.group(2);
	  
	              // get image url
	              String imageUrl = apiBaseUrl+imageMatch.group(2);
	  
	              // return a Captcha struct
	              CaptchaEntity captcha = new CaptchaEntity();
	              captcha.setKey(challenge);
	              captcha.setUrl(imageUrl);
	              return captcha;
	          }
	          else
	          {
	              // something didn't work.
	              return null;
	          }
	        }
	        catch(Exception e)
	        {
	            return null;
	        
	        }
	    }
}
