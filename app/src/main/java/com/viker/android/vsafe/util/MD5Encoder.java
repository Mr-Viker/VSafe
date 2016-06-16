package com.viker.android.vsafe.util;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by Viker on 2016/5/26.
 * 工具类：利用MD5将密码加密
 */
public class MD5Encoder {

    public static String encode(String password) {
        try {
            //获取到数字消息的摘要器
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //执行加密操作
            byte[] result = digest.digest(password.getBytes());
            return Arrays.toString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
