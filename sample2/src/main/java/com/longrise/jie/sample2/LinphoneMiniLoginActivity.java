package com.longrise.jie.sample2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.mediastream.Log;

import static android.content.Intent.ACTION_MAIN;

/**
 * Created by Jie on 2016-12-19.
 */

public class LinphoneMiniLoginActivity extends Activity implements View.OnClickListener, LinphoneAccountCreator.LinphoneAccountCreatorListener
{
    private LinphoneMiniPreferences mPrefs;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermission();
        }
        else
        {
            initErrorLogDetactor();
        }

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

    private void requestPermission()
    {
        if (PermissionsChecker.checkPermissions(this, PermissionsChecker.storagePermissions))
        {
            initErrorLogDetactor();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsChecker.REQUEST_STORAGE_PERMISSION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                initErrorLogDetactor();
            }
            else
            {
                Toast.makeText(LinphoneMiniLoginActivity.this, "000", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initErrorLogDetactor()
    {
        UncaughtException mUncaughtException = UncaughtException.getInstance();
        mUncaughtException.init(this, getString(R.string.app_name));
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
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onClick(View view)
    {
        if (!TextUtils.isEmpty(tvUserName.getText())
                && !TextUtils.isEmpty(tvPassword.getText())
                && !TextUtils.isEmpty(tvDomain.getText()))
        {
            LinphoneAddress.TransportType transport = LinphoneAddress.TransportType.LinphoneTransportUdp;
            saveCreatedAccount(tvUserName.getText().toString(), tvPassword.getText().toString(),
                    null , null, tvDomain.getText().toString(), transport);
        }
        else
        {
            Toast.makeText(LinphoneMiniLoginActivity.this, "三者都不能为空", Toast.LENGTH_LONG).show();
        }
    }

    protected void onServiceReady()
    {
        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneMiniManager.getLc(), LinphoneMiniPreferences.instance().getXmlrpcUrl());
        accountCreator.setDomain(getResources().getString(R.string.default_domain));
        accountCreator.setListener(this);
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

        boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
        LinphoneMiniPreferences.AccountBuilder builder = new LinphoneMiniPreferences.AccountBuilder(LinphoneMiniManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setHa1(ha1)
                .setPassword(password);

        if (prefix != null)
        {
            builder.setPrefix(prefix);
        }

        if (isMainAccountLinphoneDotOrg)
        {
            if (getResources().getBoolean(R.bool.disable_all_security_features_for_markets))
            {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTcp);
            }
            else
            {
                builder.setProxy(domain)
                        .setTransport(LinphoneAddress.TransportType.LinphoneTransportTls);
            }

            builder.setExpires("604800")
                    .setAvpfEnabled(true)
                    .setAvpfRRInterval(3)
                    .setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
                    .setQualityReportingEnabled(true)
                    .setQualityReportingInterval(180)
                    .setRealm("sip.linphone.org")
                    .setNoDefault(false);

            mPrefs.enabledFriendlistSubscription(getResources().getBoolean(R.bool.use_friendlist_subscription));

            mPrefs.setStunServer(getString(R.string.default_stun));
            mPrefs.setIceEnabled(true);

            accountCreator.setUsername(username);
            accountCreator.setPassword(password);
            accountCreator.setHa1(ha1);
        }
        else
        {
            String forcedProxy = "";
            if (!TextUtils.isEmpty(forcedProxy))
            {
                builder.setProxy(forcedProxy)
                        .setOutboundProxyEnabled(true)
                        .setAvpfRRInterval(5);
            }

            if (transport != null)
            {
                builder.setTransport(transport);
            }
        }

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
        if(status.equals(LinphoneAccountCreator.Status.AccountExistWithAlias))
        {
            startActivity(new Intent().setClass(this, LinphoneMiniActivity.class).putExtra("isNewProxyConfig", true));
            finish();
        }
        else
        {

        }
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
