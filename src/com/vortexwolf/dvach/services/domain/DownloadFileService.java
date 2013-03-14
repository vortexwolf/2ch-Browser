package com.vortexwolf.dvach.services.domain;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;
import android.net.Uri;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.exceptions.DownloadFileException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;

public class DownloadFileService {
    private static final String TAG = "DownloadFileService";

    private final Resources mResources;
    private final DefaultHttpClient mHttpClient;

    public DownloadFileService(Resources resources, DefaultHttpClient httpClient) {
        this.mResources = resources;
        this.mHttpClient = httpClient;
    }

    public void downloadFile(Uri uri, File to, IProgressChangeListener progressChangeListener, ICancelled task) throws DownloadFileException {
        if (to.exists()) {
            throw new DownloadFileException(this.mResources.getString(R.string.error_file_exist));
        }

        HttpGet request = null;
        HttpResponse response = null;
        BufferedInputStream input = null;
        try {
            File fromFile = new File(uri.getPath());

            if (fromFile.exists()) {
                progressChangeListener.setContentLength(fromFile.length());
                input = new BufferedInputStream(new FileInputStream(fromFile));
            } else {
                request = new HttpGet(uri.toString());
                response = this.mHttpClient.execute(request);
                HttpEntity entity = response.getEntity();

                progressChangeListener.setContentLength(entity.getContentLength());
                input = new BufferedInputStream(entity.getContent());
            }

            this.SaveStream(input, to, progressChangeListener, task);

        } catch (DownloadFileException e) {
            throw e;
        } catch (Exception e) {
            MyLog.e(TAG, e);
            throw new DownloadFileException(this.mResources.getString(R.string.error_save_file));
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }

            ExtendedHttpClient.releaseRequestResponse(request, response);
        }
    }

    private void SaveStream(InputStream input, File to, IProgressChangeListener progressChangeListener, ICancelled task) throws Exception, DownloadFileException {
        OutputStream output = null;
        byte data[] = new byte[8192];
        int total = 0, count;
        boolean wasCancelled = false;

        try {
            output = new FileOutputStream(to);

            while ((count = input.read(data)) != -1) {
                if (task != null && task.isCancelled()) {
                    wasCancelled = true;
                    return;
                }

                total += count;
                output.write(data, 0, count);

                if (progressChangeListener != null) {
                    progressChangeListener.progressChanged(total);
                }
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
            wasCancelled = true;
            if (e instanceof FileNotFoundException) {
                throw new DownloadFileException(this.mResources.getString(R.string.error_download_no_space_sdcard));
            }

            throw e;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }

            if (wasCancelled) {
                to.delete();
            }
        }
    }

}
