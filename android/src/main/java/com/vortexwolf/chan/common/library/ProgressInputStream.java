/**
 * Code taken from stackoverflow user Adamski:
 * http://stackoverflow.com/questions/1339437/inputstream-or-reader-wrapper-for-progress-reporting/1339589#1339589
 */
package com.vortexwolf.chan.common.library;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.vortexwolf.chan.interfaces.IProgressChangeListener;

public class ProgressInputStream extends FilterInputStream {
    public static final String TAG = "ProgressInputStream";
    private final List<IProgressChangeListener> mListeners = new ArrayList<IProgressChangeListener>();
    private volatile long mTotalNumBytesRead;

    public ProgressInputStream(InputStream in) {
        super(in);
    }

    public long getTotalNumBytesRead() {
        return this.mTotalNumBytesRead;
    }

    public void addProgressChangeListener(IProgressChangeListener l) {
        this.mListeners.add(l);
    }

    public void removeProgressChangeListener(IProgressChangeListener l) {
        this.mListeners.remove(l);
    }

    @Override
    public int read() throws IOException {
        return (int) this.updateProgress(super.read());
    }

    @Override
    public int read(byte[] b) throws IOException {
        return (int) this.updateProgress(super.read(b));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return (int) this.updateProgress(super.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return this.updateProgress(super.skip(n));
    }

    private long updateProgress(long numBytesRead) {
        if (numBytesRead > 0) {
            this.mTotalNumBytesRead += numBytesRead;
            for (IProgressChangeListener l : this.mListeners) {
                l.progressChanged(this.mTotalNumBytesRead);
            }
        }

        return numBytesRead;
    }
}
