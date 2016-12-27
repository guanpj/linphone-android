package com.longrise.jie.sample2;
/*
LinphoneMiniActivity.java
Copyright (C) 2014  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;

import static com.longrise.jie.sample2.LinphoneMiniManager.getLc;

/**
 * @author Sylvain Berfini
 */
public class LinphoneMiniActivity extends Activity implements View.OnClickListener
{
    private LinphoneCoreListenerBase mListener;
    private LinphoneCall mCall;
    private LinearLayout lyDail;
    private LinearLayout lyCall;
    private Button btnDecline;
    private EditText txtNum;
    private Button btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        initView();
        regEvent();
    }

    private void initView()
    {
        lyDail = (LinearLayout) findViewById(R.id.dail_layout);
        lyCall = (LinearLayout) findViewById(R.id.call_layout);
        btnDecline = (Button) findViewById(R.id.btn_decline);
        txtNum = (EditText) findViewById(R.id.txt_num);
        btnCall = (Button) findViewById(R.id.btn_call);
    }

    private void regEvent()
    {
        btnCall.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
        mListener = new LinphoneCoreListenerBase()
        {
            @Override
            public void callState(LinphoneCore linphoneCore, LinphoneCall linphoneCall, LinphoneCall.State state, String s)
            {
                if (state == LinphoneCall.State.OutgoingInit || state == LinphoneCall.State.OutgoingProgress)
                {
                    mCall = linphoneCall;
                    lyDail.setVisibility(View.GONE);
                    lyCall.setVisibility(View.VISIBLE);
                }
                else if(state == LinphoneCall.State.Connected)
                {
                    Intent intent = new Intent(LinphoneMiniActivity.this, CallActivity.class);
                    startActivity(intent);
                }
            }
        };
        LinphoneMiniManager.getLc().addListener(mListener);
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.btn_call)
        {
            if (!TextUtils.isEmpty(txtNum.getText()))
            {
                newOutgoingCall(txtNum.getText().toString());
            }
            else
            {
                Toast.makeText(this, "输入号码！", Toast.LENGTH_SHORT).show();
            }
        }
        else if(v.getId() == R.id.btn_decline)
        {
            LinphoneMiniManager.getLc().terminateCall(mCall);
            lyDail.setVisibility(View.VISIBLE);
            lyCall.setVisibility(View.GONE);
        }
    }

    public void newOutgoingCall(String to)
    {
        if (to == null) return;

        LinphoneProxyConfig lpc = getLc().getDefaultProxyConfig();
        if (lpc != null)
        {
            to = lpc.normalizePhoneNumber(to);
        }

        LinphoneAddress lAddress;
        try
        {
            lAddress = LinphoneMiniManager.getLc().interpretUrl(to);
        }
        catch (LinphoneCoreException e)
        {
            Log.e(e);
            return;
        }
        lAddress.setDisplayName(null);

        boolean isLowBandwidthConnection = !LinphoneMiniUtils.isHighBandwidthConnection(LinphoneMiniService.instance().getApplicationContext());

        try
        {
            inviteAddress(lAddress, false, isLowBandwidthConnection);
        }
        catch (LinphoneCoreException e)
        {
            Toast.makeText(this, "呼叫失败！", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void inviteAddress(LinphoneAddress lAddress, boolean videoEnabled, boolean lowBandwidth) throws LinphoneCoreException
    {
        LinphoneCore lc = LinphoneMiniManager.getLc();
        LinphoneCallParams params = lc.createCallParams(null);

        if (videoEnabled && params.getVideoEnabled())
        {
            params.setVideoEnabled(true);
        }
        else
        {
            params.setVideoEnabled(false);
        }

        if (lowBandwidth)
        {
            params.enableLowBandwidth(true);
            Log.d("Low bandwidth enabled in call params");
        }

        lc.inviteAddressWithParams(lAddress, params);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
