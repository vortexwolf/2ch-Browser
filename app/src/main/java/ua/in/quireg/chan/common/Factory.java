package ua.in.quireg.chan.common;

public class Factory {

    private static final Container sContainer = new Container();

    public static Container getContainer() {
        return sContainer;
    }

    public static <T> T resolve(Class<T> type) {
        return getContainer().resolve(type);
    }
}
