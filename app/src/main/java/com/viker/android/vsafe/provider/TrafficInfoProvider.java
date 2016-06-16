package com.viker.android.vsafe.provider;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

import com.viker.android.vsafe.model.TrafficInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/6/5.
 * [流量信息]提供器，向调用者提供设备上所有具有网络访问权限的程序的相关信息
 */
public class TrafficInfoProvider {

    private Context context;

    public TrafficInfoProvider(Context context) {
        this.context = context;
    }

    /*内部封装了获取TrafficInfo列表操作，返回一个流量信息列表给调用者*/
    public List<TrafficInfo> getTrafficInfoList() {
        //用于存放获取到的流量信息对象
        List<TrafficInfo> trafficInfoList = new ArrayList<>();
        //获取包管理器，用于获取具有访问权限的包信息列表
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(
                PackageManager.GET_PERMISSIONS);
        //遍历该列表，获取具有网络访问权限的包信息对象。
        for (PackageInfo packageInfo : packageInfoList) {
            //获取每个包信息对象所具有的全部访问权限信息
            String[] permissions = packageInfo.requestedPermissions;
            //遍历该访问权限信息数组，如果其中有网络访问权限，则创建TrafficInfo对象，
            //然后获取所需该包的信息，最后将该对象添加进流量信息列表trafficInfoList
            if (permissions != null && permissions.length > 0) {
                for (String permission : permissions) {
                    if ("android.permission.INTERNET".equals(permission)) {
                        //创建TrafficInfo对象
                        TrafficInfo trafficInfo = new TrafficInfo();
                        //获取该具有网络访问权限的packageInfo的信息
                        trafficInfo.setAppIcon(packageInfo.applicationInfo.
                                loadIcon(packageManager));
                        trafficInfo.setAppName(packageInfo.applicationInfo.
                                loadLabel(packageManager).toString());
                        //获取uid，因为获取流量信息时需要向TrafficStats的静态方法传入uid
                        int uid = packageInfo.applicationInfo.uid;
                        //获取该程序的上传和下载所用的流量信息
                        long rx = TrafficStats.getUidRxBytes(uid);
                        long tx = TrafficStats.getUidTxBytes(uid);
                        trafficInfo.setRx(rx);
                        trafficInfo.setTx(tx);
                        trafficInfoList.add(trafficInfo);
                        trafficInfo = null;
                        break;
                    }
                }
            }
        }
        return trafficInfoList;
    }

}












