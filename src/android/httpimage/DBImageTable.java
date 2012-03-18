package android.httpimage;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * DB Table definition for implementing persistent storage of downloaded images.
 * 
 * @author zonghai@gmail.com
 */
public class DBImageTable implements BaseColumns {
    
    
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI
            = Uri.parse("httpimage.provider.DataProvider/thumbnail");
    
    /**
     * The name of the image
     * <P>Type: TEXT</P>
     */
    public static final String NAME = "Name";

    /**
     * The image data itself
     * <P>Type: IMAGE DATA</P>
     */
    public static final String DATA = "Data";

    
    /**
     * size of the image after being compressed.
     * <P>Type: INTEGER (long)</P>
     */
    public static final String SIZE = "Size";
    
    
    /**
     * The timestamp when the image was last modified
     * <P>Type: DATE </P>
     */
    public static final String TIMESTAMP = "Timestamp";
    
    /**
     * number of reference of the image
     * <P>Type: INTEGER (long)</P>
     */
    public static final String NUSE = "nUsed";
    
}
