package com.viker.android.vsafe.util;

import android.app.ProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Viker on 2016/5/26.
 * 用于下载升级文件的工具类
 */
public class DownloadUtil {

    public static File getFile(final String apkUrl, final String filePath,
                               final ProgressDialog progressDialog) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(apkUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            int max = connection.getContentLength();
            progressDialog.setMax(max);
            File file = new File(filePath);
            InputStream inputStream = connection.getInputStream();
            FileOutputStream fileOutputStream =
                    new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            int progress = 0; //初始化进度
            while ((len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
                //每读取一次就刷新一次下载进度
                progress += len;
                progressDialog.setProgress(progress);
            }
            //刷新缓存数据到文件中
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}











