package pl.droidsonroids.gif;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * A {@link Drawable} which can be used to hold GIF images, especially animations.
 * Basic GIF metadata can be also obtained.
 *
 * @author koral--
 */
public class GifDrawable extends Drawable {
    static {
        System.loadLibrary("gif");
    }

    private static native void renderFrame(int[] pixels, int gifFileInPtr, int[] metaData);

    private static native int openFd(int[] metaData, FileDescriptor fd, long offset) throws GifIOException;

    private static native int openByteArray(int[] metaData, byte[] bytes) throws GifIOException;

    private static native int openDirectByteBuffer(int[] metaData, ByteBuffer buffer) throws GifIOException;

    private static native int openStream(int[] metaData, InputStream stream) throws GifIOException;

    private static native int openFile(int[] metaData, String filePath) throws GifIOException;

    private static native void free(int gifFileInPtr);

    private static native void reset(int gifFileInPtr);

    private static native void setSpeedFactor(int gifFileInPtr, float factor);

    private static native String getComment(int gifFileInPtr);

    private static native int getLoopCount(int gifFileInPtr);

    private static native int getDuration(int gifFileInPtr);

    private static native int getCurrentPosition(int gifFileInPtr);

    private static native int seekToTime(int gifFileInPtr, int pos, int[] pixels);

    private static native int seekToFrame(int gifFileInPtr, int frameNr, int[] pixels);

    private static native int saveRemainder(int gifFileInPtr);

    private static native int restoreRemainder(int gifFileInPtr);

