package com.example.administrator.inventorytools;

import com.android.hdhe.uhf.reader.UhfReader;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * 设置功率，调节距离
 *
 * @author Administrator
 */
public class SettingPower extends Activity implements OnClickListener
{
    private EditText editValues;
    private int value = 26; //初始值为最大，2600为26dbm(value范围16dbm~26dbm)
    private UhfReader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.setting_power);
        super.onCreate(savedInstanceState);
        initView();
        reader = UhfReader.getInstance();
        if (reader == null)
        {
            Toast.makeText(getApplicationContext(), "打开RFID读写器失败!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView()
    {
        Button buttonMin = (Button) findViewById(R.id.button_min);
        Button buttonPlus = (Button) findViewById(R.id.button_plus);
        Button buttonSet = (Button) findViewById(R.id.button_set);
        editValues = (EditText) findViewById(R.id.editText_power);

        buttonMin.setOnClickListener(this);
        buttonPlus.setOnClickListener(this);
        buttonSet.setOnClickListener(this);
        value = getSharedValue();
        editValues.setText("" + value);

        SeekBar sb_power = (SeekBar) findViewById(R.id.sb_power);
        sb_power.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                int cur_value = 16 + progress;
                editValues.setText(Integer.toString(cur_value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }

    //获取存储Value
    private int getSharedValue()
    {
        SharedPreferences shared = getSharedPreferences("power", 0);
        return shared.getInt("value", 26);
    }

    //保存Value
    private void saveSharedValue(int value)
    {
        SharedPreferences shared = getSharedPreferences("power", 0);
        Editor editor = shared.edit();
        editor.putInt("value", value);
        editor.apply();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_min://减
                if (value > 16)
                {
                    value = value - 1;
                }
                editValues.setText(value + "");
                break;

            case R.id.button_plus://加
                if (value < 26)
                {
                    value = value + 1;
                }
                editValues.setText(value + "");
                break;

            case R.id.button_set://设置
                if (reader != null)
                {
                    if (reader.setOutputPower(value))
                    {
                        saveSharedValue(value);
                        Toast.makeText(getApplicationContext(), "设置成功", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "设置失败", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "打开RFID读写器失败!", Toast.LENGTH_SHORT).show();
                }

                finish();
                break;

            default:
                break;
        }
    }
}
