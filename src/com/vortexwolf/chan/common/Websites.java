package com.vortexwolf.chan.common;

import java.util.regex.Pattern;

import android.net.Uri;

import com.vortexwolf.chan.boards.fourchan.FourchanUrlBuilder;
import com.vortexwolf.chan.boards.fourchan.FourchanUrlParser;
import com.vortexwolf.chan.boards.makaba.MakabaUrlBuilder;
import com.vortexwolf.chan.boards.makaba.MakabaUrlParser;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IUrlParser;

public class Websites {
    public static final String DVACH = "2ch";
    public static final String FOURCHAN = "4chan";

    private static final Pattern sDvachPattern = Pattern.compile("2ch|2-ch");
    private static final Pattern sFourchanPattern = Pattern.compile("4chan");

    // Handles external URLs which are declared in AndroidManifest
    public static String fromUri(Uri uri) {
        String host = uri.getHost();
        if (sDvachPattern.matcher(host).matches()) {
            return DVACH;
        } else if (sFourchanPattern.matcher(host).matches()) {
            return FOURCHAN;
        }

        return null;
    }

    public static IUrlBuilder getUrlBuilder(String website) {
        if (DVACH.equals(website)) {
            return Factory.resolve(MakabaUrlBuilder.class);
        } else if (FOURCHAN.equals(website)) {
            return Factory.resolve(FourchanUrlBuilder.class);
        }

        return null;
    }

    public static IUrlParser getUrlParser(String website){
        if (DVACH.equals(website)) {
            return Factory.resolve(MakabaUrlParser.class);
        } else if (FOURCHAN.equals(website)) {
            return Factory.resolve(FourchanUrlParser.class);
        }

        return null;
    }
}
