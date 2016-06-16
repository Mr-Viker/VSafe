package com.viker.android.vsafe.activity;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.receiver.MyAdmin;

/**
 * Created by Viker on 2016/5/27.
 */
public class ProtectedSetup4Activity extends Activity
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "ProtectedSetup4Activity";

    //偏好设置，在本活动中主要是用于存储防盗保护是否开启的值，还有设置向导是否完成的值
    private SharedPreferences sharedPreferences;

    private Button btnActivate; //激活管理权限按钮
    private CheckBox cbProtect;
    private Button btnPrevious;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protectedsetup4);

        btnActivate = (Button) findViewById(R.id.btn_protectedsetup4_activate);
        btnActivate.setOnClickListener(this);
        cbProtect = (CheckBox) findViewById(R.id.cb_protectedsetup4_protect);
        cbProtect.setOnCheckedChangeListener(this);
        btnPrevious = (Button) findViewById(R.id.btn_protectedsetup4_previous);
        btnPrevious.setOnClickListener(this);
        btnNext = (Button) findViewById(R.id.btn_protectedsetup4_next);
        btnNext.setOnClickListener(this);
        //获取存储在config文件中防盗保护是否开启的值，默认是否
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        boolean protectStatus = sharedPreferences.getBoolean("protect_status", false);
        cbProtect.setChecked(protectStatus);
        if (protectStatus) {
            cbProtect.setText("防盗保护已开启");
        } else {
            cbProtect.setText("防盗保护未开启");
        }
    }

    /*响应开启防盗保护勾选框的操作*/
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //将改变后的值存储在config文件中
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("protect_status", isChecked);
        editor.apply();
        //进行响应的改变
        if (isChecked) {
            cbProtect.setText("防盗保护已开启");
        } else {
            cbProtect.setText("防盗保护未开启");
        }
    }

    /*响应点击事件的操作*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击[获取管理权限]
            case R.id.btn_protectedsetup4_activate:
                //创建一个与MyAdmin相关联的组件
                ComponentName adminName = new ComponentName(this, MyAdmin.class);
                //获取手机设备管理器
                DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
                        getSystemService(DEVICE_POLICY_SERVICE);
                //判断组建是否已经获取超级管理员的权限
                if (!devicePolicyManager.isAdminActive(adminName)) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    //将组建的超级管理员权限激活
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "你已具有管理权限", Toast.LENGTH_SHORT).show();
                }
                break;

            //点击上一步
            case R.id.btn_protectedsetup4_previous:
                Intent toSetup3 = new Intent(ProtectedSetup4Activity.this,
                        ProtectedSetup3Activity.class);
                startActivity(toSetup3);
                finish();
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                break;

            //点击设置完成
            case R.id.btn_protectedsetup4_next:
                /*//如果未开启防盗保护，则弹出一个对话框提示开启
                if (!cbProtect.isChecked()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("温馨提示");
                    builder.setMessage("手机防盗极大地保护了你的手机安全，强烈建议开启");
                    builder.setPositiveButton("开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cbProtect.setChecked(true);
                        }
                    });
                    builder.setNegativeButton("不开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cbProtect.setChecked(false);
                        }
                    });
                    builder.create().show();
                }*/
                //不管最后有没开启防盗保护，当完成设置向导后，都要将“完成设置向导”的值保存在config文件中
                //下次进入手机防盗将根据该值来判断是否需要进入设置向导页面。
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("is_setup", true);
                editor.apply();
                Intent toLostProtected = new Intent(ProtectedSetup4Activity.this,
                        LostProtectedActivity.class);
                startActivity(toLostProtected);
                finish();
                overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
                break;

            default:
                break;
        }
    }
}