    private static native long getAllocationByteCount(int gifFileInPtr);

    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());

    private volatile int mGifInfoPtr;
    private volatile boolean mIsRunning = true;

    private final int[] mMetaData = new int[5];//[w,h,imageCount,errorCode,post invalidation time]
    private final long mInputSourceLength;

    private float mSx = 1f;
    private float mSy = 1f;
    private boolean mApplyTransformation;
    private final Rect mDstRect = new Rect();

    /**
     * Paint used to draw on a Canvas
     */
    protected final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
    /**
     * Frame buffer, holds current frame.
     * Each element is a packed int representing a {@link Color} at the given pixel.
     */
    private int[] mColors;

    private final Runnable mResetTask = new Runnable() {
        @Override
        public void run() {
            reset(mGifInfoPtr);
        }
    };

    private final Runnable mStartTask = new Runnable() {
        @Override
        public void run() {
            restoreRemainder(mGifInfoPtr);
            invalidateSelf();
        }
    };

    private final Runnable mSaveRemainderTask = new Runnable() {
        @Override
        public void run() {
            saveRemainder(mGifInfoPtr);
        }
    };

    private final Runnable mInvalidateTask = new Runnable() {
        @Override
        public void run() {
            invalidateSelf();
        }
    };

    private static void runOnUiThread(Runnable task) {
        if (Looper.myLooper() == UI_HANDLER.getLooper())
            task.run();
        else
            UI_HANDLER.post(task);
    }

    /**
     * Creates drawable from resource.
     *
     * @param res Resources to read from
     * @param id  resource id
     * @throws NotFoundException    if the given ID does not exist.
     * @throws IOException          when opening failed
     * @throws NullPointerException if res is null
     */
    public GifDrawable(Resources res, int id) throws NotFoundException, IOException {
        this(res.openRawResourceFd(id));
    }

    /**
     * Creates drawable from asset.
     *
     * @param assets    AssetManager to read from
     * @param assetName name of the asset
     * @throws IOException          when opening failed
     * @throws NullPointerException if assets or assetName is null
     */
    public GifDrawable(AssetManager assets, String assetName) throws IOException {
        this(assets.openFd(assetName));
    }

    /**
     * Constructs drawable from given file path.<br>
     * Only metadata is read, no graphic data is decoded here.
     * In practice can be called from main thread. However it will violate
     * {@link StrictMode} policy if disk reads detection is enabled.<br>
     *
     * @param filePath path to the GIF file
     * @throws IOException          when opening failed
     * @throws NullPointerException if filePath is null
     */
    public GifDrawable(String filePath) throws IOException {
        if (filePath == null)
            throw new NullPointerException("Source is null");
        mInputSourceLength = new File(filePath).length();
        mGifInfoPtr = openFile(mMetaData, filePath);
        mColors = new int[mMetaData[0] * mMetaData[1]];
    }

    /**
     * Equivalent to {@code} GifDrawable(file.getPath())}
     *
     * @param file the GIF file
     * @throws IOException          when opening failed
     * @throws NullPointerException if file is null
     */
    public GifDrawable(File file) throws IOException {
        if (file == null)
            throw new NullPointerException("Source is null");
        mInputSourceLength = file.length();
        mGifInfoPtr = openFile(mMetaData, file.getPath());
        mColors = new int[mMetaData[0] * mMetaData[1]];
    }

    /**
     * Creates drawable from InputStream.
     * InputStream must support marking, IllegalArgumentException will be thrown otherwise.
     *
     * @param stream stream to read from
     * @throws IOException              when opening failed
     * @throws IllegalArgumentException if stream does not support marking
     * @throws NullPointerException     if stream is null
     */
    public GifDrawable(InputStream stream) throws IOException {
        if (stream == null)
            throw new NullPointerException("Source is null");
        if (!stream.markSupported())
            throw new IllegalArgumentException("InputStream does not support marking");
        mGifInfoPtr = openStream(mMetaData, stream);
        mColors = new int[mMetaData[0] * mMetaData[1]];
        mInputSourceLength = -1L;
    }

    /**
     * Creates drawable from AssetFileDescriptor.
     * Convenience wrapper for {@link GifDrawable#GifDrawable(FileDescriptor)}
     *
     * @param afd source
     * @throws NullPointerException if afd is null
     * @throws IOException          when opening failed
     */
    public GifDrawable(AssetFileDescriptor afd) throws IOException {
        if (afd == null)
            throw new NullPointerException("Source is null");
        FileDescriptor fd = afd.getFileDescriptor();
        try {
            mGifInfoPtr = openFd(mMetaData, fd, afd.getStartOffset());
        } catch (IOException ex) {
            afd.close();
            throw ex;
        }
        mColors = new int[mMetaData[0] * mMetaData[1]];
        mInputSourceLength = afd.getLength();
    }

    /**
     * Creates drawable from FileDescriptor
     *
     * @param fd source
     * @throws IOException          when opening failed
     * @throws NullPointerException if fd is null
     */
    public GifDrawable(FileDescriptor fd) throws IOException {
        if (fd == null)
            throw new NullPointerException("Source is null");
        mGifInfoPtr = openFd(mMetaData, fd, 0);
        mColors = new int[mMetaData[0] * mMetaData[1]];
        mInputSourceLength = -1L;
    }

    /**
     * Creates drawable from byte array.<br>
     * It can be larger than size of the GIF data. Bytes beyond GIF terminator are not accessed.
     *
     * @param bytes raw GIF bytes
     * @throws IOException          if bytes does not contain valid GIF data
     * @throws NullPointerException if bytes are null
     */
    public GifDrawable(byte[] bytes) throws IOException {
        if (bytes == null)
            throw new NullPointerException("Source is null");
        mGifInfoPtr = openByteArray(mMetaData, bytes);
        mColors = new int[mMetaData[0] * mMetaData[1]];
        mInputSourceLength = bytes.length;
    }

    /**
     * Creates drawable from {@link ByteBuffer}. Only direct buffers are supported.
     * Buffer can be larger than size of the GIF data. Bytes beyond GIF terminator are not accessed.
     *
     * @param buffer buffer containing GIF data
     * @throws IOException              if buffer does not contain valid GIF data
     * @throws IllegalArgumentException if buffer is indirect
     * @throws NullPointerException     if buffer is null
     */
    public GifDrawable(ByteBuffer buffer) throws IOException {
        if (buffer == null)
            throw new NullPointerException("Source is null");
        if (!buffer.isDirect())
            throw new IllegalArgumentException("ByteBuffer is not direct");
        mGifInfoPtr = openDirectByteBuffer(mMetaData, buffer);
        mColors = new int[mMetaData[0] * mMetaData[1]];
        mInputSourceLength = buffer.capacity();
    }

    /**
     * Creates drawable from {@link android.net.Uri} which is resolved using {@code resolver}.
     * {@link android.content.ContentResolver#openAssetFileDescriptor(android.net.Uri, String)}
     * is used to open an Uri.
     *
     * @param uri      GIF Uri, cannot be null.
     * @param resolver resolver, cannot be null.
     * @throws IOException if resolution fails or destination is not a GIF.
     */
    public GifDrawable(ContentResolver resolver, Uri uri) throws IOException {
        this(resolver.openAssetFileDescriptor(uri, "r"));
    }

    /**
     * Frees any memory allocated native way.
     * Operation is irreversible. After this call, nothing will be drawn.
     * This method is idempotent, subsequent calls have no effect.
     * Like {@link android.graphics.Bitmap#recycle()} this is an advanced call and
     * is invoked implicitly by finalizer.
     */
    public void recycle() {
        mIsRunning = false;
        int tmpPtr = mGifInfoPtr;
        mGifInfoPtr = 0;
        mColors = null;
        free(tmpPtr);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }

    @Override
    public int getIntrinsicHeight() {
        return mMetaData[1];
    }

    @Override
    public int getIntrinsicWidth() {
        return mMetaData[0];
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    /**
     * See {@link Drawable#getOpacity()}
     *
     * @return always {@link PixelFormat#TRANSPARENT}
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
    
    /**
     * @return basic description of the GIF including size and number of frames
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "Size: %dx%d, %d frames, error: %d", mMetaData[0], mMetaData[1], mMetaData[2], mMetaData[3]);
    }
    
    /**
     * Sets new animation speed factor.<br>
     * Note: If animation is in progress ({@link #draw(Canvas)}) was already called)
     * then effects will be visible starting from the next frame. Duration of the currently rendered
     * frame is not affected.
     *
     * @param factor new speed factor, eg. 0.5f means half speed, 1.0f - normal, 2.0f - double speed
     * @throws IllegalArgumentException if factor&lt;=0
     */
    public void setSpeed(float factor) {
        if (factor <= 0f)
            throw new IllegalArgumentException("Speed factor is not positive");
        setSpeedFactor(mGifInfoPtr, factor);
    }
    
    /**
     * Returns the minimum number of bytes that can be used to store pixels of the single frame.
     * Returned value is the same for all the frames since it is based on the size of GIF screen.
     *
     * @return width * height (of the GIF screen ix pixels) * 4
     */
    public int getFrameByteCount() {
        return mMetaData[0] * mMetaData[1] * 4;
    }
    
    /**
     * Returns length of the input source obtained at the opening time or -1 if
     * length is unknown. Returned value does not change during runtime.
     * For GifDrawables constructed from {@link InputStream} and {@link FileDescriptor} -1 is always returned.
     * In case of {@link File}, file path, byte array and {@link ByteBuffer} length is always known.
     *
     * @return number of bytes backed by input source or -1 if it is unknown
     */
    public long getInputSourceByteCount() {
        return mInputSourceLength;
    }
    
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mApplyTransformation = true;
    }

    /**
     * Reads and renders new frame if needed then draws last rendered frame.
     *
     * @param canvas canvas to draw into
     */
    @Override
    public void draw(Canvas canvas) {
        if (mApplyTransformation) {
            mDstRect.set(getBounds());
            mSx = (float) mDstRect.width() / mMetaData[0];
            mSy = (float) mDstRect.height() / mMetaData[1];
            mApplyTransformation = false;
        }
        if (mPaint.getShader() == null) {
            if (mIsRunning)
                renderFrame(mColors, mGifInfoPtr, mMetaData);
            else
                mMetaData[4] = -1;

            canvas.scale(mSx, mSy);
            final int[] colors = mColors;

            if (colors != null)
                canvas.drawBitmap(colors, 0, mMetaData[0], 0f, 0f, mMetaData[0], mMetaData[1], true, mPaint);

            if (mMetaData[4] >= 0 && mMetaData[2] > 1)
                //UI_HANDLER.postDelayed(mInvalidateTask, mMetaData[4]);//TODO don't post if message for given frame was already posted
                invalidateSelf();
        } else
            canvas.drawRect(mDstRect, mPaint);
    }
    
    /**
     * @return the paint used to render this drawable
     */
    public final Paint getPaint() {
        return mPaint;
    }

    @Override
    public int getAlpha() {
        return mPaint.getAlpha();
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override
    public void setDither(boolean dither) {
        mPaint.setDither(dither);
        invalidateSelf();
    }

    @Override
    public int getMinimumHeight() {
        return mMetaData[1];
    }

    @Override
    public int getMinimumWidth() {
        return mMetaData[0];
    }

}