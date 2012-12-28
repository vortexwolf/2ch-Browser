package com.vortexwolf.dvach.services;

import java.util.Timer;
import java.util.TimerTask;

import com.vortexwolf.dvach.common.library.MyLog;

import android.app.Activity;

public class TimerService {

    private Timer mTimer = null;
    private final Runnable mTask;
    private final Activity mActivity;

    private boolean mEnabled;
    private int mInterval;

    public TimerService(boolean isEnabled, int interval, Runnable task, Activity activity) {
        this.mTask = task;
        this.mEnabled = isEnabled;
        this.mInterval = interval;
        this.mActivity = activity;
    }

    public void start() {
        if (!this.mEnabled) {
            return;
        }

        this.mTimer = new Timer();
        this.mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mActivity.runOnUiThread(mTask);
            }
        }, mInterval * 1000, mInterval * 1000);
    }

    public void stop() {
        if (this.mTimer != null) {
            this.mTimer.cancel();
        }
    }

    public void update(boolean isEnabled, int interval) {
        if (this.mEnabled == isEnabled && this.mInterval == interval) {
            return;
        }

        MyLog.v("TimerService", "AutoRefresh settings were changed");

        this.stop();

        this.mEnabled = isEnabled;
        this.mInterval = interval;

        this.start();
    }
}
