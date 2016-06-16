package com.viker.android.vsafe.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.util.MD5Encoder;

/**
 * Created by Viker on 2016/5/26.
 */
public class LostProtectedActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LostProtectedActivity";

    //偏好设置存储对象，在本活动中主要是读取config文件中的一些值，如防盗密码，是否已设置过向导，
    // 安全号码，是否开启了防盗保护等
    private SharedPreferences sharedPreferences;

    //手机防盗界面中的控件
    private TextView tvSafeNumber;
    private TextView tvProtectStatus;
    private TextView tvChangePwd;
    private TextView tvReset;

    //对话框对象
    private AlertDialog dialog;
    //修改密码界面显示的控件
    private EditText etOldPwd;
    private EditText etNewPwd;
    private EditText etNewPwdConfirm;
    private Button btnChangeOk;
    private Button btnChangeCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lostprotected);
        initToolbar(); //初始化工具栏

        //获取应用中的config文件，无则自动创建该文件
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);

        tvSafeNumber = (TextView) findViewById(R.id.tv_lostprotected_safenumber);
        tvProtectStatus = (TextView) findViewById(R.id.tv_lostprotected_protectstatus);
        tvChangePwd = (TextView) findViewById(R.id.tv_lostprotected_changepwd);
        tvReset = (TextView) findViewById(R.id.tv_lostprotected_reset);
        //从config读取安全号码、是否开启防盗保护等数据信息,并将之显示
        String safeNumber = sharedPreferences.getString("safe_number", "");
        tvSafeNumber.setText(safeNumber);
        boolean protectStatus = sharedPreferences.getBoolean("protect_status", false);
        if (protectStatus) {
            tvProtectStatus.setText("已开启");
            tvProtectStatus.setTextColor(getResources().getColor(R.color.BLACK));
        } else {
            tvProtectStatus.setText("未开启");
            tvProtectStatus.setTextColor(getResources().getColor(R.color.RED));
        }

        tvChangePwd.setOnClickListener(this);
        tvReset.setOnClickListener(this);

    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_lostprotected);
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
                Toast.makeText(LostProtectedActivity.this, "you click setting",
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*点击事件的处理*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //[重新进入设置向导]按键
            case R.id.tv_lostprotected_reset:
                loadProtectedSetup1UI();
                break;

            //[修改密码]按键
            case R.id.tv_lostprotected_changepwd:
                //获取对话框构造器
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View view = View.inflate(this,
                        R.layout.dialog_lostprotected_changepwd, null);
                etOldPwd = (EditText) view.findViewById(R.id.et_changepwd_oldpwd);
                etNewPwd = (EditText) view.findViewById(R.id.et_changepwd_newpwd);
                etNewPwdConfirm = (EditText) view.findViewById(
                        R.id.et_changepwd_newpwd_confirm);
                btnChangeOk = (Button) view.findViewById(R.id.btn_changepwd_ok);
                btnChangeCancel = (Button) view.findViewById(R.id.btn_changepwd_cancel);
                btnChangeCancel.setOnClickListener(this);
                btnChangeOk.setOnClickListener(this);
                builder.setView(view);
                dialog = builder.create();
                dialog.show();
                break;

            //修改密码中的[确定]按键
            case R.id.btn_changepwd_ok:
                String inputOldPwd = etOldPwd.getText().toString(); //输入的原密码
                String savedPwd = sharedPreferences.getString("password", ""); //保存的密码
                //当输入的原密码与config文件中保存的密码相同时再进行下一步判断
                if ((!TextUtils.isEmpty(inputOldPwd)) &&
                        savedPwd.equals(MD5Encoder.encode(inputOldPwd))) {
                    String newPwd = etNewPwd.getText().toString();
                    String newPwdConfirm = etNewPwdConfirm.getText().toString();
                    //对新密码是否为空进行判断
                    if (TextUtils.isEmpty(newPwd)) {
                        Toast.makeText(LostProtectedActivity.this, "密码不能为空",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //两次输入的密码都相等时表示设置成功
                    if (newPwd.equals(newPwdConfirm)) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("password", MD5Encoder.encode(newPwd));
                        editor.apply();
                        dialog.dismiss();
                        Toast.makeText(LostProtectedActivity.this, "密码修改成功 \n重新" +
                                "进入手机防盗主界面", Toast.LENGTH_SHORT).show();
                        Intent reentry = new Intent(LostProtectedActivity.this,
                                ToLostProtectedActivity.class);
                        startActivity(reentry);
                        finish();
                    } else {
                        Toast.makeText(LostProtectedActivity.this, "新密码两次" +
                                "输入不一致", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LostProtectedActivity.this, "原密码错误",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            //修改密码对话框中的[取消]按键
            case R.id.btn_changepwd_cancel:
                dialog.dismiss();
                break;

            default:
                break;
        }
    }

    /*进入防盗设置向导第一步的页面*/
    private void loadProtectedSetup1UI() {
        Intent toSetup1 = new Intent(LostProtectedActivity.this,
                ProtectedSetup1Activity.class);
        startActivity(toSetup1);
        finish();
        overridePendingTransition(R.anim.tran_in, R.anim.tran_out);
    }


}















