package com.viker.android.vsafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

/**
 * Created by Viker on 2016/5/28.
 * 可以接受系统开机启动的广播接受器。主要是用于开机后检查手机SIM卡是否换了
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "config", Context.MODE_PRIVATE);
        boolean protectStatus = sharedPreferences.getBoolean("protect_status", false);
        //如果绑定了SIM卡，则获取现在的SIM卡号码和保存在config中的SIM卡号码进行比较
        if (protectStatus) {
            String savedSim = sharedPreferences.getString("sim_number", "");
            //通过电话管理器获取SIM卡号码
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
            String realSim = telephonyManager.getSimSerialNumber();
            //如果现在手机上的SIM卡和绑定的SIM卡不相同，则向安全号码发送报警短信
            if (!savedSim.equals(realSim)) {
                String safeNumber = sharedPreferences.getString("safe_number", "");
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(safeNumber, null, "SIM卡已更换", null, null);
            }
        }
    }

}













