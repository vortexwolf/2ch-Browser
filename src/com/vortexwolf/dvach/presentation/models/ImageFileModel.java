package com.vortexwolf.dvach.presentation.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.vortexwolf.dvach.activities.files.SerializableFileModel;
import com.vortexwolf.dvach.common.library.MyLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageFileModel {
	
	public static final int IMAGE_MAX_SIZE = 70;
	
	public File file;
	public int imageHeight;
	public int imageWidth;
	
	private Bitmap mBitmap;
	
	
	protected ImageFileModel() {}
	
	public ImageFileModel(String filePath){
		this(new File(filePath));
	}
	
	public ImageFileModel(File file){
		this.file = file;
		
		//Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        
        this.readBitmapFromFile(o);

        this.imageHeight = o.outHeight;
        this.imageWidth = o.outWidth;
	}
	
	public int getFileSize(){
		return (int)Math.round(file.length() / 1024.0);
	}
	
	public Bitmap getBitmap(){
		if(mBitmap == null) {
			int scale = 1;
	        if (this.imageHeight > IMAGE_MAX_SIZE || this.imageWidth > IMAGE_MAX_SIZE) {
	            scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(this.imageHeight, this.imageWidth)) / Math.log(0.5)));
	        }
	
	        //Decode with inSampleSize
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inSampleSize = scale;
	        
	        mBitmap = this.readBitmapFromFile(o);
		}
		
        return mBitmap;
	}
	
	private Bitmap readBitmapFromFile(BitmapFactory.Options o){
		try{
	        FileInputStream fis = new FileInputStream(this.file);
	        Bitmap b = BitmapFactory.decodeStream(fis, null, o);
	        fis.close();
	        
	        return b;
		}
		catch(IOException e){
			MyLog.e("FileModel", e);
		}
		
		return null;
	}
}
