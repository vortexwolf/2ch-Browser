package ua.in.quireg.chan.boards.fourchan;

import ua.in.quireg.chan.common.Factory;
import ua.in.quireg.chan.interfaces.IUrlBuilder;
import ua.in.quireg.chan.interfaces.IUrlParser;
import ua.in.quireg.chan.interfaces.IWebsite;

import java.util.regex.Pattern;

public class FourchanWebsite implements IWebsite {
    public static final String NAME = "4chan";
    public static final Pattern URI_PATTERN = Pattern.compile("4chan");

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public IUrlBuilder getUrlBuilder() {
        return Factory.resolve(FourchanUrlBuilder.class);
    }

    @Override
    public IUrlParser getUrlParser() {
        return Factory.resolve(FourchanUrlParser.class);
    }
}
