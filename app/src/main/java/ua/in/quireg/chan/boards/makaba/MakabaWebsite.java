package ua.in.quireg.chan.boards.makaba;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IUrlParser;
import ua.in.quireg.chan.interfaces.IWebsite;

import java.util.regex.Pattern;

public class MakabaWebsite implements IWebsite {
    public static final String NAME = "2ch";
    public static final Pattern URI_PATTERN = Pattern.compile("2ch|2-ch");

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public IUrlBuilder getUrlBuilder() {
        return Factory.resolve(MakabaUrlBuilder.class);
    }

    @Override
    public IUrlParser getUrlParser(){
        return Factory.resolve(MakabaUrlParser.class);
    }
}
