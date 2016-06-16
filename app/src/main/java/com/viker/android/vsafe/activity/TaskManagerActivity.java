package com.viker.android.vsafe.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.ProcessInfo;
import com.viker.android.vsafe.provider.ProcessInfoProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/6/4.
 * [进程管理]模块
 */
public class TaskManagerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TaskManagerActivity";

    //传入LoadProcessInfoTask的标记，用于区分用户点击的是用户进程还是系统进程，默认0是用户进程
    private int flag;

    //控件
    private TextView tvUserProcess;
    private TextView tvSystemProcess;
    private Button btnSelectAll;
    private Button btnOneKeyClear;
    private RelativeLayout rlLoading;
    private ListView lvProcess;
    private ProcessInfoAdapter adapter; //加载进程信息的适配器

    private List<ProcessInfo> userProcessInfoList; //用户进程列表
    private List<ProcessInfo> systemProcessInfoList; //系统进程列表


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskmanager);
        Log.d(TAG, "加载布局完成");
        initToolbar(); //初始化工具栏
        findViews(); //引用各控件
        tvUserProcess.setOnClickListener(this);
        tvSystemProcess.setOnClickListener(this);
        btnSelectAll.setOnClickListener(this);
        btnOneKeyClear.setOnClickListener(this);

        userProcessInfoList = new ArrayList<>();
        systemProcessInfoList = new ArrayList<>();

        //考虑到加载进程信息可能比较耗时，所以开启新线程来加载进程信息
        flag = 0;
        new LoadProcessTask().execute(flag); //传入0代表加载用户进程[默认]
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_taskmanager);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /*引用各控件*/
    private void findViews() {
        tvUserProcess = (TextView) findViewById(R.id.tv_taskmanager_userprocess);
        tvSystemProcess = (TextView) findViewById(R.id.tv_taskmanager_systemprocess);
        lvProcess = (ListView) findViewById(R.id.lv_taskmanager_process);
        btnSelectAll = (Button) findViewById(R.id.btn_taskmanager_selectall);
        btnOneKeyClear = (Button) findViewById(R.id.btn_taskmanager_onekeyclear);
        rlLoading = (RelativeLayout) findViewById(R.id.rl_taskmanager_loading);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_public, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //[导航键]
            case android.R.id.home:
                finish();
                break;
            case R.id.action_setting:
                Toast.makeText(this, "you click setting in taskManager.",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*点击事件的处理*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //[用户进程]
            case R.id.tv_taskmanager_userprocess:
                flag = 0;
                new LoadProcessTask().execute(flag);
                break;
            //[系统进程]
            case R.id.tv_taskmanager_systemprocess:
                flag = 1;
                new LoadProcessTask().execute(flag);
                break;
            //[全选]
            case R.id.btn_taskmanager_selectall:
                selectAll();
                break;
            //[一键清理]
            case R.id.btn_taskmanager_onekeyclear:
                oneKeyClear();
                break;
            default:
                break;
        }
    }

    /*[全选]按键的处理。根据flag标记来判断ListView中显示的用户进程还是系统进程，
    再将现所显示的进程列表都勾选上 */
    private void selectAll() {
        if (flag == 0) { //用户进程
            for (ProcessInfo processInfo : userProcessInfoList) {
                processInfo.setChecked(true);
                if (processInfo.getAppName().equals(getString(R.string.app_name))) {
                    processInfo.setChecked(false);
                }
                Log.d(TAG, processInfo.getAppName() + ">" + processInfo.isChecked());
            }
        } else {
            for (ProcessInfo processInfo : systemProcessInfoList) {
                processInfo.setChecked(true);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /*[一键清理]按键的处理。将所有已勾选的进程都Kill了，并显示释放了多少内存*/
    private void oneKeyClear() {
        //获取活动管理器，该管理器具有杀死进程的方法
        final ActivityManager activityManager = (ActivityManager)
                getSystemService(ACTIVITY_SERVICE);
        if (flag != 0) { //当勾选的是系统进程时，弹出警告对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("警告");
            builder.setMessage("杀死系统进程可能会导致系统崩溃");
            builder.setPositiveButton("清理", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //计数杀死的进程数量
                    int count = 0;
                    //计算要被杀死的全部进程所占用的RAM
                    long memorySize = 0;
                    /*创建一个被kill掉的进程的集合killedProcessList，用于添加被kill掉的进程，
                    等kill操作全部完成后再遍历当前的进程列表，将其中被killedProcessList包含的
                    进程移除[原因：在遍历当前进程列表的时候只能进行查询操作，不能进行增删等操作]*/
                    List<ProcessInfo> killedProcessList = new ArrayList<>();
                    for (ProcessInfo processInfo : systemProcessInfoList) {
                        if (processInfo.isChecked()) {
                            count++;
                            memorySize += processInfo.getMemorySize();
                            activityManager.killBackgroundProcesses(processInfo.getPackName());
                            killedProcessList.add(processInfo);
                        }
                    }
                    for (ProcessInfo killedProcessInfo : killedProcessList) {
                        if (systemProcessInfoList.contains(killedProcessInfo)) {
                            systemProcessInfoList.remove(killedProcessInfo);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(TaskManagerActivity.this, "共杀掉" + count +
                            "个进程，释放了" + Formatter.formatFileSize(TaskManagerActivity.
                            this, memorySize) + "内存", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else {//当勾选的是用户进程时，直接kill掉
            //计数杀死的进程数量
            int count = 0;
            //计算要被杀死的全部进程所占用的RAM
            long memorySize = 0;
             /*同样，创建一个被kill掉的进程的集合killedProcessList，用于添加被kill掉的进程，
               等kill操作全部完成后再遍历当前的进程列表，将其中被killedProcessList包含的
               进程移除[原因：在遍历当前进程列表的时候只能进行查询操作，不能进行增删等操作]*/
            List<ProcessInfo> killedProcessList = new ArrayList<>();
            for (ProcessInfo processInfo : userProcessInfoList) {
                Log.d(TAG, processInfo.getAppName());
                if (processInfo.isChecked()) {
                    Log.d(TAG, processInfo.getAppName() + ">" + processInfo.isChecked());
                    count++;
                    memorySize += processInfo.getMemorySize();
                    activityManager.killBackgroundProcesses(processInfo.getPackName());
                    killedProcessList.add(processInfo);
                    Log.d(TAG, processInfo.getAppName() + " was killed");
                }
            }
            for (ProcessInfo killedProcessInfo : killedProcessList) {
                if (userProcessInfoList.contains(killedProcessInfo)) {
                    userProcessInfoList.remove(killedProcessInfo);
                }
            }
            adapter.notifyDataSetChanged();
            Toast.makeText(TaskManagerActivity.this, "共杀掉" + count + "个进程，释放了" +
                    Formatter.formatFileSize(this, memorySize) +
                    "内存", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * 自定义LoadProcessTask类继承自AsyncTask类，开启新线程来加载进程信息
     */
    private class LoadProcessTask extends AsyncTask<Integer, Void, List<ProcessInfo>>
            implements AdapterView.OnItemClickListener {


        @Override
        protected List<ProcessInfo> doInBackground(Integer... flags) {
            int flag = flags[0]; //获取第一个标记[也仅有一个标记]
            //创建进程信息提供器
            ProcessInfoProvider provider = new ProcessInfoProvider(TaskManagerActivity.this);
            //通过进程信息提供器获取进程信息列表
            List<ProcessInfo> processInfoList = provider.getProcessInfoList();
            //清空进程列表，防止重复加载
            userProcessInfoList.clear();
            systemProcessInfoList.clear();
            //通过判断是否是用户程序将每个进程信息都分好类
            for (ProcessInfo processInfo : processInfoList) {
                if (processInfo.isUserProcess()) {
                    userProcessInfoList.add(processInfo);
                } else {
                    systemProcessInfoList.add(processInfo);
                }
            }
            //通过传入的标记flag来判断是显示用户进程还是系统进程[0代表用户进程，1代表系统进程]
            if (flag == 0) {
                return userProcessInfoList;
            } else {
                return systemProcessInfoList;
            }
        }

        @Override
        protected void onPostExecute(List<ProcessInfo> processInfoList) {
            super.onPostExecute(processInfoList);
            rlLoading.setVisibility(View.GONE);
            if (processInfoList != null) {
                adapter = new ProcessInfoAdapter(processInfoList);
                lvProcess.setAdapter(adapter);
                lvProcess.setOnItemClickListener(this);
            } else {
                Toast.makeText(TaskManagerActivity.this, "加载失败",
                        Toast.LENGTH_SHORT).show();
            }
        }

        /*ListView中的点击事件的处理*/
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //获取被选中对象
            ProcessInfo selectedProcessInfo = adapter.getItem(position);
            //如果被选中对象是本程序，则无法勾选，直接跳过。
            if (selectedProcessInfo.getPackName().equals(getPackageName())) {
                return;
            }
            CheckBox selectedCB = (CheckBox) view.findViewById(
                    R.id.cb_taskmanager_selectedprocess);
            if (selectedProcessInfo.isChecked()) {
                selectedProcessInfo.setChecked(false);
                selectedCB.setChecked(false);
            } else {
                selectedProcessInfo.setChecked(true);
                selectedCB.setChecked(true);
            }
        }
    }


    /**
     * 自定义适配器ProcessInfoAdapter继承自ArrayAdapter,用于加载进程信息
     */
    private class ProcessInfoAdapter extends ArrayAdapter<ProcessInfo> {

        public ProcessInfoAdapter(List<ProcessInfo> objects) {
            super(TaskManagerActivity.this, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            android.view.View view;
            ViewHolder holder;
            if (convertView == null) {
                view = getLayoutInflater().inflate(
                        R.layout.listview_taskmanager_process, null);
                holder = new ViewHolder();
                holder.ivIcon = (ImageView) view.findViewById(
                        R.id.iv_taskmanager_processicon);
                holder.tvProcessName = (TextView) view.findViewById(
                        R.id.tv_taskmanager_processname);
                holder.tvMemory = (TextView) view.findViewById(
                        R.id.tv_taskmanager_memory);
                holder.cbSelectedProcess = (CheckBox) view.findViewById(
                        R.id.cb_taskmanager_selectedprocess);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            //获取加载位置所对应的对象
            ProcessInfo processInfo = getItem(position);
            //在Item中加载显示数据
            holder.ivIcon.setImageDrawable(processInfo.getIcon());
            holder.tvProcessName.setText(processInfo.getAppName());
            holder.tvMemory.setText(android.text.format.Formatter.formatFileSize(
                    TaskManagerActivity.this, processInfo.getMemorySize()));
            holder.cbSelectedProcess.setChecked(processInfo.isChecked());

            return view;
        }
    }


    /**
     * 定义一个静态内部类来存储ListView中子项的控件变量，这样被static修饰的类的字节码在虚拟机中只会
     * 存在一份，其中的变量在栈中也只会存在一份
     */
    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvProcessName;
        TextView tvMemory;
        CheckBox cbSelectedProcess;
    }


}




















