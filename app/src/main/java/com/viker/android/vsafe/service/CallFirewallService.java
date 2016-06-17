package com.viker.android.vsafe.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.viker.android.vsafe.db.BlackNumberDao;

import java.lang.reflect.Method;

/**
* Created by Viker on 2016/6/2.
* 实现黑名单电话拦截功能的服务类，属于通信卫士中，根据设置中心的开关来开启或关闭服务。
*/

public class CallFirewallService extends Service {

    private static final String TAG = "CallFirewallService";

    //数据库操作对象
    private BlackNumberDao dao;

    //电话管理器
    private TelephonyManager telephonyManager;
    //自定义的监听器[监听电话状态改变]
    private MyPhoneListener listener;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //第一次创建服务时调用
    @Override
    public void onCreate() {
        super.onCreate();
        dao = BlackNumberDao.getInstance(this);
        //获取电话管理器
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //创建监听器
        listener = new MyPhoneListener();
        //为系统的电话服务设置监听器，监听电话状态的改变
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    //在销毁服务时注销监听器
    @Override
    public void onDestroy() {
        super.onDestroy();
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
        listener = null;
    }

    //自定义监听器MyPhoneListener继承自PhoneStateListener类，实现监听电话状态的改变
    private class MyPhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                //当电话的状态为正在响铃时
                case TelephonyManager.CALL_STATE_RINGING:
                    //判断是否是黑名单列表中的号码，通过queryMode方法来判断[返回的是拦截模式，
                    //如果拦截模式为空则代表没有该黑名单号码]
                    String mode = dao.queryMode(incomingNumber);
                    if (mode.equals("电话拦截") || mode.equals("全部拦截")) {
                        //挂断该电话
                        endCall(incomingNumber);
                    }
                    break;
                //空闲状态
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                //通话状态
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
            }
        }


    }

    private void endCall(String incomingNumber) {
        try {
            //利用反射获取系统的service方法
            Method method = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{TELEPHONY_SERVICE});
            //通过aidl实现方法的调用
            ITelephony telephony = ITelephony.Stub.asInterface(binder);
            //该方法是个异步方法，会新开启一个线程将呼入的号码存入数据库中
            telephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}



















