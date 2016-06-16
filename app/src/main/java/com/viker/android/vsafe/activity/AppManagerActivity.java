package com.viker.android.vsafe.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.AppInfo;
import com.viker.android.vsafe.provider.AppInfoProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/6/3.
 */
public class AppManagerActivity extends AppCompatActivity {

    private static final String TAG = "AppManagerActivity";
    //启动系统自带的卸载程序时传入的请求码
    private static final int REQUEST_CODE = 0;

    private PackageManager packageManager; //包管理器

    private List<AppInfo> appInfoList; //包含所有应用程序信息的列表
    private List<AppInfo> userAppInfoList; //包含所有用户程序信息的列表
    private List<AppInfo> systemAppInfoList; //包含所有系统程序信息的列表

    //控件
    private TextView tvMemory; //显示手机可用ROM
    private TextView tvSd; //显示SD卡可用内存
    private RelativeLayout rlLoading; //ProgressBar的父控件
    private ListView lvApp; //用于显示应用程序列表
    private TextView tvAllApp; //用于显示“全部程序”
    private TextView tvUserApp; //用于显示“用户程序”
    private TextView tvSystemApp; //用于显示“系统程序”

    //自定义的适配器
    private AppAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appmanager);
        initToolbar(); //初始化工具栏
        findViews(); //引用控件
        //初始化各列表
        appInfoList = new ArrayList<>();
        userAppInfoList = new ArrayList<>();
        systemAppInfoList = new ArrayList<>();

        tvMemory.setText(getAvailROMSize());
        tvSd.setText(getAvailSDSize());

        packageManager = getPackageManager();
        //开启新线程加载App信息[因为这是比较耗时的操作]
        new LoadTask().execute();
        //注册上下文菜单
        registerForContextMenu(lvApp);

    }


    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_appmanager);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /*创建选项菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_public, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*选项菜单点击事件的处理*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //导航
            case android.R.id.home:
                finish();
                break;
            //设置
            case R.id.setting:
                Toast.makeText(this, "you click setting in AppManager",
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*引用各控件*/
    private void findViews() {
        tvMemory = (TextView) findViewById(R.id.tv_appmanager_memory);
        tvSd = (TextView) findViewById(R.id.tv_appmanager_sd);
        rlLoading = (RelativeLayout) findViewById(R.id.rl_appmanager_loading);
        lvApp = (ListView) findViewById(R.id.lv_appmanager_app);
        tvAllApp = (TextView) findViewById(R.id.tv_appmanager_all);
        tvUserApp = (TextView) findViewById(R.id.tv_appmanager_user);
        tvSystemApp = (TextView) findViewById(R.id.tv_appmanager_system);
    }

    /*创建上下文菜单*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_appmanager_context, menu);
    }

    /*上下文菜单点击事件的处理*/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //获取到菜单Item对应的对象
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
                item.getMenuInfo();
        //通过该对象获取其对应的是ListView中的哪一项
        int position = (int) info.id;
        AppInfo selectedAppInfo = adapter.getItem(position);
        switch (item.getItemId()) {
            //[卸载]菜单
            case R.id.menu_uninstall_app:
                //判断是否是用户程序，是则卸载，否则提示系统程序不能卸载
                if (selectedAppInfo.isUserApp()) {
                    uninstallApp(selectedAppInfo);
                } else {
                    Toast.makeText(this, "禁止卸载系统程序", Toast.LENGTH_SHORT).show();
                }
                return true;
            //[启动]菜单
            case R.id.menu_start_app:
                startApp(selectedAppInfo);
                return true;
            //[分享]菜单
            case R.id.menu_share_app:
                shareApp(selectedAppInfo);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /*[卸载]所选中的程序*/
    private void uninstallApp(AppInfo selectedAppInfo) {
        String selectedPackageName = selectedAppInfo.getPackageName();
        Uri packageUri = Uri.parse("package:" + selectedPackageName);
        Intent toUninstall = new Intent(Intent.ACTION_DELETE, packageUri);
        startActivityForResult(toUninstall, REQUEST_CODE);
        Log.d(TAG, "go to packageInstaller");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult execute");
        switch (requestCode) {
            //[卸载程序]所返回的结果
            case REQUEST_CODE:
                if (resultCode == 0) {
                    Log.d(TAG, "resultCode:" + resultCode);
                    tvMemory.setText(getAvailROMSize());
                    tvSd.setText(getAvailSDSize());
                    new LoadTask().execute();
                    Log.d(TAG, "LoadTask execute");
                }
                break;
            default:
                break;
        }
    }

    /*[启动]选中的程序*/
    private void startApp(AppInfo selectedAppInfo) {
        String selectedPackageName = selectedAppInfo.getPackageName();
        PackageInfo packageInfo;
        try {
            //解析选中程序的清单文件, PackageManager.GET_ACTIVITIES表示只解析其中的活动节点
            packageInfo = packageManager.getPackageInfo(selectedPackageName,
                    PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activityInfos = packageInfo.activities;
            if (activityInfos.length > 0) {
                //获取清单文件中的第一个活动节点名，即启动Activity
                String className = activityInfos[0].name;
                Intent intent = new Intent();
                intent.setClassName(selectedPackageName, className);
                startActivity(intent);
            } else {
                Toast.makeText(this, "启动该应用失败", Toast.LENGTH_SHORT).show();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "启动该应用失败", Toast.LENGTH_SHORT).show();
        }
    }

    /*[分享]选中的程序*/
    private void shareApp(AppInfo selectedAppInfo) {
        //通过意图的动作和类型来激活手机中具有相应分享功能的应用程序，会以列表形式显示出来供用户选择
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        //设置标题
        intent.putExtra("subject", "分享的标题");
        //设置分享的默认内容
        intent.putExtra("sms_body", "推荐你使用一款软件" + selectedAppInfo.getAppName());
        intent.putExtra(Intent.EXTRA_TEXT, "extra_text");
        startActivity(intent);
    }


    /*获取手机剩余可用ROM*/
    private String getAvailROMSize() {
        //获取手机内存根目录所在的文件对象
        File path = Environment.getDataDirectory();
        //获取状态空间对象
        StatFs stat = new StatFs(path.getPath());
        //获取可用分区数量
        long availableBlocks = stat.getAvailableBlocks();
        //获取每块分区可以存放的byte数量
        long blockSize = stat.getBlockSize();
        //计算总的byte
        long availROMSize = availableBlocks * blockSize;
        return Formatter.formatFileSize(this, availROMSize);
    }

    /*获取SD卡剩余可用内存*/
    private String getAvailSDSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long availableBlocks = stat.getAvailableBlocks();
        long blockSize = stat.getBlockSize();
        long availSDSize = availableBlocks * blockSize;
        return Formatter.formatFileSize(this, availSDSize);
    }


    /**
     * 自定义LoadTask类继承自AsyncTask类，加载所有App数据
     */
    private class LoadTask extends AsyncTask<Void, Void, List<AppInfo>>
            implements View.OnClickListener {

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            AppInfoProvider provider = new AppInfoProvider(AppManagerActivity.this);
            appInfoList.clear();
            appInfoList = provider.getInstalledApps();
            return appInfoList;
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfoList) {
            super.onPostExecute(appInfoList);
            if (appInfoList != null) {
                rlLoading.setVisibility(View.GONE);
                //默认加载“全部程序”
                adapter = new AppAdapter(appInfoList);
                lvApp.setAdapter(adapter);
                userAppInfoList.clear();
                systemAppInfoList.clear();
                //将应用程序分类
                for (AppInfo appInfo : appInfoList) {
                    if (appInfo.isUserApp()) {
                        userAppInfoList.add(appInfo);
                    } else {
                        systemAppInfoList.add(appInfo);
                    }
                }
                tvAllApp.setOnClickListener(this);
                tvUserApp.setOnClickListener(this);
                tvSystemApp.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //点击[全部程序]
                case R.id.tv_appmanager_all:
                    adapter = new AppAdapter(appInfoList);
                    lvApp.setAdapter(adapter);
                    break;
                //点击[用户程序]
                case R.id.tv_appmanager_user:
                    adapter = new AppAdapter(userAppInfoList);
                    lvApp.setAdapter(adapter);
                    break;
                //点击[系统程序]
                case R.id.tv_appmanager_system:
                    adapter = new AppAdapter(systemAppInfoList);
                    lvApp.setAdapter(adapter);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 自定义适配器AppAdapter继承自BaseAdapter类，用于关联lvApp与appInfoList
     */
    private class AppAdapter extends ArrayAdapter<AppInfo> {

        public AppAdapter(List<AppInfo> appInfoList) {
            super(AppManagerActivity.this, 0, appInfoList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.listview_appmanager_app, null);
                holder = new ViewHolder();
                holder.ivIcon = (ImageView) view.findViewById(R.id.iv_appmanager_appicon);
                holder.tvName = (TextView) view.findViewById(R.id.tv_appmanager_appname);
                holder.tvVersion = (TextView) view.findViewById(R.id.tv_appmanager_appversion);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            AppInfo appInfo = getItem(position);
            holder.ivIcon.setImageDrawable(appInfo.getAppIcon());
            holder.tvName.setText(appInfo.getAppName());
            holder.tvVersion.setText(appInfo.getAppVersion());

            return view;
        }

    }

    /**
     * 将Item中的控件使用static修饰，被static修饰的类的字节码在JVM中只会存在一份，其中的
     * 变量在栈中也只会存在一份
     */
    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvVersion;
    }
}


















