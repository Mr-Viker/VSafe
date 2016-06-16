package com.viker.android.vsafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.model.ContactInfo;
import com.viker.android.vsafe.provider.ContactInfoProvider;

import java.util.List;

/**
 * Created by Viker on 2016/5/27.
 */
public class SelectContactActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "SelectContactActivity";

    private ContactInfoProvider provider; //用于提供联系人列表的自定义提供器

    private List<ContactInfo> contactInfoList; //联系人数据列表

    private ListView lvSelectContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectcontact);
        initToolbar();

        lvSelectContact = (ListView) findViewById(R.id.lv_selectcontact_contact);
        Log.d(TAG, "加载完ListView");

        //通过自定义的提供器将查询到的联系人列表返回
        provider = new ContactInfoProvider(this);
        contactInfoList = provider.getContactInfoList();
        Log.d(TAG, "获取到联系人列表" + contactInfoList.toString());
        ContactAdapter adapter = new ContactAdapter(contactInfoList);
        lvSelectContact.setAdapter(adapter);
        lvSelectContact.setOnItemClickListener(this);
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_selectcontact);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    /*创建工具栏中的选项菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_public, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*选项菜单点击事件的处理*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_setting:
                Toast.makeText(SelectContactActivity.this, "you click setting",
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*监听联系人列表ListView，当用户点击其子项时，将该子项的手机号码返回给本活动调用
    者ProtectedSetup3Activity[通过setResult方法]*/
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ContactInfo contactInfo = (ContactInfo) lvSelectContact
                .getAdapter().getItem(position);
        String safeNumber = contactInfo.getPhone();
        Intent backSetup3 = new Intent();
        backSetup3.putExtra("safeNumber", safeNumber);
        setResult(RESULT_OK, backSetup3);
        finish();
    }


    /*自定义适配器，用于关联ListView和ContactInfoList，显示联系人姓名和手机号码*/
    private class ContactAdapter extends ArrayAdapter<ContactInfo> {

        public ContactAdapter(List<ContactInfo> contactInfoList) {

            super(SelectContactActivity.this, 0, contactInfoList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(
                        R.layout.listview_selectcontact_item, null);
            }

            ContactInfo info = getItem(position);
            TextView tvName = (TextView) convertView.findViewById(
                    R.id.tv_selectcontact__item_name);
            tvName.setText(info.getName());
            TextView tvPhone = (TextView) convertView.findViewById(
                    R.id.tv_selectcontact__item_phone);
            tvPhone.setText(info.getPhone());
            return convertView;
        }

    }


}












