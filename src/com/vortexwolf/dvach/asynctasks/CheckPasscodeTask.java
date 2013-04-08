package com.vortexwolf.dvach.asynctasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.vortexwolf.dvach.R;
import com.vortexwolf.dvach.common.library.ExtendedHttpClient;
import com.vortexwolf.dvach.common.library.MyLog;
import com.vortexwolf.dvach.common.utils.AppearanceUtils;
import com.vortexwolf.dvach.common.utils.StringUtils;

public class CheckPasscodeTask extends AsyncTask<Void, Void, String> {
    private Context mContext;
    private String mUri;
    private String mPasscode;

    public CheckPasscodeTask(Context context, Uri uri, String passcode) {
        this.mContext = context;
        this.mUri = uri.toString();
        this.mPasscode = passcode;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(this.mUri);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("task", "auth"));
            nameValuePairs.add(new BasicNameValuePair("usercode", this.mPasscode));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs);
            post.setEntity(entity);

            HttpClientParams.setRedirecting(post.getParams(), false);

            HttpResponse response = client.execute(post);

            String location = ExtendedHttpClient.getLocationHeader(response);

            post.abort();

            return location;
        } catch (Exception e) {
            MyLog.e("CheckPasscodeTask", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (StringUtils.isEmpty(this.mPasscode)) {
            return;
        }

        if (StringUtils.emptyIfNull(result).equals("/b/")) {
            AppearanceUtils.showToastMessage(this.mContext, this.mContext.getString(R.string.notification_passcode_correct));
        } else {
            AppearanceUtils.showToastMessage(this.mContext, this.mContext.getString(R.string.notification_passcode_incorrect));
        }
    }
}