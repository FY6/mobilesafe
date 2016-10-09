package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.MD5Utils;
import com.wfy.mobilesafe.utils.SmsUtils;
import com.wfy.mobilesafe.utils.ToastUtils;

import net.youmi.android.listener.Interface_ActivityListener;
import net.youmi.android.offers.OffersManager;

/**
 * 在哪调用Looper对象就保存到那个线程中,也就是说handler想
 * <p/>
 * <p/>
 * 每一个activity 一创建对象，就已经创建好了handler和looper，所以runOnUiThread是activity的方法
 * 次方法的内部就是通过handler的post方法发消息去更新UI的，又因为主线程系统早就已经完成handler和looper
 * 的创建。
 * Handler handler = new Handler();
 * handler.sendMessage(new Message());
 * handler 发送消息，其实就把消息发送至Looper的消息队列中
 * <p/>
 * Looper:
 * 调用：Looper.prepare();
 * sThreadLocal.set(new Looper(quitAllowed));//把Looper对象保存至当前线程
 * Handler：
 * Looper.myLooper();//然后handler取出Looper对象的消息队列
 */

/**
 * 高级工具
 *
 * @author wfy
 */
public class AToolsActivity extends Activity {

    private ProgressDialog dialog;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atools);
        mPref = getSharedPreferences("config", MODE_PRIVATE);
    }

    /**
     * 软件推荐
     *
     * @param v
     */
    public void appRecomment(View v) {
        OffersManager.getInstance(this).showOffersWall(new Interface_ActivityListener() {

            /**
             * 当积分墙销毁的时候，即积分墙的Activity调用了onDestory的时候回调
             */
            @Override
            public void onActivityDestroy(Context context) {
                Toast.makeText(context, "全屏积分墙退出了", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 程序锁
     *
     * @param v
     */
    public void appLocks(View v) {
        showAppLock();
    }

    private void showAppLock() {
        if (TextUtils.isEmpty(mPref.getString("applocks_pass", null))) {
            showSettingPass();
        } else {
            showInputPass();
        }
    }

    //输入程序锁密码
    private void showInputPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        //填充布局
        View view = View.inflate(AToolsActivity.this, R.layout.dialog_password_applock, null);
        final EditText et_password = (EditText) view.findViewById(R.id.et_password);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText("请输入程序锁密码");

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = et_password.getText().toString().trim();
                String applocks_pass = mPref.getString("applocks_pass", null);
                if (!TextUtils.isEmpty(applocks_pass)) {
                    if (applocks_pass.equals(MD5Utils.encode(pass))) {
                        Intent intent = new Intent(AToolsActivity.this, AppLocksActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(AToolsActivity.this, "密码错误，重新输入", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 设置程序锁密码
     */
    private void showSettingPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.dialog_password_applock, null);
        final EditText et_password = (EditText) view.findViewById(R.id.et_password);
        Button btn_ok = (Button) view.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = et_password.getText().toString().trim();
                if (!TextUtils.isEmpty(pass)) {
                    pass = MD5Utils.encode(pass);
                    mPref.edit().putString("applocks_pass", pass).commit();
                    dialog.dismiss();
                } else {
                    Toast.makeText(AToolsActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }


    /**
     * 归属地查询
     *
     * @param v
     */
    public void numberAddressQuery(View v) {
        startActivity(new Intent(this, AddressActivity.class));
    }

    /**
     * 备份短信
     *
     * @param v 被点击组件的对象
     */
    public void smsBackup(View v) {
        dialog = new ProgressDialog(this);
        dialog.setTitle("短信备份提示框");
        dialog.setMessage("短信备份中，请稍等...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        new Thread() {
            @Override
            public void run() {
                boolean backup = SmsUtils.backup(AToolsActivity.this, new SmsUtils.SMSCallBack() {
                    @Override
                    public void before(int count) {
                        dialog.setMax(count);
                    }

                    @Override
                    public void onBackUp(int progress) {
                        dialog.setProgress(progress);
                    }
                });
                if (backup) {
                    ToastUtils.showToast(AToolsActivity.this, "短信备份成功");
                    dialog.dismiss();
                } else {
                    ToastUtils.showToast(AToolsActivity.this, "短信备份失败");
                    dialog.dismiss();
                }
            }
        }.start();

        dialog.show();
    }

    /**
     * 常用号码查询
     *
     * @param view
     */
    public void commonNuberQuery(View view) {
        startActivity(new Intent(this, CommonNumberActivity.class));
    }
}
