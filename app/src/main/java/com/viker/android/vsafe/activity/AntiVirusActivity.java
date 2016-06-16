package com.viker.android.vsafe.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.db.AntiVirusDao;
import com.viker.android.vsafe.model.PackageVirusInfo;
import com.viker.android.vsafe.util.MD5Encoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/6/6.
 */
public class AntiVirusActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AntiVirusActivity";

    //卸载病毒程序时向系统卸载程序传入的请求码，需要根据该请求码以及返回码来更新视图
    private static final int REQUEST_CODE = 10;

    //控件
    private ImageView ivScan;
    private ProgressBar pbScanProgress;
    private LinearLayout llScanStatus;
    private Button btnScan;
    private Button btnClear;
    private TextView tvStatus;

    //包管理器
    PackageManager packageManager;

    //病毒包信息列表
    List<PackageInfo> virusPackageInfoList;

    //清理病毒程序时进度条进度
    int count;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_antivirus);
        initToolbar(); //初始化工具栏
        findViews(); //引用控件
        tvStatus.setText("一切准备就绪");
        btnScan.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        packageManager = getPackageManager();
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_antivirus);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /*引用控件*/
    private void findViews() {
        ivScan = (ImageView) findViewById(R.id.iv_antivirus_scan);
        pbScanProgress = (ProgressBar) findViewById(R.id.pb_antivirus_scanprogress);
        llScanStatus = (LinearLayout) findViewById(R.id.ll_antivirus_scanstatus);
        btnScan = (Button) findViewById(R.id.btn_antivirus_scan);
        btnClear = (Button) findViewById(R.id.btn_antivirus_clear);
        tvStatus = (TextView) findViewById(R.id.tv_antivirus_status);
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
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_setting:
                Toast.makeText(this, "you click setting in antivirus.",
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //[扫描]
            case R.id.btn_antivirus_scan:
                //当点击扫描按钮时，开启子线程进行手机扫描病毒
                new ScanTask().execute();
                break;
            //[清理]
            case R.id.btn_antivirus_clear:
                if (virusPackageInfoList == null) {
                    Toast.makeText(AntiVirusActivity.this, "无病毒可清理",
                            Toast.LENGTH_SHORT).show();
                } else {
                    tvStatus.setText("正在清理...");
                    //设置进度条最大值为病毒列表的长度
                    pbScanProgress.setMax(virusPackageInfoList.size());
                    count = 0; //清理病毒时进度条进度
                    for (PackageInfo packageInfo : virusPackageInfoList) {
                        Uri packageUri = Uri.parse("package:" + packageInfo.packageName);
                        Intent toUninstall = new Intent(Intent.ACTION_DELETE, packageUri);
                        startActivityForResult(toUninstall, REQUEST_CODE);
                    }
                    tvStatus.setText("清理完毕");
                    TextView tvKillFinish = new TextView(this);
                    tvKillFinish.setText("清理完毕，一共清理了" + count + "个病毒");
                    tvKillFinish.setTextColor(getResources().getColor(R.color.BLACK));
                    llScanStatus.addView(tvKillFinish,0);
                }
                break;
            default:
                break;
        }
    }

    /*清理病毒时系统卸载程序的返回结果处理*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                String appName = virusPackageInfoList.get(count).applicationInfo.
                        loadLabel(packageManager).toString();
                if (resultCode == 0) {
                    count++;
                    pbScanProgress.setProgress(count);
                    TextView tvShowKilledVirus = new TextView(AntiVirusActivity.this);
                    tvShowKilledVirus.setText("已清理" + appName);
                    llScanStatus.addView(tvShowKilledVirus,0);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 新建ScanTask类继承自AsyncTask类，用于开启子线程进行手机扫描病毒
     * 扫描病毒原理：提取所扫描的应用程序的特征码[即签名]，然后查询已经复制到手机系统中[即手机
     * 卫士目录下的]病毒特征码数据库文件"antivirus.db"，对比即可实现病毒的查询
     * 清理杀毒的原理：其实就是卸载病毒程序
     */
    private class ScanTask extends AsyncTask<Void, PackageVirusInfo, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvStatus.setText("正在查杀...");
            //设置一个旋转动画，即手机杀毒在进行扫描时会一直在旋转的雷达动画
            RotateAnimation ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f);
            ra.setDuration(1000);  //设置旋转一次为1s
            ra.setRepeatCount(Animation.INFINITE); //设置旋转的重复次数为一直旋转
            ra.setRepeatMode(Animation.RESTART); //设置旋转的模式为旋转完一个回合后重新旋转
            ra.reset(); //重置动画，保证每次开启都是从起点开始旋转
            ivScan.startAnimation(ra); //为该视图开启旋转动画
        }

        @Override
        protected Integer doInBackground(Void... params) {
            //获取病毒数据库访问对象
            AntiVirusDao dao = AntiVirusDao.getInstance(AntiVirusActivity.this);
            //使用包管理器获取具有签名信息的包信息
            List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(
                    PackageManager.GET_SIGNATURES);
            //设置扫描进度条的最大值为包信息列表的长度
            pbScanProgress.setMax(packageInfoList.size());
            int scanCount = 0; //进度条进度
            //创建病毒程序信息列表
            virusPackageInfoList = new ArrayList<>();
            //遍历包含应用程序签名的包信息列表，然后与数据库文件进行比较
            for (PackageInfo packageInfo : packageInfoList) {
                //获取包信息对象的第一个特征码
                String signature = packageInfo.signatures[0].toCharsString();
                //因为数据库中的病毒特征码都是以MD5的形式存在的，所以也要将签名信息转换
                //成MD5值
                String md5 = MD5Encoder.encode(signature);
                //将md5和病毒特征码进行比较[即查询数据库是否含有该md5]
                String result = dao.getVirusInfo(md5);
                //创建PackageVirusInfo对象
                PackageVirusInfo packageVirusInfo = new PackageVirusInfo();
                packageVirusInfo.setPackageInfo(packageInfo); //设置包信息
                //如果返回结果为空则表示该程序并不是病毒
                if (result == null) {
                    packageVirusInfo.setVirus(false);
                } else {
                    packageVirusInfo.setVirus(true);
                    //如果该程序是病毒，则添加该包信息到virusPackageInfoList
                    virusPackageInfoList.add(packageInfo);
                }
                scanCount++;
                pbScanProgress.setProgress(scanCount); //设置进度条进度
                publishProgress(packageVirusInfo); //将包信息和是否是病毒的信息设置到显示屏
            }
            return scanCount;
        }

        /*设置包信息和是否是病毒的信息到显示屏*/
        @Override
        protected void onProgressUpdate(PackageVirusInfo... packageVirusInfos) {
            super.onProgressUpdate(packageVirusInfos);
            PackageVirusInfo info = packageVirusInfos[0];
            //获取程序名
            String appName = info.getPackageInfo().applicationInfo.
                    loadLabel(packageManager).toString();
            //获取该程序是否是病毒的boolean值
            boolean virus = info.isVirus();
            TextView tvShowScanInfo = new TextView(AntiVirusActivity.this);
            if (virus) {
                tvShowScanInfo.setText("发现 " + appName + "  含有病毒");
                tvShowScanInfo.setTextColor(getResources().getColor(R.color.RED));
            } else {
                tvShowScanInfo.setText("扫描 " + appName + "  安全");
            }
            llScanStatus.addView(tvShowScanInfo, 0); //显示该TextView
        }

        /*当扫描完毕后显示共扫描了多少个文件*/
        @Override
        protected void onPostExecute(Integer scanCount) {
            super.onPostExecute(scanCount);
            ivScan.clearAnimation();
            tvStatus.setText("扫描完毕");
            TextView tvScanFinish = new TextView(AntiVirusActivity.this);
            tvScanFinish.setText("扫描完毕，一共扫描了" + scanCount + "个文件");
            tvScanFinish.setTextColor(getResources().getColor(R.color.BLACK));
            llScanStatus.addView(tvScanFinish, 0);
        }
    }

}





















