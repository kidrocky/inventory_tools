package com.example.administrator.inventorytools;

import com.android.hdhe.uhf.reader.UhfReader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenStateReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        UhfReader reader = UhfReader.getInstance();
        //屏亮
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            if ( reader != null )
            {
                reader.powerOn();
            }
            Log.i("ScreenStateReceiver", "screen on");
        } //屏灭
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            if ( reader != null )
            {
                reader.powerOff();
            }
            Log.i("ScreenStateReceiver", "screen off");
        }
    }
}
