package ua.in.quireg.chan.interfaces;

public interface IWebsite {
    String name();

    IUrlBuilder getUrlBuilder();

    IUrlParser getUrlParser();
}
