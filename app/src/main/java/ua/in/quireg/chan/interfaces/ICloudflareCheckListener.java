package ua.in.quireg.chan.interfaces;

public interface ICloudflareCheckListener {
    void onStart();
    void onSuccess();
    void onTimeout();
}
