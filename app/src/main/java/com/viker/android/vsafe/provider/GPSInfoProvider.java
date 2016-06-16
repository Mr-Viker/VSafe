package com.viker.android.vsafe.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Viker on 2016/5/28.
 * 进一步封装GPSInfoProvider位置提供者，提供精准度较高的位置信息
 * */

public class GPSInfoProvider {

    private static GPSInfoProvider gpsInfoProvider;
    private static LocationManager locationManager; //位置管理器
    private static MyListener myListener; //自定义的位置变化监听器

    //偏好设置，在这里主要是用于持久化位置的信息
    private static SharedPreferences sharedPreferences;


    /*使用单例模式，目的是减少往系统服务注册监听，避免程序挂掉，减少耗电量和内存消耗*/
    private GPSInfoProvider() {
    }

    public synchronized static GPSInfoProvider getInstance(Context context) {
        if (gpsInfoProvider == null) {
            gpsInfoProvider = new GPSInfoProvider();
            //获取位置管理器
            locationManager = (LocationManager) context.getSystemService(
                    Context.LOCATION_SERVICE);
            //获取查询地理位置的查询条件对象
            Criteria criteria = new Criteria();
            //设置精确度
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            //GPS定位是否允许产生开销
            criteria.setCostAllowed(true);
            //手机的功耗消耗情况
            criteria.setPowerRequirement(Criteria.POWER_HIGH);
            //获取海拔信息
            criteria.setAltitudeRequired(true);
            //对手机的移动速度是否敏感
            criteria.setSpeedRequired(true);
            //获取当前手机最好用的位置提供者
            String provider = locationManager.getBestProvider(criteria, true);
            myListener = new GPSInfoProvider().new MyListener();
            //调用更新位置方法
            locationManager.requestLocationUpdates(provider, 6000, 100, myListener);
            //创建一个config文件[如果不存在]，在监听器里使用偏好设置向config文件存入位置信息
            sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return gpsInfoProvider;
    }

    /*取消位置的监听*/
    public void stopLinsten() {
        locationManager.removeUpdates(myListener);
        myListener = null;
    }


    /*自定义一个位置变化的监听器，实现LocationListener接口*/
    protected class MyListener implements LocationListener {

        /*手机位置发生改变时调用的方法*/
        @Override
        public void onLocationChanged(Location location) {
            String latitude = "纬度：" + location.getLatitude();
            String longitude = "经度" + location.getLongitude();
            String meter = "精确度：" + location.getAccuracy();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("last_location", latitude + " " + longitude + " " + meter);
            editor.apply();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


    /*获取手机的位置信息*/
    public String getLocation() {
        return sharedPreferences.getString("last_location", "");
    }

}











