package com.example.administrator.inventorytools;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.administrator.inventorytools.Util.DoHttpGet;

public class inventory extends ActionBarActivity
{
    private ArrayAdapter adapter;
    private UhfReadTask uhf_read_task;  // 异步读取rfid标签任务
    private ListView listViewData;
    private UhfReader reader; //超高频读写器
    private ArrayList<Map<String, Object>> storehouse_map;
    private ArrayList<Map<String, Object>> storehouse_item_map;
    private SimpleAdapter lv_adapter;
    private int connect_stat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

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

        // 初始化变量
        storehouse_map = new ArrayList<>();
        ArrayList<String> storehouse_name_list = new ArrayList<>();
        storehouse_item_map = new ArrayList<>();

        InitView();

        // 获取reader句柄
        reader = UhfReader.getInstance();
        if (reader == null)
        {
            Toast.makeText(getApplicationContext(), "打开RFID读写器失败!", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            //获取用户设置功率,并设置
            SharedPreferences shared = getSharedPreferences("power", 0);
            int value = shared.getInt("value", 26);
            Log.e("", "value" + value);
            reader.setOutputPower(value);
        }

        // todo: spinner内容需要动态获取，暂时从xml里写死
        /*
        try
        {
            storehouse_list = this.GetStoreHouseList();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        */
        // for test
        JSONArray jsonArray;
        try
        {
            // for test
            jsonArray = new JSONArray("[{\"id\": 1, \"storename\": \"物证库房\"}, {\"id\": 2, \"storename\": \"武器库房\"}, {\"id\": 3, \"storename\": \"耗材库房\"}, {\"id\": 4, \"storename\": \"还有啥子库房\"}]");
            for (int i = 0; i < jsonArray.length(); i++)
            {
                try
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    System.out.println("111" + jsonObject);

                    Map<String, Object> map;
                    map = new HashMap<>();
                    map.put("ID", jsonObject.getInt("id"));
                    map.put("NAME", jsonObject.getString("storename"));
                    storehouse_map.add(map);
                    storehouse_name_list.add(jsonObject.getString("storename"));
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            System.out.println("222" + storehouse_name_list);
            System.out.println("333" + storehouse_map);

            // 下拉列表内容
            Spinner spinner_storehouses = (Spinner) findViewById(R.id.spinner_storehouses);
            //将可选内容与ArrayAdapter连接起来
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, storehouse_name_list);

            //设置下拉列表的风格
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            //将adapter 添加到spinner中
            spinner_storehouses.setAdapter(adapter);

            //添加事件Spinner事件监听
            spinner_storehouses.setOnItemSelectedListener(new SpinnerXMLSelectedListener());

            //设置默认值
            spinner_storehouses.setVisibility(View.VISIBLE);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    protected String GetStoreHouseList()
    {
        String url = "http://rocknio.gnway.cc:8000/inventory_api/get_storehouse_list/";
        return DoHttpGet(url);
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

    //读写/data/data/<应用程序名>目录上的文件:
    //写数据
    public void writeFile(String fileName, String writestr) throws IOException
    {
        try
        {
            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = writestr.getBytes();
            fout.write(bytes);
            fout.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //读数据
    public String readFile(String fileName) throws IOException
    {
        String res = "";
        try
        {
            FileInputStream fin = openFileInput(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            int read_result = fin.read(buffer);
            if ( read_result > 0 )
            {
                res = EncodingUtils.getString(buffer, "UTF-8");
            }
            fin.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return res;
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
                if (reader != null)
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

        Button btn_inventory_save = (Button) findViewById(R.id.btn_inventory_save);
        btn_inventory_save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ( connect_stat == 0 )
                {
                    // todo 存本地操作记录文件
                    System.out.println(connect_stat);

                }
                else
                {
                    // todo 同步数据
                    System.out.println(connect_stat);
                }
            }
        });
    }

    //使用XML形式操作
    class SpinnerXMLSelectedListener implements AdapterView.OnItemSelectedListener
    {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {
            System.out.println(position);

            String text = "你选择库房：" + adapter.getItem(position);
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

            //获取被点击的item所对应的数据
            Map<String, Object> map = storehouse_map.get(position);
            System.out.println("444" + map);

            /*
            String url = "http://rocknio.gnway.cc:8000/inventory_api/get_items_by_storehouse/" + map.get("id").toString();
            try
            {
                url = URLEncoder.encode(url, "UTF-8");
                String resp = Util.DoHttpGet(url);
                JSONObject jsonObject = new JSONObject(resp);
                // todo 更新listview
            }
            catch (UnsupportedEncodingException | JSONException e)
            {
                e.printStackTrace();
            }
            */

            // for test
            JSONArray jsonArray;
            storehouse_item_map.clear();
            try
            {
                jsonArray = new JSONArray("[{\"itemno\": \"0001\", \"status\": 0, \"storehouseid\": 1, \"itemname\": \"屠龙刀\", \"id\": 1, \"epc\": \"0000001\"}, {\"itemno\": \"0002\", \"status\": 0, \"storehouseid\": 1, \"itemname\": \"倚天剑\", \"id\": 2, \"epc\": \"0000002\"}, {\"itemno\": \"0003\", \"status\": 0, \"storehouseid\": 1, \"itemname\": \"独孤九剑\", \"id\": 3, \"epc\": \"0000003\"}, {\"itemno\": \"0004\", \"status\": 0, \"storehouseid\": 1, \"itemname\": \"辟邪剑谱\", \"id\": 4, \"epc\": \"0000004\"}]");
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    try
                    {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        System.out.println("111" + jsonObject);

                        Map<String, Object> detail_map;
                        detail_map = new HashMap<>();
                        detail_map.put("itemno", jsonObject.getString("itemno"));
                        detail_map.put("status", jsonObject.getInt("status"));
                        detail_map.put("storehouseid", jsonObject.getInt("storehouseid"));
                        detail_map.put("itemname", jsonObject.getString("itemname"));
                        detail_map.put("id", jsonObject.getInt("id"));
                        detail_map.put("store_desc", "不在库");
                        detail_map.put("epc", jsonObject.getInt("epc"));
                        System.out.println("555" + detail_map);
                        storehouse_item_map.add(detail_map);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }

                System.out.println("666" + storehouse_item_map);
                // 绑定数据到listview
                lv_adapter = new SimpleAdapter(getApplicationContext(),
                        storehouse_item_map, R.layout.lv_item,
                        new String[]{"itemname", "epc", "store_desc"},
                        new int[]{R.id.tv_id, R.id.tv_epc, R.id.tv_count});
                listViewData.setAdapter(lv_adapter);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        public void onNothingSelected(AdapterView<?> arg0)
        {
        }
    }

    // async task 读取标签数据task
    private class UhfReadTask extends AsyncTask<String, String, String>
    {
        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        //onProgressUpdate方法用于更新进度信息
        @Override
        protected void onProgressUpdate(String... values)
        {
            Log.i("UhfReadTask", "onProgressUpdate() called");

            // 刷新listview
            lv_adapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled()
        {
            Log.i("UhfReadTask", "onCancelled() called");
            super.onCancelled();
        }

        @Override
        protected String doInBackground(String... params)
        {
            Log.i("UhfReadTask", "doInBackground() called");
            // 开始实时扫描标签
            List<byte[]> epcList;
            while(!this.isCancelled())
            {
                epcList = reader.inventoryRealTime();
                if (epcList != null && !epcList.isEmpty())
                {
                    //播放提示音
                    Util.play(1, 0);
                    for (byte[] epc : epcList)
                    {
                        String epcStr = Tools.Bytes2HexString(epc, epc.length);
                        for (Map<String, Object> one_item: storehouse_item_map)
                        {
                            if ( epcStr == one_item.get("epc") )
                            {
                                one_item.put("store_desc", "在库");
                            }
                        }
                    }
                }
                if ( epcList != null )
                {
                    epcList.clear();
                }

                // 更新ui
                publishProgress("");

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
            return "";
        }
    }
}
