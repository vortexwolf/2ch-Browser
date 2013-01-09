package com.vortexwolf.dvach.common.library;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class SingleMediaScanner implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection mMs;
    private File mFile;

    public SingleMediaScanner(Context context, File f) {
        mFile = f;
        mMs = new MediaScannerConnection(context, this);
        mMs.connect();
    }
    
    @Override
    public void onMediaScannerConnected() {
        mMs.scanFile(mFile.getAbsolutePath(), null);
    }
    
    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
    }
}
