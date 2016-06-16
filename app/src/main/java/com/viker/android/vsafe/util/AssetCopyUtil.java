package com.viker.android.vsafe.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Viker on 2016/6/6.
 * 将资源文件复制到手机系统中的工具类。
 */
public class AssetCopyUtil {

    public static void copyAntivirus(Context context, String assetFileName,
                                     String destFileName) {
        InputStream in = null;
        FileOutputStream fos = null;
        try {
            //打开资源文件作为输入流
            in = context.getAssets().open(assetFileName);
            //获取根据传入的[目标文件路径]参数的文件，如果不存在则会新建该文件
            File file = new File(destFileName);
            //将该目标文件对象作为输出流的输出文件
            fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int len;
            while ((len = in.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
