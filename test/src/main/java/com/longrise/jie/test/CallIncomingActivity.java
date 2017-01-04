package com.longrise.jie.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;

/**
 * Created by Jie on 2016-12-29.
 */

public class CallIncomingActivity extends Activity implements View.OnClickListener
{
    LinphoneCoreListenerBase mListener;
    LinphoneCall mCall;
    private TextView txtNum;
    private Button btnHungUp;
    private Button btnAnswer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_incoming);

        txtNum = (TextView) findViewById(R.id.txt_num);
        btnHungUp = (Button) findViewById(R.id.btn_hungup);
        btnAnswer = (Button) findViewById(R.id.btn_answer);

        btnHungUp.setOnClickListener(this);
        btnAnswer.setOnClickListener(this);

        mListener = new LinphoneCoreListenerBase()
        {
            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message)
            {
                if (state == LinphoneCall.State.StreamsRunning)
                {
                    LinphoneMiniManager.getLc().enableSpeaker(LinphoneMiniManager.getLc().isSpeakerEnabled());
                }
                if (LinphoneCall.State.CallEnd == state)
                {
                    finish();
                }
            }
        };
        LinphoneMiniManager.getLc().addListener(mListener);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mCall = LinphoneMiniManager.getLc().getCurrentCall();
        if(mCall != null)
        {
            LinphoneAddress address = mCall.getRemoteAddress();
            txtNum.setText(LinphoneMiniUtils.getAddressDisplayName(address) +
                    "    " + address.asStringUriOnly());
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btn_hungup)
        {
            if(mCall != null)
            {
                LinphoneMiniManager.getLc().terminateCall(mCall);
                finish();
            }
        }
        else if (v.getId() == R.id.btn_answer)
        {
            answer();
        }
    }

    private void answer()
    {
        try
        {
            if(mCall != null)
            {
                LinphoneMiniManager.getLc().acceptCall(mCall);
                finish();
            }
        }
        catch (LinphoneCoreException e)
        {
            e.printStackTrace();
        }
    }
}
