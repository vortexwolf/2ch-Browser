package ua.in.quireg.chan.services;

import ua.in.quireg.chan.BuildConfig;
import ua.in.quireg.chan.common.utils.UriUtils;
import ua.in.quireg.chan.interfaces.IHttpStringReader;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IWebsite;
import ua.in.quireg.chan.models.domain.CaptchaType;
import ua.in.quireg.chan.settings.ApplicationSettings;
import com.wildflyforcer.utils.CaptchaResultNew;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.map.ObjectMapper;

public class HtmlCaptchaChecker {
    private final IHttpStringReader mHttpStringReader;
    private final ApplicationSettings mApplicationSettings;

    public HtmlCaptchaChecker(IHttpStringReader httpStringReader, ApplicationSettings settings) {
        this.mHttpStringReader = httpStringReader;
        this.mApplicationSettings = settings;
    }

    public CaptchaResult canSkipCaptcha(IWebsite website, CaptchaType captchaType, String boardName, String threadNumber) {
        IUrlBuilder urlBuilder = website.getUrlBuilder();
        String checkUrl;

        if (captchaType == CaptchaType.APP) {
            checkUrl = urlBuilder.getAppCaptchaCheckUrl(BuildConfig.CAPTCHA_API_PUBLIC_KEY);
        } else {
            checkUrl = urlBuilder.getPasscodeCookieCheckUrl(boardName, threadNumber);
        }

        CaptchaResult result;
        try {
            String referer = UriUtils.getBoardOrThreadUrl(urlBuilder, boardName, 0, threadNumber);
            Header[] extraHeaders = new Header[] { new BasicHeader("Referer", referer) };
            String captchaBlock = this.mHttpStringReader.fromUri(checkUrl, extraHeaders);
            result = this.checkHtmlBlock(captchaBlock);
        } catch (Exception e) {
            result = this.createEmptyResult();
        }

        return result;
    }

    public CaptchaResult checkHtmlBlock(String captchaBlock) {
        if (captchaBlock == null) {
            return this.createEmptyResult();
        }
        ObjectMapper mapper = new ObjectMapper();
        CaptchaResult result = new CaptchaResult();
        try {
            CaptchaResultNew fromJson = mapper.readValue(captchaBlock, CaptchaResultNew.class);
            result.captchaType = fromJson.getType();
            switch (fromJson.getResult()) {
                case "3":
                    result.canSkip = true;
                    break;
                case "2":
                    result.canSkip = true;
                    result.successPassCode = true;
                    break;
                case "1":
                    result.canSkip = false;
                    result.captchaKey = fromJson.getId();
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            result = this.createEmptyResult();
        }
        return result;
    }

    private CaptchaResult createEmptyResult() {
        return new CaptchaResult();
    }

    public static class CaptchaResult {
        public boolean canSkip;
        public boolean successPassCode;
        public boolean failPassCode;
        public String captchaKey;
        public String captchaType;
    }
}
