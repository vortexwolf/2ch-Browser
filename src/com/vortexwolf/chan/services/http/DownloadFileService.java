package com.vortexwolf.chan.services.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.res.Resources;
import android.net.Uri;

import com.vortexwolf.chan.R;
import com.vortexwolf.chan.common.library.ExtendedHttpClient;
import com.vortexwolf.chan.common.library.MyLog;
import com.vortexwolf.chan.common.utils.IoUtils;
import com.vortexwolf.chan.exceptions.DownloadFileException;
import com.vortexwolf.chan.exceptions.HttpRequestException;
import com.vortexwolf.chan.interfaces.ICancelled;
import com.vortexwolf.chan.interfaces.IProgressChangeListener;

public class DownloadFileService {
    private static final String TAG = "DownloadFileService";

    private final Resources mResources;
    private final HttpStreamReader mHttpStreamReader;

    public DownloadFileService(Resources resources, HttpStreamReader httpStreamReader) {
        this.mResources = resources;
        this.mHttpStreamReader = httpStreamReader;
    }

    public void downloadFile(Uri uri, File to, IProgressChangeListener listener, ICancelled task) throws DownloadFileException {
        if (to.exists()) {
            throw new DownloadFileException(this.mResources.getString(R.string.error_file_exist));
        }

        boolean wasCancelled = false;

        try {
            // check if the uri is a file uri
            File fromFile = new File(uri.getPath());

            if (fromFile.exists()) {
                this.saveFromFile(fromFile, to, listener, task);
            } else {
                this.saveFromUri(uri, to, listener, task);
            }

            if (task != null && task.isCancelled()) {
                wasCancelled = true;
            }
        } catch (FileNotFoundException e) {
            MyLog.e(TAG, e);
            wasCancelled = true;
            throw new DownloadFileException(this.mResources.getString(R.string.error_download_no_space_sdcard));
        } catch (HttpRequestException e) {
            MyLog.e(TAG, e);
            wasCancelled = true;
            if (e.getMessage().startsWith("503")) { throw new DownloadFileException("503"); }
            else throw new DownloadFileException(this.mResources.getString(R.string.error_save_file));
        } catch (Exception e) {
            MyLog.e(TAG, e);
            wasCancelled = true;
            throw new DownloadFileException(this.mResources.getString(R.string.error_save_file));
        } finally {
            if (wasCancelled) {
                to.delete();
            }
        }
    }

    private void saveFromUri(Uri uri, File to, IProgressChangeListener listener, ICancelled task) throws HttpRequestException, IOException {
        HttpStreamModel streamModel = null;
        try {
            streamModel = this.mHttpStreamReader.fromUri(uri.toString(), false, null, listener, task);

            this.saveStream(streamModel.stream, to);
        } finally {
            if (streamModel != null) {
                ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
            }
        }
    }

    private void saveFromFile(File from, File to, IProgressChangeListener listener, ICancelled task) throws HttpRequestException, IOException {
        InputStream input = null;
        try {
            input = IoUtils.modifyInputStream(new FileInputStream(from), from.length(), listener, task);

            this.saveStream(input, to);
        } finally {
            IoUtils.closeStream(input);
        }
    }

    private void saveStream(InputStream input, File to) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(to);
            IoUtils.copyStream(input, output);
        } finally {
            IoUtils.closeStream(output);
        }
    }
}
