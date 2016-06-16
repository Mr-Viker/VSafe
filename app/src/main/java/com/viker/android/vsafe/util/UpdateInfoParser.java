package com.viker.android.vsafe.util;

import android.util.Xml;

import com.viker.android.vsafe.model.UpdateInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Viker on 2016/5/25.
 * 将从服务端获取的Xml格示配置信息解析成UpdateInfo模型类对象
 */
public class UpdateInfoParser {

    public static UpdateInfo getUpdateInfo(InputStream inputStream)
            throws IOException, XmlPullParserException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "utf-8");
        UpdateInfo updateInfo = new UpdateInfo();//用于存放解析好的数据。
        int type = parser.getEventType();
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG) {
                switch (parser.getName()) {
                    case "version":
                        updateInfo.setVersion(parser.nextText());
                        break;
                    case "description":
                        updateInfo.setDescription(parser.nextText());
                        break;
                    case "apkurl":
                        updateInfo.setApkUrl(parser.nextText());
                        break;
                }
            }
            type = parser.next();
        }
        return updateInfo;
    }
}









