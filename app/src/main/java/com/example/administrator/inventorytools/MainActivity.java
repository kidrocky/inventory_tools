package com.example.administrator.inventorytools;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.UhfReader;

import java.lang.reflect.Field;


public class MainActivity extends ActionBarActivity
{
    private UhfReader reader; //超高频读写器
    private ScreenStateReceiver screenReceiver; // 屏幕状态监控广播接收器

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 设置始终显示菜单栏
        setOverflowShowingAlways();

        // 设置主页面
        setContentView(R.layout.activity_main);

        //添加广播，默认屏灭时休眠，屏亮时唤醒
        screenReceiver = new ScreenStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);

        //初始化声音池
        Util.initSoundPool(this);

        // 获取reader句柄
        reader = UhfReader.getInstance();
        if ( reader == null )
        {
            Toast.makeText(getApplicationContext(), "打开RFID读写器失败!", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取用户设置功率,并设置
        SharedPreferences shared = getSharedPreferences("power", 0);
        int value = shared.getInt("value", 26);
        Log.e("", "value" + value);
        reader.setOutputPower(value);
    }

    @Override
    protected void onDestroy()
    {
        // 注销广播接收器
        unregisterReceiver(screenReceiver);

        // 关闭reader句柄
        if (reader != null)
        {
            reader.close();
        }

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
