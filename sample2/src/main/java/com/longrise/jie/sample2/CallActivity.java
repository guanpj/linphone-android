package com.longrise.jie.sample2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;

/**
 * Created by Jie on 2016-12-27.
 */
public class CallActivity extends Activity implements View.OnClickListener
{
    private Button btnVedio;
    private Button btnDecline;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_call);

        btnVedio = (Button) findViewById(R.id.btn_video);
        btnDecline = (Button) findViewById(R.id.btn_decline);

        btnVedio.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
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

        }
    }

    private void disableVideo(final boolean videoDisabled)
    {
        final LinphoneCall call = LinphoneMiniManager.getLc().getCurrentCall();
        if (call == null)
        {
            return;
        }

        if (videoDisabled)
        {
            LinphoneCallParams params = LinphoneMiniManager.getLc().createCallParams(call);
            params.setVideoEnabled(false);
            LinphoneMiniManager.getLc().updateCall(call, params);
        }
        else
        {
            LinphoneMiniManager.getInstance().addVideo();
        }
    }
}
