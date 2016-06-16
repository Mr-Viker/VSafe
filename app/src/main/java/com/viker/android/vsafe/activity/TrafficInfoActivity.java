package com.viker.android.vsafe.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.TrafficInfo;
import com.viker.android.vsafe.provider.TrafficInfoProvider;

import java.util.List;

/**
 * Created by Viker on 2016/6/5.
 * [流量统计]模块
 */
public class TrafficInfoActivity extends AppCompatActivity {

    private static final String TAG = "TrafficInfoActivity";

    //控件
    private RelativeLayout rlLoading; //加载数据时显示的进度条的父控件
    private ListView lvAppFlow; //用于显示应用程序流量的列表

    private List<TrafficInfo> trafficInfoList; //流量信息列表，流量信息包含了程序名和图标

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trafficinfo);
        initToolbar(); //初始化工具栏
        findViews(); //引用控件

        //考虑到获取流量信息列表是件较耗时的操作，因此开启子线程
        new LoadTrafficInfoTask().execute();
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_trafficinfo);
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

    /*选项菜单的点击事件的处理*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //导航键
            case android.R.id.home:
                finish();
                break;
            //[设置]
            case R.id.action_setting:
                Toast.makeText(this, "you click setting in trafficinfo.",
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*引用相关控件*/
    private void findViews() {
        rlLoading = (RelativeLayout) findViewById(R.id.rl_trafficinfo_loading);
        lvAppFlow = (ListView) findViewById(R.id.lv_trafficinfo_appflow);
    }


    /**
     * 新建内部类LoadTrafficInfoTask继承自AsyncTask类，用于获取TrafficInfoList并完成
     * 相应的界面更新
     */
    private class LoadTrafficInfoTask extends AsyncTask<Void, Void, List<TrafficInfo>> {

        @Override
        protected List<TrafficInfo> doInBackground(Void... params) {
            //创建流量信息提供器对象
            TrafficInfoProvider provider = new TrafficInfoProvider(TrafficInfoActivity.this);
            //获取流量信息列表
            trafficInfoList = provider.getTrafficInfoList();
            return trafficInfoList;
        }

        @Override
        protected void onPostExecute(List<TrafficInfo> trafficInfoList) {
            super.onPostExecute(trafficInfoList);
            //如果流量信息列表不为空，则显示在界面上
            if (trafficInfoList != null) {
                rlLoading.setVisibility(View.GONE);
                //创建适配器对象，关联数据源为trafficInfoList
                TrafficInfoAdapter adapter = new TrafficInfoAdapter();
                lvAppFlow.setAdapter(adapter);
            } else {
                Toast.makeText(TrafficInfoActivity.this, "加载失败",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }


    /**
     * 自定义适配器TrafficInfoAdapter继承自BaseAdapter[为什么不用ArrayAdapter呢，因为想玩玩0.0]，
     * 用于显示流量信息列表
     */
    private class TrafficInfoAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return trafficInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return trafficInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            //复用缓存
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.listview_trafficinfo_appflow, null);
                holder = new ViewHolder();
                holder.ivAppIcon = (ImageView) view.findViewById(R.id.iv_trafficinfo_appicon);
                holder.tvAppName = (TextView) view.findViewById(R.id.tv_trafficinfo_appname);
                holder.tvTx = (TextView) view.findViewById(R.id.tv_trafficinfo_tx);
                holder.tvRx = (TextView) view.findViewById(R.id.tv_trafficinfo_rx);
                holder.tvTotalx = (TextView) view.findViewById(R.id.tv_trafficinfo_totalx);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            //获取滑入屏幕中的流量信息对象
            TrafficInfo trafficInfo = (TrafficInfo) getItem(position);
            //将该对象的信息布局到视图中
            holder.ivAppIcon.setImageDrawable(trafficInfo.getAppIcon());
            holder.tvAppName.setText(trafficInfo.getAppName());
            //如果流量数据无法获取到[如有些设备不提供该功能]，则显示未知
            String tx;
            if (trafficInfo.getTx() >= 0) {
                tx = Formatter.formatFileSize(TrafficInfoActivity.this, trafficInfo.getTx());
            } else {
                tx = "未知";
            }
            holder.tvTx.setText(tx); //显示上传流量
            String rx;
            if (trafficInfo.getRx() >= 0) {
                rx = Formatter.formatFileSize(TrafficInfoActivity.this, trafficInfo.getRx());
            } else {
                rx = "未知";
            }
            holder.tvRx.setText(rx); //显示下载流量
            //若tx或rx无法获取到则总流量也显示未知
            String totalx;
            if ("未知".equals(tx) || "未知".equals(rx)) {
                totalx = "未知";
            } else {
                totalx =Formatter.formatFileSize(TrafficInfoActivity.this,
                        trafficInfo.getTx()+trafficInfo.getRx());
            }
            holder.tvTotalx.setText(totalx); //显示总流量

            return view;
        }
    }


    /**
     * 新建一个内部静态类，静态类在虚拟机中只会存在一份字节码，其中的变量在栈中也只会存在一份
     * 且被共用
     */
    private static class ViewHolder {
        ImageView ivAppIcon;
        TextView tvAppName;
        TextView tvTx;
        TextView tvRx;
        TextView tvTotalx;
    }


}


















