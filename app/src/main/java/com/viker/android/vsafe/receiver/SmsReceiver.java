package com.viker.android.vsafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.db.BlackNumberDao;
import com.viker.android.vsafe.provider.GPSInfoProvider;

/**
 * Created by Viker on 2016/5/28.
 * 系统在接收到短信之后会发送一条广播，本广播接受器接受该广播并获取其Intent中包含的短信内容
 * 属于[手机防盗]与[通信卫士]模块共用，主要作用是：
 * 1.根据安全号码发送的防盗指令来完成相应的操作
 * 2.判断发送者是否是本地黑名单数据库中的黑名单号码，如果是则拦截
 */


public class SmsReceiver extends BroadcastReceiver {

    private SharedPreferences sharedPreferences;

    private Context context;

    //手机设备管理器
    private DevicePolicyManager devicePolicyManager;
    //组件[稍后创建时会与MyAdmin相关联]
    private ComponentName adminName;

    //黑名单数据库访问对象
    private BlackNumberDao dao;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        //获取黑名单数据库访问对象
        dao = BlackNumberDao.getInstance(context);
        sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        String safeNumber = sharedPreferences.getString("safe_number", "");
        //获取短信中的内容。系统接受到一个信息广播时，会将接受到的信息存放在Intent中的pdus数组中
        SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        //获取手机设备管理器
        devicePolicyManager = (DevicePolicyManager) context.
                getSystemService(Context.DEVICE_POLICY_SERVICE);
        //创建一个与MyAdmin相关联的组件[获取到了管理权限]
        adminName = new ComponentName(context, MyAdmin.class);
        //遍历出信息中的所有内容
        for (SmsMessage smsMessage : smsMessages) {
            //获取发件人的地址
            String sender = smsMessage.getOriginatingAddress();
            //查询黑名单数据库是否含有该号码，如果发送者是黑名单号码并且拦截模式是[短信拦截]
            // 或是[全部拦截]则拦截短信
            String mode = dao.queryMode(sender);
            if ("短信拦截".equals(mode) || "全部拦截".equals(mode)) {
                abortBroadcast();
            }
            //获取消息体
            String body = smsMessage.getMessageBody();
            //如果发送者是安全号码并且消息体不为空的话，则根据指令来进行相应的操作
            if (safeNumber.equals(sender)) {
                if (!TextUtils.isEmpty(body)) {
                    orderAction(safeNumber, body);
                }
            }
        }
    }

    private void orderAction(String safeNumber, String body) {
        switch (body) {
            //发送位置信息给安全号码
            case "#*location*#":
                //获取当前位置信息
                String lastLocation = GPSInfoProvider.getInstance(context).getLocation();
                //如果位置信息不为空则发送当前位置信息给安全号码
                if (!TextUtils.isEmpty(lastLocation)) {
                    //获取信息管理器
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(safeNumber, null, lastLocation, null, null);
                }
                abortBroadcast(); //截断广播
                break;
            //播放报警音乐
            case "#*alarm*#":
                //获取音频播放器
                MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
                player.setVolume(1.0f, 1.0f); //即使静音模式也有音乐的声音
                player.start();
                abortBroadcast();
                break;
            //清除数据
            case "#*wipedata*#":
                //判断手机的管理员权限是否激活。只有激活后才可以执行锁屏、清除数据、重置
                //出厂设置等操作
                if (devicePolicyManager.isAdminActive(adminName)) {
                    devicePolicyManager.wipeData(0); //清除设备中的数据，手机会重启
                }
                abortBroadcast();
                break;
            //远程锁屏
            case "#*lockscreen*#":
                if (devicePolicyManager.isAdminActive(adminName)) {
                    devicePolicyManager.resetPassword("7890", 0); //设置屏幕解锁密码为7890
                    devicePolicyManager.lockNow(); //锁屏
                }
                abortBroadcast();
                break;
            default:
                break;
        }
    }

}







