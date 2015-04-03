package com.vortexwolf.chan.common;

import java.util.regex.Pattern;

import android.net.Uri;

import com.vortexwolf.chan.boards.fourchan.FourchanUrlBuilder;
import com.vortexwolf.chan.boards.fourchan.FourchanUrlParser;
import com.vortexwolf.chan.boards.fourchan.FourchanWebsite;
import com.vortexwolf.chan.boards.makaba.MakabaUrlBuilder;
import com.vortexwolf.chan.boards.makaba.MakabaUrlParser;
import com.vortexwolf.chan.boards.makaba.MakabaWebsite;
import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IUrlParser;
import com.vortexwolf.chan.interfaces.IWebsite;

public class Websites {
    public static IWebsite getDefault() {
        return Factory.resolve(MakabaWebsite.class);
    }

    // Handles external URLs which are declared in AndroidManifest
    public static IWebsite fromUri(Uri uri) {
        String host = uri.getHost();
        if (MakabaWebsite.URI_PATTERN.matcher(host).matches()) {
            return Factory.resolve(MakabaWebsite.class);
        } else if (FourchanWebsite.URI_PATTERN.matcher(host).matches()) {
            return Factory.resolve(FourchanWebsite.class);
        }

        return null;
    }

    public static IWebsite fromName(String name) {
        if (MakabaWebsite.NAME.equals(name)) {
            return Factory.resolve(MakabaWebsite.class);
        } else if (FourchanWebsite.NAME.equals(name)) {
            return Factory.resolve(FourchanWebsite.class);
        }

        return null;
    }
}
