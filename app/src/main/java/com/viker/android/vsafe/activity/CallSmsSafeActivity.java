package com.viker.android.vsafe.activity;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.viker.android.vsafe.R;
import com.viker.android.vsafe.db.BlackNumberDao;
import com.viker.android.vsafe.model.BlackNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viker on 2016/5/29.
 * 通信卫士模块 主要是关于黑名单列表
 */
public class CallSmsSafeActivity extends AppCompatActivity
        implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "CallSmsSafeActivity";

    //数据库访问基类对象,用于对数据库的CRUD操作
    private BlackNumberDao dao;

    //黑名单列表
    private List<BlackNumber> blackNumberList;

    //通信卫士界面控件
    private RelativeLayout rlLoading;
    private ListView lvBlackNumber;
    private BlackNumberAdapter adapter;
    //对话框控件
    private AlertDialog dialog;
    //对话框内选择的拦截模式
    private String interruptMode;

    //添加黑名单对话框中的控件
    private EditText etAddBlackNumber;

    //修改黑名单对话框中的控件
    private EditText etUpdateBlackNumber;
    private BlackNumber selectedBlackNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callsmssafe);
        initToolbar(); //初始化工具栏

        blackNumberList = new ArrayList<>(); //创建黑名单列表
        dao = BlackNumberDao.getInstance(this); //获取DAO对象

        rlLoading = (RelativeLayout) findViewById(R.id.rl_callsmssafe_loading);
        lvBlackNumber = (ListView) findViewById(R.id.lv_callsmssafe_blacknumber);
        registerForContextMenu(lvBlackNumber);

        //考虑到一次性从数据库中获取全部黑名单记录是较耗时操作，因此采用AsyncTask来进行
        new BlackNumberTask().execute();
    }

    /*初始化工具栏*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_callsmssafe);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    /*创建工具栏中的选项菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_callsmssafe, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*选项菜单点击事件的处理*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.add_black_number:
                showAddBlackNumberDialog(); //显示添加黑名单记录对话框
                break;
            case R.id.delete_all_black_number:

                break;
            case R.id.setting:
                Toast.makeText(CallSmsSafeActivity.this, "you click setting in CallSmsSafe",
                        Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*创建上下文菜单*/
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_callsmssafe_context, menu);
    }

    /*上下文菜单点击事件的处理*/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //获取到Item对应的对象
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)
                item.getMenuInfo();
        //当前上下文菜单所对应的是ListView中的哪一项的位置
        int position = (int) info.id;
        switch (item.getItemId()) {
            //[删除]菜单
            case R.id.delete_black_number:
                deleteBlackNumber(position);
                return true;
            //[修改]菜单
            case R.id.update_blacknumber:
                showUpdateBlackNumber(position);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /*删除选中的黑名单记录*/
    private void deleteBlackNumber(int position) {
        BlackNumber blackNumber = adapter.getItem(position);
        dao.delete(blackNumber);
        blackNumberList.remove(blackNumber);
        adapter.notifyDataSetChanged();
    }

    /*[修改黑名单记录]对话框 修改选中的黑名单记录*/
    private void showUpdateBlackNumber(int position) {
        //获取选中的黑名单记录对象
        selectedBlackNumber = adapter.getItem(position);
        //创建修改黑名单记录对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this,
                R.layout.dialog_callsmssafe_updateblacknumber, null);
        etUpdateBlackNumber = (EditText) dialogView.findViewById(
                R.id.et_callsmssafe_updatedialog_blacknumber);
        RadioGroup rgSavedMode = (RadioGroup) dialogView.findViewById(
                R.id.rg_callsmssafe_updatedialog);
        RadioButton rbCall = (RadioButton) dialogView.findViewById(
                R.id.rb_callsmssafe_updatedialog_call);
        RadioButton rbSms = (RadioButton) dialogView.findViewById(
                R.id.rb_callsmssafe_updatedialog_sms);
        RadioButton rbAll = (RadioButton) dialogView.findViewById(
                R.id.rb_callsmssafe_updatedialog_all);
        Button btnUpdateOk = (Button) dialogView.findViewById(
                R.id.btn_callsmssafe_updatedialog_ok);
        Button btnUpdateCancel = (Button) dialogView.findViewById(
                R.id.btn_callsmssafe_updatedialog_cancel);
        //将该黑名单记录的内容回显在修改对话框中
        etUpdateBlackNumber.setText(selectedBlackNumber.getNumber());
        String savedMode = selectedBlackNumber.getMode();
        if (savedMode.equals("电话拦截")) {
            rbCall.setChecked(true);
        } else if (savedMode.equals("短信拦截")) {
            rbSms.setChecked(true);
        } else {
            rbAll.setChecked(true);
        }
        //设置监听器
        rgSavedMode.setOnCheckedChangeListener(this);
        btnUpdateOk.setOnClickListener(this);
        btnUpdateCancel.setOnClickListener(this);
        builder.setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }

    /*[添加黑名单记录]对话框*/
    private void showAddBlackNumberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this,
                R.layout.dialog_callsmssafe_addblacknumber, null);
        //引用对话框控件
        etAddBlackNumber = (EditText) dialogView.findViewById(
                R.id.et_callsmssafe_adddialog_blacknumber);
        RadioGroup rgMode = (RadioGroup) dialogView.findViewById(
                R.id.rg_callsmssafe_adddialog);
        Button btnAddOK = (Button) dialogView.findViewById(
                R.id.btn_callsmssafe_adddialog_ok);
        Button btnAddCancel = (Button) dialogView.findViewById(
                R.id.btn_callsmssafe_adddialog_cancel);
        rgMode.setOnCheckedChangeListener(this);
        btnAddOK.setOnClickListener(this);
        btnAddCancel.setOnClickListener(this);
        builder.setView(dialogView);
        dialog = builder.create();
        dialog.show();
    }

    /*对话框中的RadioGroup中勾选状态改变的处理*/
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            //1.[添加对话框]中的RadioButton
            //2.[修改对话框]中的RadioButton
            //电话拦截模式
            case R.id.rb_callsmssafe_adddialog_call:
            case R.id.rb_callsmssafe_updatedialog_call:
                interruptMode = "电话拦截";
                break;
            //短信拦截模式
            case R.id.rb_callsmssafe_adddialog_sms:
            case R.id.rb_callsmssafe_updatedialog_sms:
                interruptMode = "短信拦截";
                break;
            //全部拦截模式
            case R.id.rb_callsmssafe_adddialog_all:
            case R.id.rb_callsmssafe_updatedialog_all:
                interruptMode = "全部拦截";
                break;
            default:
                break;
        }
    }

    /*对话框中的按键的点击事件的处理*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //[添加黑名单]对话框的确定按键
            case R.id.btn_callsmssafe_adddialog_ok:
                //获取用户输入的黑名单号码
                String number = etAddBlackNumber.getText().toString();
                //获取拦截模式
                String mode = interruptMode;
                BlackNumber blackNumber = new BlackNumber();
                blackNumber.setNumber(number);
                blackNumber.setMode(mode);
                //查询黑名单列表中是否已经存在该黑名单记录
                BlackNumber savedBlackNumber = dao.query(blackNumber);
                if (TextUtils.isEmpty(number) || TextUtils.isEmpty(mode)) {
                    Toast.makeText(this, "号码或拦截模式不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (savedBlackNumber.equals(blackNumber)) {
                    Toast.makeText(this, "该号码已存在黑名单中",
                            Toast.LENGTH_SHORT).show();
                } else if (number.equals(dao.queryNumber(number))) {
                    //如果黑名单记录中不存在一样的黑名单记录但存在一样的黑名单号码，则提示用户
                    Toast.makeText(this, "黑名单列表中已存在相同的黑名单（拦截模式不同）",
                            Toast.LENGTH_SHORT).show();
                } else {
                    //如果黑名单列表中不存在该记录，则新建一个黑名单记录将该数据保存
                    dao.insert(blackNumber); //保存到数据库中
                    blackNumberList.add(blackNumber);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                break;
            //[添加黑名单]对话框的取消按键
            case R.id.btn_callsmssafe_adddialog_cancel:
                dialog.dismiss();
                break;

            //[修改黑名单]对话框的确定按键
            case R.id.btn_callsmssafe_updatedialog_ok:
                //获取修改后的黑名单号码
                String updatedNumber = etUpdateBlackNumber.getText().toString();
                //获取修改后的拦截模式
                String updatedMode = interruptMode;
                BlackNumber newBlackNumber = new BlackNumber();
                newBlackNumber.setNumber(updatedNumber);
                newBlackNumber.setMode(updatedMode);
                //查找数据库中是否已经包含了该号码
                BlackNumber savedBN = dao.query(newBlackNumber);
                if (TextUtils.isEmpty(updatedNumber) ||
                        TextUtils.isEmpty(updatedMode)) {
                    Toast.makeText(this, "号码或拦截模式不能为空",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //如果数据库中已有修改后的黑名单记录，则提示已有。否则将修改后的黑名单记录覆盖之前的。
                if (savedBN.equals(newBlackNumber)) {
                    Toast.makeText(this, "该号码已存在黑名单中",
                            Toast.LENGTH_SHORT).show();
                } else {
                    dao.update(newBlackNumber, selectedBlackNumber);
                    blackNumberList.remove(selectedBlackNumber);
                    blackNumberList.add(newBlackNumber);
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
                break;
            //[修改黑名单]对话框的确定按键
            case R.id.btn_callsmssafe_updatedialog_cancel:
                dialog.dismiss();
                break;
            default:
                break;
        }
    }


    /*自定义BlackNumberTask继承自AsyncTask类，用于开启子线程获取数据库中的全部黑名单记录*/
    private class BlackNumberTask extends AsyncTask<Void, Void, List<BlackNumber>> {

        @Override
        protected List<BlackNumber> doInBackground(Void... voids) {
            return dao.queryAll();
        }

        @Override
        protected void onPostExecute(List<BlackNumber> numberList) {
            super.onPostExecute(numberList);
            //当加载完数据后取消显示ProgressBar和提醒文本
            rlLoading.setVisibility(View.GONE);
            //如果黑名单列表不为空，则在ListView中显示
            if (numberList != null) {
                blackNumberList = numberList;
                adapter = new BlackNumberAdapter(blackNumberList);
                lvBlackNumber.setAdapter(adapter);
            } else {
                Toast.makeText(CallSmsSafeActivity.this, "黑名单列表为空",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*自定义适配器继承自ArrayAdapter，用于显示黑名单列表*/
    private class BlackNumberAdapter extends ArrayAdapter<BlackNumber> {

        public BlackNumberAdapter(List<BlackNumber> blackNumberList) {
            super(CallSmsSafeActivity.this, 0, blackNumberList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            ViewHolder holder;
            //当convertView中缓存了View对象时，就直接使用。否则就创建新视图
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.listview_callsmssafe_item, null);
                holder = new ViewHolder();
                holder.tvBlackNumber = (TextView) view.findViewById(
                        R.id.tv_callsmssafe_item_blacknumber);
                holder.tvMode = (TextView) view.findViewById(
                        R.id.tv_callsmssafe_item_mode);
                view.setTag(holder); //把控件id的引用都存放在view对象中
            } else {
                view = convertView;
                holder = (ViewHolder) view.getTag();
            }
            //获取该加载项的模型对象
            BlackNumber blackNumber = getItem(position);
            //显示模型对象的数据
            holder.tvBlackNumber.setText(blackNumber.getNumber());
            holder.tvMode.setText(blackNumber.getMode());

            return view;
        }
    }


    /*将Item中的控件使用static修饰，被static修饰的类的字节码在虚拟机中只会存在一份，
    * 其中的变量在栈中也只会存在一份，如此，可以避免每次显示一项Item都重新引用一次控件*/
    private class ViewHolder {
        TextView tvBlackNumber;
        TextView tvMode;
    }


}









