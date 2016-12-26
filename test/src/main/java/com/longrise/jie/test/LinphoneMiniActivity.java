package com.longrise.jie.test;
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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Sylvain Berfini
 */
public class LinphoneMiniActivity extends Activity implements View.OnClickListener
{
    private LinphoneMiniManager mManager;

    private EditText txtNum;
    private Button btnCall;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aaa_main);

        txtNum = (EditText) findViewById(R.id.txt_num);
        btnCall = (Button) findViewById(R.id.btn_call);

        btnCall.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {

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
        mManager.destroy();

        super.onDestroy();
    }
}
