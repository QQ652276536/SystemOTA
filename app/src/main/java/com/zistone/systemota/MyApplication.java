package com.zistone.systemota;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        PRDownloader.initialize(getApplicationContext());
        // Setting timeout globally for the download network requests:
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder().setReadTimeout(30_000).setConnectTimeout(30_000).build();
        PRDownloader.initialize(getApplicationContext(), config);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
