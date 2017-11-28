package ua.in.quireg.chan.common;

import android.net.Uri;

import ua.in.quireg.chan.boards.fourchan.FourchanWebsite;
import ua.in.quireg.chan.boards.makaba.MakabaWebsite;
import ua.in.quireg.chan.interfaces.IWebsite;

public class Websites {

    public static IWebsite getDefault() {
        return Factory.resolve(MakabaWebsite.class);
    }

    // Handles external URLs which are declared in AndroidManifest
    public static IWebsite fromUri(Uri uri) {
        String host = uri.getHost();
        if (MakabaWebsite.URI_PATTERN.matcher(host).find()) {
            return Factory.resolve(MakabaWebsite.class);
        } else if (FourchanWebsite.URI_PATTERN.matcher(host).find()) {
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
