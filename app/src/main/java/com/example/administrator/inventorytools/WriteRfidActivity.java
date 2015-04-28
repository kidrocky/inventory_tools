package com.example.administrator.inventorytools;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class WriteRfidActivity extends ActionBarActivity
{
    private int connect_stat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_rfid);

        // 从intent中获取联机状态
        Intent intent = getIntent();
        connect_stat = intent.getIntExtra("connect_stat", 0);
        if ( connect_stat == 0 )
        {
            this.setTitle("脱机盘点");
        }
        else
        {
            this.setTitle("联机盘点");
        }
    }
}
