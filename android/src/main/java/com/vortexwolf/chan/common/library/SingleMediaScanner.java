package com.vortexwolf.chan.common.library;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mMs;
    private File mFile;

    public SingleMediaScanner(Context context, File f) {
        this.mFile = f;
        this.mMs = new MediaScannerConnection(context, this);
        this.mMs.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        this.mMs.scanFile(this.mFile.getAbsolutePath(), null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        this.mMs.disconnect();
    }
}
