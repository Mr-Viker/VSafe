package com.viker.android.vsafe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.util.MD5Encoder;

/**
 * Created by Viker on 2016/5/28.
 */
public class ToLostProtectedActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ToLostProtectedActivity";

    //偏好设置，这里主要是用于处理密码，
    private SharedPreferences sharedPreferences;

    //首次进入手机防盗界面时显示的控件
    private EditText etFirstPwd;
    private EditText etFirstPwdConfirm;
    private Button btnFirstOk;
    private Button btnFirstCancel;
    //非首次进入手机防盗界面时显示的控件
    private EditText etNormalPwd;
    private Button btnNormalOk;
    private Button btnNormalCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);

        if (isSetupPwd()) {
            showNormalEntryDialog();
        } else {
            showFirstEntryDialog();
        }
    }

    /*判断用户是否设置过密码*/
    private boolean isSetupPwd() {
        String savedPwd = sharedPreferences.getString("password", "");
        if (TextUtils.isEmpty(savedPwd)) {
            return false;
        } else {
            return true;
        }
    }

    /*首次进入手机防盗时弹出的对话框*/
    private void showFirstEntryDialog() {
        setContentView(R.layout.actvity_tolostprotected_first_entry);

        etFirstPwd = (EditText) findViewById(R.id.et_first_password);
        etFirstPwdConfirm = (EditText) findViewById(R.id.et_first_confirm);
        btnFirstOk = (Button) findViewById(R.id.btn_first_ok);
        btnFirstCancel = (Button) findViewById(R.id.btn_first_cancel);
        btnFirstOk.setOnClickListener(this);
        btnFirstCancel.setOnClickListener(this);

    }

    /*非首次进入手机防盗页面时弹出的对话框*/
    private void showNormalEntryDialog() {
        setContentView(R.layout.activity_tolostprotected_normal_entry);
        etNormalPwd = (EditText) findViewById(R.id.et_normal_password);
        btnNormalOk = (Button) findViewById(R.id.btn_normal_ok);
        btnNormalCancel = (Button) findViewById(R.id.btn_normal_cancel);
        btnNormalOk.setOnClickListener(this);
        btnNormalCancel.setOnClickListener(this);
    }


    /*响应对话框中的点击事件操作*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //首次进入时[确定]按键响应事件
            case R.id.btn_first_ok:
                //获取两次输入的密码，如果不为空且相等则设置成功
                String pwd = etFirstPwd.getText().toString();
                String pwdConfirm = etFirstPwdConfirm.getText().toString();
                if (TextUtils.isEmpty(pwd) || TextUtils.isEmpty(pwdConfirm)) {
                    Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                //如果相等则将该密码利用MD5加密后存储在config文件中，key="password"
                if (pwd.equals(pwdConfirm)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    //利用MD5加密并存入config文件中
                    editor.putString("password", MD5Encoder.encode(pwd));
                    editor.apply();
                    //前往设置向导页面
                    loadProtectedSetup1UI();
                } else {
                    Toast.makeText(this, "两次密码不相同",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                break;

            //首次进入时[取消]按键响应事件
            case R.id.btn_first_cancel:
                finish();
                break;

            //非首次进入时[确定]按键响应事件
            case R.id.btn_normal_ok:
                String userEntryPwd = etNormalPwd.getText().toString();
                if (TextUtils.isEmpty(userEntryPwd)) {
                    Toast.makeText(this, "密码不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String savedPwd = sharedPreferences.getString("password", "");
                //因为在config文件中保存的密码是经过MD5加密的，所以比较的时候需要将用户输入的密码
                //也进行加密
                if (savedPwd.equals(MD5Encoder.encode(userEntryPwd))) {
                    Toast.makeText(this, "密码正确", Toast.LENGTH_SHORT).show();
                    //根据config文件中is_setup[即是否设置过向导]的值来判断是需要进入设置向导页面
                    //还是直接进入手机防盗主界面
                    boolean isSetup = sharedPreferences.getBoolean("is_setup", false);
                    if (isSetup) { //直接进入手机防盗主界面
                        loadLostProtectedUI();
                    } else { //前往设置向导页面
                        loadProtectedSetup1UI();
                    }
                    return;
                } else {
                    Toast.makeText(this, "密码不正确", Toast.LENGTH_SHORT).show();
                }
                break;

            //非首次进入时弹出的对话框中[取消]按键响应事件
            case R.id.btn_normal_cancel:
                finish();
                break;

            default:
                break;
        }
    }

    /*前往设置向导页面*/
    private void loadProtectedSetup1UI() {
        Intent toSetup1 = new Intent(ToLostProtectedActivity.this,
                ProtectedSetup1Activity.class);
        startActivity(toSetup1);
        finish();
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

    /*前往手机防盗主界面*/
    private void loadLostProtectedUI() {
        Intent toLostProtected = new Intent(ToLostProtectedActivity.this,
                LostProtectedActivity.class);
        startActivity(toLostProtected);
        finish();
        overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
    }

}









