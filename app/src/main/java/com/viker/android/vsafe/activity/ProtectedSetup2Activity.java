package com.viker.android.vsafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.viker.android.vsafe.R;

/**
 * Created by Viker on 2016/5/27.
 * 手机防盗设置向导
 */
public class ProtectedSetup2Activity extends Activity implements View.OnClickListener {

    private static final String TAG = "ProtectedSetup2Activity";

    //偏好设置，在本活动中主要是用于存储和获取绑定SIM卡的值 key="sim_number"
    private SharedPreferences sharedPreferences;

    //该控件用于响应用户的点击事件，这样用户点击其中的任意子控件都可以响应到点击事件
    private RelativeLayout rlBind;
    //用于显示绑定状态的控件
    private ImageView ivBindStatus;
    private Button btnPrevious;
    private Button btnNext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protectedsetup2);

        rlBind = (RelativeLayout) findViewById(R.id.rl_protectedsetup2_bind);
        ivBindStatus = (ImageView) findViewById(R.id.iv_protectedsetup2_bindstatus);
        btnPrevious = (Button) findViewById(R.id.btn_protectedsetup2_previous);
        btnNext = (Button) findViewById(R.id.btn_protectedsetup2_next);
        rlBind.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        //从config文件中读取绑定SIM卡号码，空表示为未绑定
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String simNumber = sharedPreferences.getString("sim_number", "");
        if (TextUtils.isEmpty(simNumber)) {
            ivBindStatus.setImageResource(R.drawable.switch_off_normal);
        } else {
            ivBindStatus.setImageResource(R.drawable.switch_on_normal);
        }
    }

    /*点击事件的处理*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击绑定SIM卡控件
            case R.id.rl_protectedsetup2_bind:
                String simNumber = sharedPreferences.getString("sim_number", "");
                //如果原来是未绑定状态，点击之后则获取SIM卡号码并保存到config文件中，并显示已绑定
                if (TextUtils.isEmpty(simNumber)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sim_number", getSimNumber());
                    editor.apply();
                    ivBindStatus.setImageResource(R.drawable.switch_on_normal);
                } else {
                    //如果原来是已绑定状态，点击之后则将config文件中保存的绑定手机号码清空，
                    //并显示未绑定
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sim_number", "");
                    editor.apply();
                    ivBindStatus.setImageResource(R.drawable.switch_off_normal);
                }
                break;
            //点击上一步
            case R.id.btn_protectedsetup2_previous:
                Intent toSetup1 = new Intent(ProtectedSetup2Activity.this,
                        ProtectedSetup1Activity.class);
                startActivity(toSetup1);
                finish();
                break;
            //点击下一步
            case R.id.btn_protectedsetup2_next:
                Intent toSetup3 = new Intent(ProtectedSetup2Activity.this,
                        ProtectedSetup3Activity.class);
                startActivity(toSetup3);
                finish();
                break;

            default:
                break;
        }
    }

    /*获取SIM卡号码信息*/
    private String getSimNumber() {
        //需要获取电话管理器并在清单文件中配置权限
        TelephonyManager telePhonyManager = (TelephonyManager) getSystemService(
                TELEPHONY_SERVICE);
        return  telePhonyManager.getSimSerialNumber();
    }

}










