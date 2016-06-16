package com.viker.android.vsafe.provider;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.viker.android.vsafe.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/6/3.
 * 用于获取手机中的所有应用程序信息并提供给调用者，属于程序管理模块
 */
public class AppInfoProvider {

    private PackageManager packageManager;

    public AppInfoProvider(Context context) {
        packageManager = context.getPackageManager();
    }

    public List<AppInfo> getInstalledApps() {
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        List<AppInfo> appInfoList = new ArrayList<>();
        for (PackageInfo info : packageInfoList) {
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(info.packageName);
            appInfo.setAppName(info.applicationInfo.loadLabel(packageManager).toString());
            appInfo.setAppVersion(info.versionName);
            appInfo.setAppIcon(info.applicationInfo.loadIcon(packageManager));
            appInfo.setUserApp(filterApp(info.applicationInfo));
            appInfoList.add(appInfo);
            appInfo = null;
        }
        return appInfoList;
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
