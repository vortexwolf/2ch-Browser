package android.httpimage;


import java.io.ByteArrayOutputStream;

import com.vortexwolf.dvach.common.library.MyLog;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;


/**
 * DB implementation of persistent storage.
 * 
 * @author zonghai@gmail.com
 */
public class DBPersistence implements BitmapCache{
    
    private static final String TAG = "DBPersistence";
    
    public DBPersistence(Context context) {
        this.mContext = context;
    }
    
    
    @Override
	public boolean exists(String key) {
        //TODO
        return false;
    }


    @Override
	public Bitmap loadData(String key) {
        Bitmap bitmap = null;
        
        Uri image = Uri.withAppendedPath(DBImageTable.CONTENT_URI, key);
        MyLog.d(TAG, "loaddata " + image.toString());
        String[] returnCollums = new String[] {
            DBImageTable.DATA,
        };
        
        Cursor c = null;
        try {
            ContentResolver cr = mContext.getContentResolver();
            c = cr.query(image, returnCollums, null, null, null);
            MyLog.d(TAG, "count=" + c.getCount());
            if(c.getCount() < 1) {
                return null;
            }
            if(c .getCount() > 1) {
                throw new RuntimeException("shouldn't reach here, make sure the NAME collumn is unique: " + key);
            }
            c.moveToFirst();
            byte[] binary = c.getBlob(c.getColumnIndex(DBImageTable.DATA));
            if( binary != null ) {
                bitmap = BitmapFactory.decodeByteArray(binary, 0, binary.length);
                if(bitmap == null) {
                     // something wrong with the persistent data, can't be decoded to bitmap.
                    throw new RuntimeException("data from db can't be decoded to bitmap");
                }
            }
            return bitmap;
        }
        finally{
            if(c != null){
                c.close();
            }
        }
    }

    
    @Override
	public void storeData(String key, Bitmap data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if( data.compress(CompressFormat.PNG, 100, out)) {
            byte[] ba = out.toByteArray();
            ContentValues values = new ContentValues();
            values.put(DBImageTable.NAME, key);
            values.put(DBImageTable.DATA, ba);
            values.put(DBImageTable.SIZE, ba.length);
            values.put(DBImageTable.NUSE, 1);
            values.put(DBImageTable.TIMESTAMP, System.currentTimeMillis());
            mContext.getContentResolver().insert(DBImageTable.CONTENT_URI, values);
        }
    }

    
    @Override
    public void clear() {
        //TODO
    }


    @Override
    public void invalidate(String key) {
        //TODO:
    }
    

    private Context mContext;

}
