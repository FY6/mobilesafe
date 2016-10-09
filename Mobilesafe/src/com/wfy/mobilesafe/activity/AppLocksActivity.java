package com.wfy.mobilesafe.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.fragments.LocksFragment;
import com.wfy.mobilesafe.fragments.UnLocksFragment;
import com.wfy.mobilesafe.utils.MD5Utils;

/**
 * 程序锁：
 * 需要注意：
 * 以为项目中已经加入了v4包，不能再加入重复v4包会报错
 */
public class AppLocksActivity extends FragmentActivity {

    private TextView unLocoks;
    private TextView mLocks;
    private FrameLayout nContent;
    private FragmentManager fragmentManager;//Fragment管理器
    private UnLocksFragment unLocksFragment;//未加锁
    private LocksFragment locksFragment;//已加锁
    private boolean isFragmentOK;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_locks);
        mPref = getSharedPreferences("config", MODE_PRIVATE);
        initUI();
    }

    public void resetPass(View v) {
        showSettingPass();
    }

    //设置程序锁密码
    private void showSettingPass() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                    Toast.makeText(AppLocksActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
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
     * 初始化UI
     */
    private void initUI() {
        unLocoks = (TextView) findViewById(R.id.tv_unlocks);
        mLocks = (TextView) findViewById(R.id.tv_locks);
        nContent = (FrameLayout) findViewById(R.id.fl_content);

        //初始化默认Fragment
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction mTransaction = fragmentManager.beginTransaction();
        unLocksFragment = new UnLocksFragment();
        locksFragment = new LocksFragment();

        //调用此方法，会调用onDestriyView方法 ，销毁当前Fragment的布局，
        // 如果fragment对象还没有销毁，那么下一次调用就会调用onCreateView() 创建fragment的布局
        mTransaction.replace(R.id.fl_content, unLocksFragment).commit();
        isFragmentOK = true;
    }

    public void click(View v) {
        isFragmentOK = false;
        final FragmentTransaction mTransaction = fragmentManager.beginTransaction();
        //unLocksFragment.backState(false);
        switch (v.getId()) {
            case R.id.tv_unlocks:
                unLocoks.setBackgroundResource(R.drawable.tab_left_pressed);
                mLocks.setBackgroundResource(R.drawable.tab_right_default);
                /**
                 * 现这个bug的原因是我将touch事件和onKey事件传递给了Fragment，
                 * 而这个时候Fragment还在动画状态中，“No host”，没有附着到Activity上面。
                 * j解決方法：等fragment動畫結束
                 */
                //设置Fragment动画
                mTransaction.setCustomAnimations(R.anim.trans_previous_in, R.anim.trans_previous_out);
                mTransaction.replace(R.id.fl_content, unLocksFragment).commit();

                new Thread() {
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        isFragmentOK = true;
                    }
                }.start();

                break;
            case R.id.tv_locks:
                unLocoks.setBackgroundResource(R.drawable.tab_left_default);
                mLocks.setBackgroundResource(R.drawable.tab_right_pressed);

                //设置Fragment动画
                mTransaction.setCustomAnimations(R.anim.trans_next_in, R.anim.trans_next_out);
                mTransaction.replace(R.id.fl_content, locksFragment).commit();

                new Thread() {
                    @Override
                    public void run() {
                        SystemClock.sleep(1000);
                        isFragmentOK = true;
                    }
                }.start();
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isFragmentOK) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!isFragmentOK) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
