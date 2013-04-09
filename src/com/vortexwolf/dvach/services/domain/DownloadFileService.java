package com.vortexwolf.dvach.services.domain;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.res.Resources;
import android.net.Uri;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.IoUtils;
import com.vortexwolf.dvach.exceptions.DownloadFileException;
import com.vortexwolf.dvach.exceptions.HttpRequestException;
import com.vortexwolf.dvach.interfaces.ICancelled;
import com.vortexwolf.dvach.interfaces.IProgressChangeListener;
import com.vortexwolf.dvach.models.domain.HttpStreamModel;

public class DownloadFileService {
    private static final String TAG = "DownloadFileService";

    private final Resources mResources;
    private final DefaultHttpClient mHttpClient;
    private final HttpStreamReader mHttpStreamReader;

    public DownloadFileService(Resources resources, DefaultHttpClient httpClient) {
        this.mResources = resources;
        this.mHttpClient = httpClient;
        this.mHttpStreamReader = new HttpStreamReader(httpClient, resources);
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
        } 
        catch (FileNotFoundException e) {
            MyLog.e(TAG, e);
            wasCancelled = true;
            throw new DownloadFileException(this.mResources.getString(R.string.error_download_no_space_sdcard));
        }
        catch (Exception e) {
            MyLog.e(TAG, e);
            wasCancelled = true;
            throw new DownloadFileException(this.mResources.getString(R.string.error_save_file));
        } finally {
            if (wasCancelled) {
                to.delete();
            }
        }
    }
    
    private void saveFromUri(Uri uri, File to, IProgressChangeListener listener, ICancelled task) throws HttpRequestException, IOException{
        HttpStreamModel streamModel = null;
        try {
            streamModel = this.mHttpStreamReader.fromUri(uri.toString(), null, listener, task);
        
            this.saveStream(streamModel.stream, to);
        } finally {
            if (streamModel != null) {
                ExtendedHttpClient.releaseRequestResponse(streamModel.request, streamModel.response);
            }
        }
    }
    
    private void saveFromFile(File from, File to, IProgressChangeListener listener, ICancelled task) throws HttpRequestException, IOException{
        InputStream input = null;
        try {
            input = IoUtils.modifyInputStream(new FileInputStream(from), from.length(), listener, task);
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
