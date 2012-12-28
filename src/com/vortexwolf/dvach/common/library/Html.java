package com.vortexwolf.dvach.common.library;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;

import android.graphics.drawable.Drawable;
import android.text.*;

/**
 * This class processes HTML strings into displayable styled text. Not all HTML
 * tags are supported.
 */
public class Html {
    /**
     * Retrieves images for HTML &lt;img&gt; tags.
     */
    public static interface ImageGetter {
        /**
         * This methos is called when the HTML parser encounters an &lt;img&gt;
         * tag. The <code>source</code> argument is the string from the "src"
         * attribute; the return value should be a Drawable representation of
         * the image or <code>null</code> for a generic replacement image. Make
         * sure you call setBounds() on your Drawable if it doesn't already have
         * its bounds set.
         */
        public Drawable getDrawable(String source);
    }

    /**
     * Is notified when HTML tags are encountered that the parser does not know
     * how to interpret.
     */
    public static interface TagHandler {
        /**
         * This method will be called whenn the HTML parser encounters a tag
         * that it does not know how to interpret.
         */
        public void handleTag(boolean opening, String tag, SpannableStringBuilder output, Attributes attributes);
    }

    private Html() {
    }

    /**
     * Returns displayable styled text from the provided HTML string. Any
     * &lt;img&gt; tags in the HTML will display as a generic replacement image
     * which your program can then go through and replace with real images.
     * 
     * <p>
     * This uses TagSoup to handle real HTML, including all of the brokenness
     * found in the wild.
     */
    public static Spanned fromHtml(String source) {
        return fromHtml(source, null, null);
    }

    /**
     * Lazy initialization holder for HTML parser. This class will a) be
     * preloaded by the zygote, or b) not loaded until absolutely necessary.
     */
    private static class HtmlParser {
        private static final HTMLSchema schema = new HTMLSchema();
    }

    /**
     * Returns displayable styled text from the provided HTML string. Any
     * &lt;img&gt; tags in the HTML will use the specified ImageGetter to
     * request a representation of the image (use null if you don't want this)
     * and the specified TagHandler to handle unknown tags (specify null if you
     * don't want this).
     * 
     * <p>
     * This uses TagSoup to handle real HTML, including all of the brokenness
     * found in the wild.
     */
    public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
        Parser parser = new Parser();
        try {
            parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
        } catch (org.xml.sax.SAXNotRecognizedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        } catch (org.xml.sax.SAXNotSupportedException e) {
            // Should not happen.
            throw new RuntimeException(e);
        }

        HtmlToSpannedConverter converter = new HtmlToSpannedConverter(source, imageGetter, tagHandler, parser);
        return converter.convert();
    }
}
