package ua.in.quireg.chan.common.library;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private final Context mContext;
    private final File mFile;
    private MediaScannerConnection mConnection;

    public SingleMediaScanner(Context context, File f) {
        this.mContext = context;
        this.mFile = f;
    }

    public void scan() {
        this.mConnection = new MediaScannerConnection(this.mContext, this);
        this.mConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        this.mConnection.scanFile(this.mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        this.mConnection.disconnect();
    }
}
