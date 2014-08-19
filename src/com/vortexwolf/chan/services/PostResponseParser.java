package com.vortexwolf.chan.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vortexwolf.chan.common.library.MyHtml;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.RegexUtils;
import com.vortexwolf.chan.common.utils.ThreadPostUtils;
import com.vortexwolf.chan.exceptions.SendPostException;

public class PostResponseParser {
    private static final String TAG = "PostResponseParser";
    private static final Pattern sCenterPattern = Pattern.compile("<center>(.+?)</center>");
    private static final Pattern sErrorPattern = Pattern.compile("(.+?)<a[^>]*>Назад</a>.*?");
    private static final Pattern sMakabaErrorPattern = Pattern.compile("\"Reason\":\\s?\"(.*?)\"");

    public boolean isPostSuccessful(String boardName, String response) throws SendPostException {
        if (response == null) {
            return true;
        }

        if (ThreadPostUtils.isMakabaBoard(boardName)) {
            String errorText = RegexUtils.getGroupValue(response, sMakabaErrorPattern, 1);
            if (errorText != null) {
                MyLog.v(TAG, errorText);
                throw new SendPostException(errorText);
            }
        } else {
            Matcher centerMatcher = sCenterPattern.matcher(response);
            while (centerMatcher.find() && centerMatcher.groupCount() > 0) {
                String htmlText = RegexUtils.getGroupValue(centerMatcher.group(1), sErrorPattern, 1);
                if (htmlText != null) {
                    String text = MyHtml.fromHtml(htmlText).toString().replaceAll("\n", "");
    
                    MyLog.v(TAG, text);
                    throw new SendPostException(text);
                }
            }
        }

        // Пусть будет true, т.к. если не нашел ошибку, то скорей всего
        // результат верный
        return true;
    }
}
