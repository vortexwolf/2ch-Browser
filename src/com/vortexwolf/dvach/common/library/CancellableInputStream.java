package com.vortexwolf.dvach.common.library;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ser.FilteredBeanPropertyWriter;

import com.vortexwolf.dvach.interfaces.ICancellable;

public class CancellableInputStream extends FilterInputStream {
	public static final String TAG = "CancellableInputStream";
	
	private final ICancellable mTask;
	
	public CancellableInputStream(InputStream in, ICancellable task) {
		super(in);
		this.mTask = task;
	}
	
    @Override
    public int read() throws IOException {
    	this.checkCancelled();
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
    	this.checkCancelled();
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
    	this.checkCancelled();
        return super.read(b, off, len);
    }

	private boolean checkCancelled(){
		if(mTask != null && mTask.isCancelled()){
			MyLog.v(TAG, "stream reading was cancelled");
			try{
				this.close();
			}
			catch (IOException e){
				MyLog.e(TAG, e);
			}
			
	        return true;
		}

		return false;
	}
}
