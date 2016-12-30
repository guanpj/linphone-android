package com.longrise.jie.sample2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;

import static android.content.Intent.ACTION_MAIN;

/**
 * Created by Jie on 2016-12-19.
 */

public class LinphoneMiniLoginActivity extends Activity implements View.OnClickListener, LinphoneAccountCreator.LinphoneAccountCreatorListener
{
    private LinphoneMiniPreferences mPrefs;
    private LinphoneCoreListenerBase mListener;
    private LinphoneAddress address;
    private LinphoneAccountCreator accountCreator;
    private Handler mHandler;
    private ServiceWaitThread mThread;
    private TextView tvUserName;
    private TextView tvPassword;
    private TextView tvDomain;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_login);

        initView();
        regEvent();

        mHandler = new Handler();
        mPrefs = LinphoneMiniPreferences.instance();

        if (LinphoneMiniService.isReady())
        {
            onServiceReady();
        }
        else
        {
            // start linphone as background
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneMiniService.class));
            mThread = new ServiceWaitThread();
            mThread.start();
        }
    }

    private void initView()
    {
        tvUserName = (TextView) findViewById(R.id.txt_username);
        tvPassword = (TextView) findViewById(R.id.txt_passwd);
        tvDomain = (TextView) findViewById(R.id.txt_domain);
        btnLogin = (Button) findViewById(R.id.btn_login);
    }

    private void regEvent()
    {
        btnLogin.setOnClickListener(this);
        mListener = new LinphoneCoreListenerBase()
        {
            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage)
            {
                if(state == LinphoneCore.RegistrationState.RegistrationOk)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(LinphoneMiniLoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent().setClass(LinphoneMiniLoginActivity.this, LinphoneMiniActivity.class));
                            finish();
                        }
                    });
                }
                else if(state == LinphoneCore.RegistrationState.RegistrationFailed)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(LinphoneMiniLoginActivity.this, "登录失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onClick(View view)
    {
        LinphoneAddress.TransportType transport = LinphoneAddress.TransportType.LinphoneTransportUdp;
            saveCreatedAccount(tvUserName.getText().toString(), tvPassword.getText().toString(),
                    null , null, tvDomain.getText().toString(), transport);

    }

    protected void onServiceReady()
    {
        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneMiniManager.getLc(), LinphoneMiniPreferences.instance().getXmlrpcUrl());
        accountCreator.setDomain(getResources().getString(R.string.default_domain));
        accountCreator.setListener(this);
        LinphoneMiniManager.getLc().addListener(mListener);
        btnLogin.setEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    public void saveCreatedAccount(String username, String password, String prefix, String ha1, String domain, LinphoneAddress.TransportType transport)
    {
        username = LinphoneMiniUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneMiniUtils.getDisplayableUsernameFromAddress(domain);

        String identity = "sip:" + username + "@" + domain;
        try
        {
            address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
        }
        catch (LinphoneCoreException e)
        {
            Log.e(e);
        }

        LinphoneMiniPreferences.AccountBuilder builder = new LinphoneMiniPreferences.AccountBuilder(LinphoneMiniManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setHa1(ha1)
                .setPassword(password)
                .setTransport(transport);

        if (getResources().getBoolean(R.bool.enable_push_id))
        {
            String regId = mPrefs.getPushNotificationRegistrationID();
            String appId = getString(R.string.push_sender_id);
            if (regId != null && mPrefs.isPushNotificationEnabled())
            {
                String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
                builder.setContactParameters(contactInfos);
            }
        }

        try
        {
            builder.saveNewAccount();
        }
        catch (LinphoneCoreException e)
        {
            Log.e(e);
        }
    }

    @Override
    public void onAccountCreatorIsAccountUsed(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {
    }

    @Override
    public void onAccountCreatorAccountCreated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {
    }

    @Override
    public void onAccountCreatorAccountActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorAccountLinkedWithPhoneNumber(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorPhoneNumberLinkActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorIsAccountActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorPhoneAccountRecovered(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorIsAccountLinked(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorIsPhoneNumberUsed(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    public void onAccountCreatorPasswordUpdated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status)
    {

    }

    @Override
    protected void onDestroy()
    {
        LinphoneMiniManager.getLc().removeListener(mListener);
        super.onDestroy();
    }

    private class ServiceWaitThread extends Thread
    {
        public void run()
        {
            while (!LinphoneMiniService.isReady())
            {
                try
                {
                    sleep(30);
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    onServiceReady();
                }
            });
            mThread = null;
        }
    }
}
