package com.example.administrator.inventorytools;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.hdhe.uhf.reader.Tools;
import com.android.hdhe.uhf.reader.UhfReader;

import java.util.ArrayList;
import java.util.List;


public class WriteRfidActivity extends ActionBarActivity
{
    private UhfReader reader; //超高频读写器

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_rfid);

        // 从intent中获取联机状态
        Intent intent = getIntent();
        int connect_stat = intent.getIntExtra("connect_stat", 0);
        if (connect_stat == 0)
        {
            this.setTitle("脱机写标签");
        }
        else
        {
            this.setTitle("联机写标签");
        }

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

        // 开始读标签
        if (reader != null)
        {
            List<byte[]> epc_list = reader.inventoryMulti();
            if (epc_list != null)
            {
                ArrayList<String> epc_string_list = new ArrayList<>();
                for (byte[] epc : epc_list)
                {
                    String epcStr = Tools.Bytes2HexString(epc, epc.length);
                    epc_string_list.add(epcStr);
                }

                // 下拉列表内容
                Spinner spinner_storehouses = (Spinner) findViewById(R.id.spinner_storehouses);
                //将可选内容与ArrayAdapter连接起来
                ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, epc_string_list);

                //设置下拉列表的风格
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                //将adapter 添加到spinner中
                spinner_storehouses.setAdapter(adapter);

                //添加事件Spinner事件监听
                spinner_storehouses.setOnItemSelectedListener(new SpinnerJsonSelectedListener());

                //设置默认值
                spinner_storehouses.setVisibility(View.VISIBLE);
            }
        }

        // 写标签按钮处理
        Button btn_write_rfid = (Button) findViewById(R.id.btn_write_rfid);
        btn_write_rfid.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                EditText ed_itemname = (EditText) findViewById(R.id.et_itemname);
                String str_itemname = ed_itemname.getText().toString();
                if (reader != null)
                {
                    byte[] epc = Tools.HexString2Bytes(str_itemname);
                    reader.selectEPC(epc);
                    // todo 写标签
                }
            }
        });
    }

    class SpinnerJsonSelectedListener implements AdapterView.OnItemSelectedListener
    {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        {

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent)
        {

        }
    }
}
