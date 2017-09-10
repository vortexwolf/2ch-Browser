package ua.in.quireg.chan.exceptions;

public class JsonApiReaderException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3031100511469203049L;

    public JsonApiReaderException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public JsonApiReaderException(String detailMessage) {
        super(detailMessage);
    }
}
