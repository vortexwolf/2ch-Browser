package com.vortexwolf.chan.services;

import java.util.regex.Pattern;

import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.models.domain.CloudflareCaptchaModel;

// TODO: delete because this class is useless
public class CloudflarePageParser {
	private static final Pattern imageUriPattern = Pattern.compile("<img id=\"recaptcha_challenge_image\".+?src=\"(.+?)\">");
	private static final Pattern keyPattern = Pattern.compile("<input.+?id=\"recaptcha_challenge_field\".+?value=\"(.+?)\">");
	private static final Pattern idPattern = Pattern.compile("<input.+?id=\"id\".+?value=\"([0-9a-f]+)\".*?>");
	
	public CloudflareCaptchaModel parse(String html) {
		String imageUri = RegexUtils.getGroupValue(html, imageUriPattern, 1);
		String key = RegexUtils.getGroupValue(html, keyPattern, 1);
		String id = RegexUtils.getGroupValue(html, idPattern, 1);
		if (imageUri == null || key == null || id == null) {
			return null;
		}
		
		CloudflareCaptchaModel model = new CloudflareCaptchaModel();
		model.setUrl(imageUri);
		model.setKey(key);
		model.setId(id);
		
		return model;
	}
}
