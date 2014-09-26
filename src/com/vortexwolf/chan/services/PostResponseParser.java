package com.vortexwolf.chan.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.exceptions.SendPostException;
import com.vortexwolf.chan.models.domain.SendPostResult;

public class PostResponseParser {
    private static final String TAG = "PostResponseParser";
    private static final Pattern sCenterPattern = Pattern.compile("<center>(.+?)</center>");
    private static final Pattern sErrorPattern = Pattern.compile("(.+?)<a[^>]*>Назад</a>.*?");
    private static final Pattern sMakabaErrorPattern = Pattern.compile("\"Reason\":\\s?\"(.*?)\"");

    public SendPostResult isPostSuccessful(String boardName, String responseText) {
        SendPostResult result = new SendPostResult();

        if (responseText == null) {
            result.isSuccess = true;
            return result;
        }

        if (ThreadPostUtils.isMakabaBoard(boardName)) {
            String errorText = RegexUtils.getGroupValue(responseText, sMakabaErrorPattern, 1);
            if (errorText != null) {
                result.error = errorText;
                return result;
            }
        } else {
            Matcher centerMatcher = sCenterPattern.matcher(responseText);
            while (centerMatcher.find() && centerMatcher.groupCount() > 0) {
                String htmlText = RegexUtils.getGroupValue(centerMatcher.group(1), sErrorPattern, 1);
                if (htmlText != null) {
                    String text = MyHtml.fromHtml(htmlText).toString().replaceAll("\n", "");

                    result.error = text;
                    return result;
                }
            }
        }

        // Пусть будет true, т.к. если не нашел ошибку, то скорей всего
        // результат верный
        result.isSuccess = true;
        return result;
    }
}
