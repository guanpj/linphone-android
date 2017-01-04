package com.longrise.jie.test;
/*
LinphoneMiniUtils.java
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneProxyConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Sylvain Berfini
 */
public class LinphoneMiniUtils
{
    public static void copyIfNotExist(Context context, int ressourceId, String target) throws IOException
    {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists())
        {
            copyFromPackage(context, ressourceId, lFileToCopy.getName());
        }
    }

    public static void copyFromPackage(Context context, int ressourceId, String target) throws IOException
    {
        FileOutputStream lOutputStream = context.openFileOutput(target, 0);
        InputStream lInputStream = context.getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1)
        {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    public static String getAddressDisplayName(LinphoneAddress address)
    {
        if (address.getDisplayName() != null)
        {
            return address.getDisplayName();
        }
        else
        {
            if (address.getUserName() != null)
            {
                return address.getUserName();
            }
            else
            {
                return address.asStringUriOnly();
            }
        }
    }

    public static String getDisplayableUsernameFromAddress(String sipAddress)
    {
        String username = sipAddress;
        LinphoneCore lc = LinphoneMiniManager.getLcIfManagerNotDestroyedOrNull();
        if (lc == null) return username;

        if (username.startsWith("sip:"))
        {
            username = username.substring(4);
        }

        if (username.contains("@"))
        {
            String domain = username.split("@")[1];
            LinphoneProxyConfig lpc = lc.getDefaultProxyConfig();
            if (lpc != null)
            {
                if (domain.equals(lpc.getDomain()))
                {
                    return username.split("@")[0];
                }
            }
            else
            {
                if (domain.equals(LinphoneMiniManager.getInstance().getContext().getString(R.string.default_domain)))
                {
                    return username.split("@")[0];
                }
            }
        }
        return username;
    }

    public static boolean isHighBandwidthConnection(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType)
    {
        if (type == ConnectivityManager.TYPE_MOBILE)
        {
            switch (subType)
            {
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false;
            }
        }
        //in doubt, assume connection is good.
        return true;
    }
}
