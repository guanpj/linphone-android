package com.longrise.jie.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Jie on 2016-12-19.
 */

public class LinphoneMiniLoginActivity extends Activity
{
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aaa_login);

        tv = (TextView) findViewById(R.id.txt_username);
    }

}
