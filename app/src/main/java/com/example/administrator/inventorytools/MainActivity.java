package com.example.administrator.inventorytools;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.TextView;
import java.lang.reflect.Field;


public class MainActivity extends ActionBarActivity
{
    private ScreenStateReceiver screenReceiver; // 屏幕状态监控广播接收器
    private int connect_stat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 设置始终显示菜单栏
        setOverflowShowingAlways();

        // 设置主页面
        setContentView(R.layout.activity_main);

        // 初始化界面元素
        InitView();

        //添加广播，默认屏灭时休眠，屏亮时唤醒
        screenReceiver = new ScreenStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);

        //初始化声音池
        Util.initSoundPool(this);

        // 测试是否联机
        TextView tv_connect_stat = (TextView) findViewById(R.id.tv_connect_stat);
        connect_stat = ifOnline();
        if ( connect_stat == 0 )
        {
            tv_connect_stat.setText("脱机");
            tv_connect_stat.setTextColor(Color.rgb(255, 0, 0));
        }
        else
        {
            tv_connect_stat.setText("联机");
            tv_connect_stat.setTextColor(Color.rgb(255, 255, 255));
        }
    }

    private int ifOnline()
    {
        // todo 测试是否联机
        return 1;
    }

    private void InitView()
    {
        // 界面元素映射
        Button btn_search_item = (Button) findViewById(R.id.btn_search_item);
        btn_search_item.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // todo: 增加处理
            }
        });

        Button btn_inventory = (Button) findViewById(R.id.btn_inventory);
        btn_inventory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 打开盘库页面
                Intent intent = new Intent(getApplicationContext(), inventory.class);
                intent.putExtra("connect_stat", connect_stat);
                startActivity(intent);
            }
        });

        Button btn_write_rfid = (Button) findViewById(R.id.btn_write_rfid);
        btn_write_rfid.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 打开盘库页面
                Intent intent = new Intent(getApplicationContext(), WriteRfidActivity.class);
                intent.putExtra("connect_stat", connect_stat);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        // 注销广播接收器
        unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            // 打开设置页面
            Intent intent = new Intent(this, SettingPower.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 在actionbar上显示菜单按钮
     */
    private void setOverflowShowingAlways()
    {
        try
        {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
