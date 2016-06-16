package com.viker.android.vsafe.provider;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.ProcessInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/6/4.
 * [进程信息]提供器，属于[进程管理]Module，主要是进一步封装了RunningAppProcessInfo[通过
 * ActivityManager获得]和ApplicationInfo[通过PackageManager获得]。
 */
public class ProcessInfoProvider {

    private static final String TAG = "ProcessInfoProvider";

    private Context context;

    public ProcessInfoProvider(Context context) {
        this.context = context;
    }

    public List<ProcessInfo> getProcessInfoList() {
        //获取活动管理器
        ActivityManager activityManager = (ActivityManager) context.
                getSystemService(Context.ACTIVITY_SERVICE);
        //获取包管理器，用于获取Application
        PackageManager packageManager = context.getPackageManager();
        //用于存放所需进程信息的列表
        List<ProcessInfo> processInfoList = new ArrayList<>();
        //获取正在运行进程的列表
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfoList =
                activityManager.getRunningAppProcesses();
        //遍历该列表，然后将其中每个进程中的所需信息封装在ProcessInfo对象中，
        // 最后存入processInfoList中返回
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo :
                runningAppProcessInfoList) {
            ProcessInfo processInfo = new ProcessInfo();
            String packageName = runningAppProcessInfo.processName;
            processInfo.setPackName(packageName); //设置包名[即进程名]
            int pid = runningAppProcessInfo.pid;
            processInfo.setPid(pid); //设置pid
            //获取到该进程对应的程序所占用的RAM空间
            long memorySize = activityManager.getProcessMemoryInfo(new int[]{pid})[0]
                    .getTotalPrivateDirty() * 1024;
            processInfo.setMemorySize(memorySize); //设置所占用的RAM
            ApplicationInfo applicationInfo;
            String appName = "";
            Drawable icon = null;
            try {
                //通过包名获取ApplicationInfo对象，其包含了程序名和图标
                applicationInfo = packageManager.
                        getApplicationInfo(packageName, 0);
                //设置是否是用户进程
                processInfo.setUserProcess(filterApp(applicationInfo));
                processInfo.setChecked(false); //设置是否被选中，默认没有
                //获取程序名
                appName = applicationInfo.loadLabel(packageManager).toString();
                //设置程序名
                processInfo.setAppName(appName);
                //获取图标
                icon = applicationInfo.loadIcon(packageManager);
                //设置图标
                processInfo.setIcon(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                //如果程序名为空，则用包名代替
                if (TextUtils.isEmpty(appName)) {
                    appName = packageName;
                    //设置程序名
                    processInfo.setAppName(appName);
                    Log.d(TAG, "将包名赋值给程序名：" + packageName + ">" + appName);
                }
                //如果程序图标为空，则用系统默认的图标代替
                if (icon == null) {
                    icon = context.getResources().getDrawable(R.drawable.appicon_default);
                    //设置图标
                    processInfo.setIcon(icon);
                }
            }
            processInfoList.add(processInfo);
            processInfo = null;
        }
        return processInfoList;
    }

    /*判断出该程序是否是用户程序*/
    private boolean filterApp(ApplicationInfo info) {
        //当前应用程序的标记与系统应用程序的标记
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return true;
        } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return true;
        }
        return false;
    }

}










