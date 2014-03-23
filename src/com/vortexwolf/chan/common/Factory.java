package com.vortexwolf.chan.common;

public class Factory {

    private static final Container sContainer = new Container();

    public static Container getContainer() {
        return sContainer;
    }
}
