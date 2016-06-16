package com.viker.android.vsafe.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.UpdateInfo;
import com.viker.android.vsafe.util.AssetCopyUtil;
import com.viker.android.vsafe.util.DownloadUtil;
import com.viker.android.vsafe.util.UpdateInfoParser;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 欢迎页面：主要进行一些初始化操作，如初始化数据库，文件复制，读取配置文件等。
 * 并联网进行检查升级。
 */
public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";

    //向Handler发送的消息标识。
    private static final int GET_INFO_SUCCESS = 0; //表示获取最新版本信息成功
    private static final int GET_INFO_ERROR = 1; //表示联网检查更新失败
    private static final int SERVER_ERROR = 2; //表示网络连接失败
    private static final int DOWNLOAD_SUCCESS = 10; //表示下载成功
    private static final int DOWNLOAD_ERROR = 11; //表示下载失败

    //用于从应用文件中读取config文件，里面包含一些如自动更新是否开启的值等。
    private SharedPreferences sharedPreferences;

    private RelativeLayout rlLogo;
    private TextView tvVersion;
    private ProgressDialog progressDialog;

    private UpdateInfo updateInfo;//存放解析好的配置信息
    private long startTime;//发送网络请求开始时间
    private long endTime;//网络传输结束时间

    //创建一个Handler实例用于获取子线程发送的消息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_INFO_SUCCESS:
                    String serverVersion = updateInfo.getVersion();
                    String currentVersion = getVersion();
                    if (currentVersion.equals(serverVersion)) {
                        loadMainUI(); //版本相同则表示是最新版本，直接进入主界面
                    } else {
                        showUpdateDialog(); //版本不同则弹出升级提示对话框
                    }
                    break;
                case GET_INFO_ERROR:
                    Toast.makeText(getApplicationContext(), "获取最新版本信息失败",
                            Toast.LENGTH_SHORT).show();
                    loadMainUI();
                    break;
                case SERVER_ERROR:
                    Toast.makeText(getApplicationContext(), "网络连接失败",
                            Toast.LENGTH_SHORT).show();
                    loadMainUI();
                    break;
                case DOWNLOAD_SUCCESS:
                    File file = (File) msg.obj;
                    installApk(file);
                    break;
                case DOWNLOAD_ERROR:
                    Toast.makeText(getApplicationContext(), "下载失败",
                            Toast.LENGTH_SHORT).show();
                    loadMainUI();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置为全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        rlLogo = (RelativeLayout) findViewById(R.id.rl_splash_logo);
        tvVersion = (TextView) findViewById(R.id.tv_splash_version);
        tvVersion.setText("版本号: " + getVersion());
        //设置启动界面动画效果
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
        alphaAnimation.setDuration(2000);
        rlLogo.setAnimation(alphaAnimation);
        //开启子线程连接服务器获取服务器上的配置信息
        new Thread(new CheckVersionTask()).start();
        /*开启子线程，判断手机系统中是否包含了antivirus.db文件，如果没有则将asset目录下
         的antivirus.db文件复制到手机系统中，如果已有则不操作该手机系统其实是指
         手机的/data/data/com.viker.android.vsafe/files目录*/
        new Thread(new CopyAntivirusTask()).start();
    }

    //通过系统的包管理器获取当前App的版本号
    private String getVersion() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    /*开启子线程连接服务器获取服务器上的配置信息*/
    private class CheckVersionTask implements Runnable {
        @Override
        public void run() {
            //从config配置文件中读取自动更新是否开启的值
            sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            boolean autoUpdate = sharedPreferences.getBoolean("auto_update", true);
            //如果未开启自动更新则等待两秒后直接进入主界面，否则联网获取服务器端的APP版本信息
            if (!autoUpdate) {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loadMainUI();
            } else {
                startTime = System.currentTimeMillis();
                Message message = Message.obtain();
                try {
                    String serverUrl = getResources().getString(R.string.serverurl);
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    int code = connection.getResponseCode();
                    //当响应码为200时，表示与服务端连接成功
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        //将解析好的数据赋值给updateInfo实例
                        updateInfo = UpdateInfoParser.getUpdateInfo(inputStream);
                        endTime = System.currentTimeMillis();
                        long resultTime = endTime - startTime;
                        if (resultTime < 2000) {
                            try {
                                Thread.sleep(2000 - resultTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        message.what = GET_INFO_SUCCESS;
                        handler.sendMessage(message);
                    } else { //获取最新版本信息失败
                        message.what = GET_INFO_ERROR;
                        handler.sendMessage(message);
                        endTime = System.currentTimeMillis();
                        long resultTime = endTime - startTime;
                        if (resultTime < 2000) {
                            try {
                                Thread.sleep(2000 - resultTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {//网络连接失败
                    message.what = SERVER_ERROR;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 开启子线程将assets目录下的病毒数据库文件antivirus.db复制到手机系统中，其中包含了病毒特征码，
     * 在手机杀毒模块中需要使用到这些特征码
     */
    private class CopyAntivirusTask implements Runnable {
        @Override
        public void run() {
            //获取手机系统中是否存在antivirus.db文件，如果不存在则会新建该文件
            File file = new File(getFilesDir(), "antivirus.db");
            //如果不存在或者为空则从assets目录下复制到手机系统中
            if ((!file.exists()) || file.length() <= 0) {
                //调用工具类将asset目录下的该文件复制到手机系统中
                AssetCopyUtil.copyAntivirus(getApplicationContext(), "antivirus.db",
                        file.getAbsolutePath());
            }
        }
    }



    //进入主界面
    private void loadMainUI() {
        Intent toMain = new Intent(this, MainActivity.class);
        startActivity(toMain);
        finish();
    }

    //升级提示对话框
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(getResources().getDrawable(R.drawable.update_notification));
        builder.setTitle("升级提示");
        builder.setMessage(updateInfo.getDescription());
        //设置升级按键并设置监听器，点击升级则启动下载并显示下载进度条
        builder.setPositiveButton("升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //创建下载进度条和配置参数
                progressDialog = new ProgressDialog(SplashActivity.this);
                progressDialog.setMessage("正在下载...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                //如果有找到可用内存空间，则开始下载
                if (Environment.MEDIA_MOUNTED.equals(
                        Environment.getExternalStorageState())) {
                    progressDialog.show();
                    //开启子线程下载apk
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String apkUrl = updateInfo.getApkUrl(); //下载地址
                            String fileName = apkUrl.substring(apkUrl.lastIndexOf("/") + 1,
                                    apkUrl.length()); //通过下载地址获取最新App名
                            File file = new File(Environment.getExternalStorageDirectory(),
                                    fileName); //在本地文件夹中创建一个文件
                            file = DownloadUtil.getFile(apkUrl, file.getAbsolutePath(),
                                    progressDialog);
                            Message message = Message.obtain();
                            if (file != null) {
                                //向主线程发送下载成功的消息
                                message.what = DOWNLOAD_SUCCESS;
                                message.obj = file;
                                handler.sendMessage(message);
                            } else {
                                //向主线程发送下载失败的消息
                                message.what = DOWNLOAD_ERROR;
                                handler.sendMessage(message);
                            }
                            progressDialog.dismiss();//下载结束后将进度条关闭
                        }
                    }).start();
                } else {
                    Toast.makeText(getApplicationContext(), "未找到可用内存空间",
                            Toast.LENGTH_SHORT).show();
                    loadMainUI();
                }
            }
        });
        //设置取消按键，点击取消则进入主界面
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadMainUI();
            }
        });
        builder.create().show();
    }


    //替换安装下载好的apk文件
    //主要是通过Intent调用系统中的PackageInstaller来完成安装工作
    private void installApk(File file) {
        Intent toInstaller = new Intent();
        toInstaller.setAction(Intent.ACTION_VIEW);
        toInstaller.addCategory(Intent.CATEGORY_DEFAULT);
        //添加调用安装器所需传入的文件名和类型
        toInstaller.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(toInstaller);
    }

}












