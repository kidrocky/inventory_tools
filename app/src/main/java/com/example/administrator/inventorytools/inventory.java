package com.example.administrator.inventorytools;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.Tools;
import com.android.hdhe.uhf.reader.UhfReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class inventory extends ActionBarActivity
{
    private ArrayAdapter adapter;
    private UhfReadTask uhf_read_task;  // 异步读取rfid标签任务
    private ArrayList<EPC> listEPC;
    private ArrayList<Map<String, Object>> listMap;
    private ListView listViewData;
    private List<byte[]> epcList;
    private UhfReader reader; //超高频读写器

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        InitView();

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

        // todo: spinner内容需要动态获取，暂时从xml里写死
        Spinner spinner_storehouses = (Spinner) findViewById(R.id.spinner_storehouses);
        adapter = ArrayAdapter.createFromResource(this, R.array.storehouses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        //将adapter2 添加到spinner中
        spinner_storehouses.setAdapter(adapter);

        //添加事件Spinner事件监听
        spinner_storehouses.setOnItemSelectedListener(new SpinnerXMLSelectedListener());

        //设置默认值
        spinner_storehouses.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy()
    {
        // 如果异步任务还在处理，则cancel掉
        if ( uhf_read_task != null && !uhf_read_task.isCancelled() )
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

    private void InitView()
    {
        listViewData = (ListView) findViewById(R.id.lv_inventory);

        // 界面元素映射
        Button btn_inventory = (Button) findViewById(R.id.btn_inventory);
        btn_inventory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ( reader != null )
                {
                    uhf_read_task = new UhfReadTask();
                    uhf_read_task.execute("");
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "打开RFID读写器失败!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btn_back = (Button) findViewById(R.id.btn_inventory_back);
        btn_back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }

    //使用XML形式操作
    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener
    {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
        {
            String text = "你选择库房：" + adapter.getItem(arg2);
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView<?> arg0)
        {
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
            listMap = new ArrayList<>();
            int idcount = 1;
            for (EPC epcdata : list)
            {
                Map<String, Object> map;
                map = new HashMap<>();
                map.put("ID", idcount);
                map.put("EPC", epcdata.getEpc());
                map.put("COUNT", epcdata.getCount());
                idcount++;
                listMap.add(map);
            }
            // 绑定数据到listview
            listViewData.setAdapter(new SimpleAdapter(getApplicationContext(),
                                                        listMap, R.layout.lv_item,
                                                        new String[]{"ID", "EPC", "COUNT"},
                                                        new int[]{R.id.tv_id, R.id.tv_epc, R.id.tv_count}));
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
                publishProgress(list);
            }
        }
    }
}
