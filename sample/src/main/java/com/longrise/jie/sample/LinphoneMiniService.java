package com.longrise.jie.sample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Jie on 2016-12-20.
 */

public class LinphoneMiniService extends Service
{
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
