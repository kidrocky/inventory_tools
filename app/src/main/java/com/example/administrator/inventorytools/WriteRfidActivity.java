package com.example.administrator.inventorytools;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;


public class WriteRfidActivity extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_rfid);

        // 从intent中获取联机状态
        Intent intent = getIntent();
        int connect_stat = intent.getIntExtra("connect_stat", 0);
        if ( connect_stat == 0 )
        {
            this.setTitle("脱机写标签");
        }
        else
        {
            this.setTitle("联机写标签");
        }
    }
}
