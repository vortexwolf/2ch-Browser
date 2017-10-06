package ua.in.quireg.chan.exceptions;

public class HtmlNotJsonException extends Exception {
    private static final long serialVersionUID = -269759010337017774L;

    private final String mHtmlString;

    public HtmlNotJsonException(String html, String message) {
        super(message);
        this.mHtmlString = html;
    }

    public String getHtml() {
        return this.mHtmlString;
    }
}
