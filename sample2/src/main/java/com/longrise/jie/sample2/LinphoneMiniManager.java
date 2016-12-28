package com.longrise.jie.sample2;
/*
LinphoneMiniManager.java
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

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.EcCalibratorStatus;
import org.linphone.core.LinphoneCore.GlobalState;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCore.RemoteProvisioningState;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneFriendList;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration.AndroidCamera;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import static android.media.AudioManager.STREAM_VOICE_CALL;

/**
 * @author Sylvain Berfini
 */
public class LinphoneMiniManager implements LinphoneCoreListener
{
    private static LinphoneMiniManager mInstance;
    private AudioManager mAudioManager;
    private Context mContext;
    private LinphoneCore mLinphoneCore;
    private Timer mTimer;

    public String mLinphoneConfigFile;

    private LinphoneMiniManager(Context context)
    {
        mContext = context;
        mInstance = this;
        try
        {
            String basePath = mContext.getFilesDir().getAbsolutePath();
            mLinphoneConfigFile = basePath + "/.linphonerc";
            copyAssetsFromPackage(basePath);
            mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(this,
                    mLinphoneConfigFile, mLinphoneConfigFile, null, mContext);
            initLinphoneCoreValues(basePath);

            setUserAgent();
            setFrontCamAsDefault();
            startIterate();
            mAudioManager = ((AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE));
            mLinphoneCore.setNetworkReachable(true); // Let's assume it's true
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public synchronized static final LinphoneMiniManager createAndStart(Context c)
    {
        if(mInstance == null)
        {
            mInstance = new LinphoneMiniManager(c);
        }
        return mInstance;
    }

    public LinphoneMiniManager()
    {
        super();
    }

    public static LinphoneMiniManager getInstance()
    {
        return mInstance;
    }

    public static synchronized final LinphoneCore getLc()
    {
        return getInstance().mLinphoneCore;
    }

    public Context getContext()
    {
        return mContext;
    }

    public void destroy()
    {
        try
        {
            mTimer.cancel();
            mLinphoneCore.destroy();
        }
        catch (RuntimeException e)
        {
        }
        finally
        {
            mLinphoneCore = null;
            mInstance = null;
        }
    }

    private void startIterate()
    {
        TimerTask lTask = new TimerTask()
        {
            @Override
            public void run()
            {
                mLinphoneCore.iterate();
            }
        };

		/*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = new Timer("LinphoneMini scheduler");
        mTimer.schedule(lTask, 0, 20);
    }

    private void setUserAgent()
    {
        try
        {
            String versionName = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
            if (versionName == null)
            {
                versionName = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("LinphoneMiniAndroid", versionName);
        }
        catch (NameNotFoundException e)
        {
        }
    }

    private void setFrontCamAsDefault()
    {
        int camId = 0;
        AndroidCamera[] cameras = AndroidCameraConfiguration.retrieveCameras();
        for (AndroidCamera androidCamera : cameras)
        {
            if (androidCamera.frontFacing)
                camId = androidCamera.id;
        }
        mLinphoneCore.setVideoDevice(camId);
    }

    private void copyAssetsFromPackage(String basePath) throws IOException
    {
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.ringback, basePath + "/ringback.wav");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.hold, basePath + "/hold.mkv");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.linphonerc_default, mLinphoneConfigFile);
        LinphoneMiniUtils.copyFromPackage(mContext, R.raw.linphonerc_factory, new File(mLinphoneConfigFile).getName());
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.lpconfig, basePath + "/lpconfig.xsd");
        LinphoneMiniUtils.copyIfNotExist(mContext, R.raw.rootca, basePath + "/rootca.pem");
    }

    private void initLinphoneCoreValues(String basePath)
    {
        mLinphoneCore.setContext(mContext);
        mLinphoneCore.setRing(null);
        mLinphoneCore.setRootCA(basePath + "/rootca.pem");
        mLinphoneCore.setPlayFile(basePath + "/hold.mkv");
        mLinphoneCore.setChatDatabasePath(basePath + "/linphone-history.db");

        int availableCores = Runtime.getRuntime().availableProcessors();
        mLinphoneCore.setCpuCount(availableCores);
    }

    public static synchronized LinphoneCore getLcIfManagerNotDestroyedOrNull()
    {
        if (mInstance == null)
        {
            return null;
        }
        return getLc();
    }

    public static final boolean isInstanciated()
    {
        return mInstance != null;
    }


    @Override
    public void globalState(LinphoneCore lc, GlobalState state, String message)
    {
        Log.d("Global state: " + state + "(" + message + ")");
    }

    @Override
    public void callState(LinphoneCore lc, LinphoneCall call, State cstate,
                          String message)
    {
        if (cstate == State.OutgoingInit)
        {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager.requestAudioFocus(null, STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT );
        }
    }

    public boolean addVideo()
    {
        LinphoneCall call = mLinphoneCore.getCurrentCall();
        call.enableCamera(true);
        return reinviteWithVideo();
    }

    boolean reinviteWithVideo()
    {
        LinphoneCall lCall = mLinphoneCore.getCurrentCall();
        if (lCall == null) {
            Log.e("Trying to reinviteWithVideo while not in call: doing nothing");
            return false;
        }
        LinphoneCallParams params = lCall.getCurrentParamsCopy();

        if (params.getVideoEnabled()) return false;

        // Abort if not enough bandwidth...
        if (!params.getVideoEnabled()) {
            return false;
        }

        // Not yet in video call: try to re-invite with video
        mLinphoneCore.updateCall(lCall, params);
        return true;
    }

    @Override
    public void authInfoRequested(LinphoneCore lc, String realm, String username, String domain)
    {

    }

    @Override
    public void authenticationRequested(LinphoneCore lc, LinphoneAuthInfo authInfo, LinphoneCore.AuthMethod method)
    {

    }

    @Override
    public void callStatsUpdated(LinphoneCore lc, LinphoneCall call,
                                 LinphoneCallStats stats)
    {

    }

    @Override
    public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call,
                                      boolean encrypted, String authenticationToken)
    {

    }

    @Override
    public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,
                                  RegistrationState cstate, String smessage)
    {
        Log.d("Registration state: " + cstate + "(" + smessage + ")");
    }

    @Override
    public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf,
                                       String url)
    {

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf)
    {

    }


    @Override
    public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr,
                                LinphoneChatMessage message)
    {
        Log.d("Message received from " + cr.getPeerAddress().asString() + " : " + message.getText() + "(" + message.getExternalBodyUrl() + ")");
    }

    @Override
    public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr)
    {
        Log.d("Composing received from " + cr.getPeerAddress().asString());
    }

    @Override
    public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf)
    {

    }

    @Override
    public void ecCalibrationStatus(LinphoneCore lc, EcCalibratorStatus status,
                                    int delay_ms, Object data)
    {

    }

    @Override
    public void uploadProgressIndication(LinphoneCore lc, int offset, int total)
    {

    }

    @Override
    public void uploadStateChanged(LinphoneCore lc, LinphoneCore.LogCollectionUploadState state, String info)
    {

    }

    @Override
    public void friendListCreated(LinphoneCore lc, LinphoneFriendList list)
    {

    }

    @Override
    public void friendListRemoved(LinphoneCore lc, LinphoneFriendList list)
    {

    }

    @Override
    public void notifyReceived(LinphoneCore lc, LinphoneCall call,
                               LinphoneAddress from, byte[] event)
    {

    }

    @Override
    public void transferState(LinphoneCore lc, LinphoneCall call,
                              State new_call_state)
    {

    }

    @Override
    public void infoReceived(LinphoneCore lc, LinphoneCall call,
                             LinphoneInfoMessage info)
    {

    }

    @Override
    public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev,
                                         SubscriptionState state)
    {

    }

    @Override
    public void notifyReceived(LinphoneCore lc, LinphoneEvent ev,
                               String eventName, LinphoneContent content)
    {
        Log.d("Notify received: " + eventName + " -> " + content.getDataAsString());
    }

    @Override
    public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev,
                                    PublishState state)
    {

    }

    @Override
    public void configuringStatus(LinphoneCore lc,
                                  RemoteProvisioningState state, String message)
    {
        Log.d("Configuration state: " + state + "(" + message + ")");
    }

    @Override
    public void show(LinphoneCore lc)
    {

    }

    @Override
    public void displayStatus(LinphoneCore lc, String message)
    {

    }

    @Override
    public void displayMessage(LinphoneCore lc, String message)
    {

    }

    @Override
    public void displayWarning(LinphoneCore lc, String message)
    {

    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore lc, LinphoneChatMessage message, LinphoneContent content, int progress)
    {

    }

    @Override
    public void fileTransferRecv(LinphoneCore lc, LinphoneChatMessage message, LinphoneContent content, byte[] buffer, int size)
    {

    }

    @Override
    public int fileTransferSend(LinphoneCore lc, LinphoneChatMessage message, LinphoneContent content, ByteBuffer buffer, int size)
    {
        return 0;
    }

}
