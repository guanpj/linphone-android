package com.longrise.jie.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;

/**
 * Created by Jie on 2016-12-20.
 */

public class LinphoneMiniService extends Service
{
    private static LinphoneMiniService mInstance;
    private LinphoneCoreListenerBase mListener;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        LinphoneMiniManager.createAndStart(LinphoneMiniService.this);

        mListener = new LinphoneCoreListenerBase()
        {
            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message)
            {
                super.callState(lc, call, state, message);
            }

            @Override
            public void globalState(LinphoneCore lc, LinphoneCore.GlobalState state, String message)
            {
                super.globalState(lc, state, message);
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage)
            {
                super.registrationState(lc, cfg, state, smessage);
            }
        };
        LinphoneMiniManager.getLc().addListener(mListener);
    }

    public static boolean isReady()
    {
        return mInstance != null;
    }

    @Override
    public synchronized void onDestroy()
    {
        LinphoneCore lc = LinphoneMiniManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null)
        {
            lc.removeListener(mListener);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
