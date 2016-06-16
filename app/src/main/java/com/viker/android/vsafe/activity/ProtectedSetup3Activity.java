package com.viker.android.vsafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.viker.android.vsafe.R;

/**
 * Created by Viker on 2016/5/27.
 */
public class ProtectedSetup3Activity extends Activity implements View.OnClickListener {

    private static final String TAG = "ProtectedSetup3Activity";

    //以startActivityForResult方式启动SelectContactActivity所传入的请求码
    private static final int REQUEST_CODE = 0;

    //偏好设置，在本活动中主要是进行存储安全号码及安全号码的回显的操作
    private SharedPreferences sharedPreferences;

    //控件
    private EditText etSafeNumber; //显示安全号码的编辑框
    private Button btnSelectContact; //选择联系人按钮
    private Button btnPrevious;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protectedsetup3);

        etSafeNumber = (EditText) findViewById(R.id.et_protectedsetup3_safenumber);
        btnSelectContact = (Button) findViewById(R.id.btn_protectedsetup3_selectcontact);
        btnSelectContact.setOnClickListener(this);
        btnPrevious = (Button) findViewById(R.id.btn_protectedsetup3_previous);
        btnPrevious.setOnClickListener(this);
        btnNext = (Button) findViewById(R.id.btn_protectedsetup3_next);
        btnNext.setOnClickListener(this);
        //获取config文件中的安全号码值，有则显示，无则不操作，避免覆盖了hint
        sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String safeNumber = sharedPreferences.getString("safe_number", "");
        Log.d(TAG, "安全号码为" + safeNumber);
        if (!TextUtils.isEmpty(safeNumber)) {
            etSafeNumber.setText(safeNumber);
        }
    }

    /*点击事件的处理*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击选择联系人
            case R.id.btn_protectedsetup3_selectcontact:
//                Toast.makeText(ProtectedSetup3Activity.this, "暂不支持选择联系人功能，请手动输入" +
//                        "安全号码", Toast.LENGTH_SHORT).show();
                Intent toSelectContact = new Intent(ProtectedSetup3Activity.this,
                        SelectContactActivity.class);
                startActivityForResult(toSelectContact, REQUEST_CODE);
                break;
            //点击上一步
            case R.id.btn_protectedsetup3_previous:
                Intent toSetup2 = new Intent(ProtectedSetup3Activity.this,
                        ProtectedSetup2Activity.class);
                startActivity(toSetup2);
                finish();
                //活动切换动画效果
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                break;
            //点击下一步
            case R.id.btn_protectedsetup3_next:
                //获取输入框中的安全号码,如果不为空则将之写入config文件中
                String safeNumber = etSafeNumber.getText().toString();
                if (!TextUtils.isEmpty(safeNumber)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("safe_number", safeNumber);
                    editor.apply();
                }
                Intent toSetup4 = new Intent(ProtectedSetup3Activity.this,
                        ProtectedSetup4Activity.class);
                startActivity(toSetup4);
                finish();
                overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
                break;
            default:
                break;
        }
    }

    /*获取其他活动返回的数据*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //获取SelectContactActivity回传的数据
            case REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    String safeNumber = data.getStringExtra("safeNumber");
                    etSafeNumber.setText(safeNumber);
                }
                break;
            default:
                break;
        }
    }
}










