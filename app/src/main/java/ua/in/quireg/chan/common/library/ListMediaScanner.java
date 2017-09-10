package ua.in.quireg.chan.common.library;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class ListMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private final Context mContext;
    private final String[] mPaths;
    private MediaScannerConnection mConnection;
    private int mNextPath;

    public ListMediaScanner(Context context, String[] paths) {
        mContext = context;
        mPaths = paths;
    }

    public void scan() {
        this.mConnection = new MediaScannerConnection(this.mContext, this);
        this.mConnection.connect();
    }

    public void onMediaScannerConnected() {
        scanNextPath();
    }

    public void onScanCompleted(String path, Uri uri) {
        scanNextPath();
    }

    private void scanNextPath() {
        if (mNextPath >= mPaths.length) {
            mConnection.disconnect();
            return;
        }

        mConnection.scanFile(mPaths[mNextPath], null);
        mNextPath++;
    }
}
