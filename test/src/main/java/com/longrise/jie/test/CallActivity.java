package com.longrise.jie.test;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.mediastream.Log;

/**
 * Created by Jie on 2016-12-27.
 */
public class CallActivity extends Activity implements View.OnClickListener
{
    private LinphoneCoreListenerBase mListener;
    private Button btnVedio;
    private Button btnDecline;
    private CallVideoFragment videoCallFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_call);

        btnVedio = (Button) findViewById(R.id.btn_video);
        btnDecline = (Button) findViewById(R.id.btn_decline);

        btnVedio.setOnClickListener(this);
        btnDecline.setOnClickListener(this);

        mListener = new LinphoneCoreListenerBase()
        {
            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message)
            {
                if (state == LinphoneCall.State.StreamsRunning)
                {
                    switchVideo(isVideoEnabled(call));
                }
                else if (state == LinphoneCall.State.CallEnd || state == LinphoneCall.State.Error)
                {
                    finish();
                }
                else if (state == LinphoneCall.State.CallUpdatedByRemote)
                {
                    acceptCallUpdate(true);
                }
            }
        };
        LinphoneMiniManager.getLc().addListener(mListener);
    }

    public void acceptCallUpdate(boolean accept)
    {
        LinphoneCall call = LinphoneMiniManager.getLc().getCurrentCall();
        if (call == null)
        {
            return;
        }

        LinphoneCallParams params = LinphoneMiniManager.getLc().createCallParams(call);
        if (accept)
        {
            params.setVideoEnabled(true);
            LinphoneMiniManager.getLc().enableVideo(true, true);
        }
        try
        {
            LinphoneMiniManager.getLc().acceptCallUpdate(call, params);
        }
        catch (LinphoneCoreException e)
        {
            Log.e(e);
        }
    }

    private boolean isVideoEnabled(LinphoneCall call)
    {
        if (call != null)
        {
            return call.getCurrentParamsCopy().getVideoEnabled();
        }
        return false;
    }

    private void switchVideo(final boolean displayVideo)
    {
        final LinphoneCall call = LinphoneMiniManager.getLc().getCurrentCall();
        if (call == null)
        {
            return;
        }

        if (call.getState() == LinphoneCall.State.CallEnd
                || call.getState() == LinphoneCall.State.CallReleased)
            return;

        if (!displayVideo)
        {
        }
        else
        {
            LinphoneMiniManager.getInstance().addVideo();
            showVideoView();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showVideoView()
    {
        videoCallFragment = new CallVideoFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, videoCallFragment);
        try
        {
            transaction.commitAllowingStateLoss();
        }
        catch (Exception e)
        {
        }
    }

    public void bindVideoFragment(CallVideoFragment fragment)
    {
        videoCallFragment = fragment;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btn_video)
        {
            disableVideo(false);
        }
        else if (v.getId() == R.id.btn_decline)
        {
            LinphoneCall call = LinphoneMiniManager.getLc().getCurrentCall();
            if (call != null)
            {
                LinphoneMiniManager.getLc().terminateCall(call);
                finish();
            }
        }
    }

    private void disableVideo(final boolean videoDisabled)
    {
        final LinphoneCall call = LinphoneMiniManager.getLc().getCurrentCall();
        if (call == null)
        {
            return;
        }
        LinphoneCallParams params = LinphoneMiniManager.getLc().createCallParams(call);
        if (videoDisabled)
        {
            params.setVideoEnabled(false);
        }
        else
        {
            LinphoneMiniManager.getInstance().addVideo();
            params.setVideoEnabled(true);
        }
        LinphoneMiniManager.getLc().updateCall(call, params);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LinphoneMiniManager.getLc().removeListener(mListener);
    }
}
