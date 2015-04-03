package com.vortexwolf.chan.interfaces;

import com.vortexwolf.chan.interfaces.IUrlBuilder;
import com.vortexwolf.chan.interfaces.IUrlParser;

public interface IWebsite {
    String name();

    IUrlBuilder getUrlBuilder();

    IUrlParser getUrlParser();
}
