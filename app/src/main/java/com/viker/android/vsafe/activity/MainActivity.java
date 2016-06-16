package com.viker.android.vsafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.MainIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/5/26.
 * 程序主界面
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private List<MainIcon> mainIconList;

    private GridView gvContainer; //包含九个功能模块控件

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar(); //初始化工具栏

        gvContainer = (GridView) findViewById(R.id.gv_main_Container);
        mainIconList = getIconList();
        MainAdapter adapter = new MainAdapter(mainIconList);
        gvContainer.setAdapter(adapter);
        gvContainer.setOnItemClickListener(this);
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(toolbar);
    }

    /*实例化图标列表并添加9个功能模块的图标和名字*/
    private List<MainIcon> getIconList() {
        mainIconList = new ArrayList<>();
        MainIcon mainIcon1 = new MainIcon(R.drawable.widget01, "手机防盗");
        MainIcon mainIcon2 = new MainIcon(R.drawable.widget02, "通信卫士");
        MainIcon mainIcon3 = new MainIcon(R.drawable.widget03, "软件管理");
        MainIcon mainIcon4 = new MainIcon(R.drawable.widget04, "进程管理");
        MainIcon mainIcon5 = new MainIcon(R.drawable.widget05, "流量统计");
        MainIcon mainIcon6 = new MainIcon(R.drawable.widget06, "手机杀毒");
        MainIcon mainIcon7 = new MainIcon(R.drawable.widget07, "系统优化");
        MainIcon mainIcon8 = new MainIcon(R.drawable.widget08, "高级工具");
        MainIcon mainIcon9 = new MainIcon(R.drawable.widget09, "设置中心");
        mainIconList.add(mainIcon1);
        mainIconList.add(mainIcon2);
        mainIconList.add(mainIcon3);
        mainIconList.add(mainIcon4);
        mainIconList.add(mainIcon5);
        mainIconList.add(mainIcon6);
        mainIconList.add(mainIcon7);
        mainIconList.add(mainIcon8);
        mainIconList.add(mainIcon9);
        return mainIconList;
    }


    /*自定义图标适配器，显示图标和名字*/
    private class MainAdapter extends ArrayAdapter<MainIcon> {

        public MainAdapter(List<MainIcon> mainIconList) {
            super(MainActivity.this, 0, mainIconList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.gridview_main_item, null);
            }
            MainIcon mainIcon = getItem(position);
            ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_main_item_icon);
            ivIcon.setImageResource(mainIcon.getIcon());
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_main_item_name);
            tvName.setText(mainIcon.getName());
            return convertView;
        }
    }

    //响应GridView中子项的点击事件操作
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            //手机防盗项
            case 0:
                Intent toToLostProtected = new Intent(MainActivity.this,
                        ToLostProtectedActivity.class);
                startActivity(toToLostProtected);
                break;

            //通信卫士
            case 1:
                Intent toCallSmsSafe = new Intent(MainActivity.this,
                        CallSmsSafeActivity.class);
                startActivity(toCallSmsSafe);
                break;

            //软件管理
            case 2:
                Intent toAppManager = new Intent(MainActivity.this,
                        AppManagerActivity.class);
                startActivity(toAppManager);
                break;

            //进程管理
            case 3:
                Intent toTaskManager = new Intent(MainActivity.this,
                        TaskManagerActivity.class);
                startActivity(toTaskManager);
                break;
            //流量统计
            case 4:
                Intent toTrafficInfo = new Intent(MainActivity.this,
                        TrafficInfoActivity.class);
                startActivity(toTrafficInfo);
                break;
            //手机杀毒
            case 5:
                Intent toAntiVirus = new Intent(MainActivity.this,
                        AntiVirusActivity.class);
                startActivity(toAntiVirus);
                break;
            //设置中心项
            case 8:
                Intent toSettingCenter = new Intent(MainActivity.this,
                        SettingCenterActivity.class);
                startActivity(toSettingCenter);
                break;

            default:
                break;
        }
    }
}










