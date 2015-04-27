package com.example.administrator.inventorytools;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class inventory extends ActionBarActivity
{
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Spinner spinner_storehouses = (Spinner) findViewById(R.id.spinner_storehouses);
        adapter = ArrayAdapter.createFromResource(this, R.array.storehouses, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //将adapter2 添加到spinner中
        spinner_storehouses.setAdapter(adapter);

        //添加事件Spinner事件监听
        spinner_storehouses.setOnItemSelectedListener(new SpinnerXMLSelectedListener());

        //设置默认值
        spinner_storehouses.setVisibility(View.VISIBLE);
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
}
