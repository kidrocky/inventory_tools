package com.example.administrator.inventorytools;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.Tools;
import com.android.hdhe.uhf.reader.UhfReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity
{
    private UhfReader reader; //超高频读写器
    private ScreenStateReceiver screenReceiver; // 屏幕状态监控广播接收器
    private UhfReadTask uhf_read_task;  // 异步读取rfid标签任务
    private ArrayList<EPC> listEPC;
    private ArrayList<Map<String, Object>> listMap;
    private ListView listViewData;
    private List<byte[]> epcList;
    private Button btn_search_item;
    private Button btn_inventory;

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

        // 获取reader句柄
        reader = UhfReader.getInstance();
        if (reader == null)
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

    private void InitView()
    {
        // 界面元素映射
        btn_search_item = (Button) findViewById(R.id.btn_search_item);
        btn_search_item.setOnClickListener(new ButtonClick());

        btn_inventory = (Button) findViewById(R.id.btn_inventory);
        btn_inventory.setOnClickListener(new ButtonClick());
    }

    private class ButtonClick implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.btn_inventory:
                    break;

                case R.id.btn_search_item:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        // 注销广播接收器
        unregisterReceiver(screenReceiver);

        // 如果异步任务还在处理，则cancel掉
        if ( !uhf_read_task.isCancelled() )
        {
            uhf_read_task.cancel(true);
        }

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

    // async task 读取标签数据task
    private class UhfReadTask extends AsyncTask
    {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute()
        {
            Log.i("UhfReadTask", "onPreExecute() called");
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(Object[] values)
        {
            Log.i("UhfReadTask", "onProgressUpdate() called");
            //将数据添加到ListView
            List<EPC> list = (List<EPC>)values[0];
            listMap = new ArrayList<Map<String, Object>>();
            int idcount = 1;
            for (EPC epcdata : list)
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("ID", idcount);
                map.put("EPC", epcdata.getEpc());
                map.put("COUNT", epcdata.getCount());
                idcount++;
                listMap.add(map);
            }
            // 绑定数据到listview
//                    listViewData.setAdapter(new SimpleAdapter(MainActivity.this,
//                            listMap, R.layout.listview_item,
//                            new String[]{"ID", "EPC", "COUNT"},
//                            new int[]{R.id.textView_id, R.id.textView_epc, R.id.textView_count}));
            // super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled()
        {
            Log.i("UhfReadTask", "onCancelled() called");
            super.onCancelled();
        }

        @Override
        protected Object doInBackground(Object[] params)
        {
            Log.i("UhfReadTask", "doInBackground() called");
            // 开始实时扫描标签

            while(!this.isCancelled())
            {
                epcList = reader.inventoryRealTime(); //实时盘存
                if (epcList != null && !epcList.isEmpty())
                {
                    //播放提示音
                    Util.play(1, 0);
                    for (byte[] epc : epcList)
                    {
                        String epcStr = Tools.Bytes2HexString(epc, epc.length);
                        addToList(listEPC, epcStr);
                    }
                }
                epcList = null;

                // 每一次扫描间隔一点时间
                try
                {
                    Thread.sleep(40);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            return null;
        }

        //将读取的EPC添加到LISTVIEW
        private void addToList(final List<EPC> list, final String epc)
        {
            //第一次读入数据
            if (list.isEmpty())
            {
                EPC epcTag = new EPC();
                epcTag.setEpc(epc);
                epcTag.setCount(1);
                list.add(epcTag);
            }
            else
            {
                for (int i = 0; i < list.size(); i++)
                {
                    EPC mEPC = list.get(i);
                    //list中有此EPC
                    if (epc.equals(mEPC.getEpc()))
                    {
                        mEPC.setCount(mEPC.getCount() + 1);
                        list.set(i, mEPC);
                        break;
                    }
                    else if (i == (list.size() - 1))
                    {
                        //list中没有此epc
                        EPC newEPC = new EPC();
                        newEPC.setEpc(epc);
                        newEPC.setCount(1);
                        list.add(newEPC);
                    }
                }

                // 更新ui显示
                this.publishProgress(list);
            }
        }
    }
}
