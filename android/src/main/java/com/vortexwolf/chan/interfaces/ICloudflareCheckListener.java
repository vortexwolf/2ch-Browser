package com.vortexwolf.chan.interfaces;

public interface ICloudflareCheckListener {
    void onStart();
    void onSuccess();
    void onTimeout();
}
