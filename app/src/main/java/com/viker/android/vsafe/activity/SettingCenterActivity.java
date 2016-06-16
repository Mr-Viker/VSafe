package com.viker.android.vsafe.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;

/**
 * Created by Viker on 2016/5/26.
 */
public class SettingCenterActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SettingCenterActivity";

    //用于存储一些设置信息，如自动更新是否开启的boolean值等
    private SharedPreferences sharedPreferences;
    private TextView tvAutoUpdateStatus; //自动更新是否开启对应的显示文本控件
    private CheckBox cbAutoUpdate; //自动更新是否开启的勾选框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingcenter);
        initToolbar();

        tvAutoUpdateStatus = (TextView) findViewById(R.id.tv_setting_autoupdate_status);
        cbAutoUpdate = (CheckBox) findViewById(R.id.cb_setting_autoupdate);
        //获取应用文件中的config.xml文件，如果不存在则会自动创建，操作类型为私有类型
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        //获取config文件中"autoUpdate"对应的值，如果为真则表示开启了自动更新，默认开启
        boolean autoUpdate = sharedPreferences.getBoolean("auto_update", true);
        if (autoUpdate) {
            tvAutoUpdateStatus.setTextColor(
                    getResources().getColor(R.color.BLACK));
            tvAutoUpdateStatus.setText("自动更新已开启");
            cbAutoUpdate.setChecked(true);
        } else {
            tvAutoUpdateStatus.setTextColor(
                    getResources().getColor(R.color.RED));
            tvAutoUpdateStatus.setText("自动更新未开启");
            cbAutoUpdate.setChecked(false);
        }
        //对Checkbox控件进行监听
        cbAutoUpdate.setOnCheckedChangeListener(this);
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_settingcenter);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    /*创建工具栏中的选项菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_public, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*选项菜单点击事件的处理*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_setting:
                Toast.makeText(SettingCenterActivity.this, "you click setting",
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == cbAutoUpdate) {
            //将CheckBox改变之后的值写入config配置文件中
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("auto_update", isChecked);
            editor.apply();
            if (isChecked) {
                tvAutoUpdateStatus.setTextColor(
                        getResources().getColor(R.color.BLACK));
                tvAutoUpdateStatus.setText("自动更新已开启");
            } else {
                tvAutoUpdateStatus.setTextColor(
                        getResources().getColor(R.color.RED));
                tvAutoUpdateStatus.setText("自动更新未开启");
            }
        }
    }


}











