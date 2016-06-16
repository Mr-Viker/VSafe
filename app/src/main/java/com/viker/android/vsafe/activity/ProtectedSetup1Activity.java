package com.viker.android.vsafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.viker.android.vsafe.R;

/**
 * Created by Viker on 2016/5/27.
 * 手机防盗设置向导
 */
public class ProtectedSetup1Activity extends Activity implements View.OnClickListener {

    private static final String TAG = "ProtectedSetup1Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protectedsetup1);

        Button btnNext = (Button) findViewById(R.id.btn_protectedsetup1_next);
        btnNext.setOnClickListener(this);
    }

    /*点击事件的处理*/
    @Override
    public void onClick(View v) {
        Intent toSetup2 = new Intent(ProtectedSetup1Activity.this,
                ProtectedSetup2Activity.class);
        startActivity(toSetup2);
        finish();
        //活动切换时播放的动画效果
        overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
    }

}
















