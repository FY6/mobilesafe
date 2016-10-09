package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.service.AutoKillService;
import com.wfy.mobilesafe.service.TimerClearService;
import com.wfy.mobilesafe.utils.ServiceStatusUtils;

/**
 * 进程管理设置页面
 */
public class TaskManagerSettingActivity extends Activity {

    private CheckBox cb_show_sysProcess;
    private SharedPreferences mPref;
    private boolean show_sys;
    private CheckBox cb_lockScreen_switch;
    private CheckBox cb_time_killProcess;
    private TextView tv_killTime_setting;
    private boolean isTime;//判断是否已经设置时间间隔

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager_setting);
        mPref = getSharedPreferences("config", MODE_PRIVATE);
        cb_show_sysProcess = (CheckBox) findViewById(R.id.cb_show_sysProcess);//是否显示系统进程
        cb_lockScreen_switch = (CheckBox) findViewById(R.id.cb_lockScreen_switch);//锁屏清理
        cb_time_killProcess = (CheckBox) findViewById(R.id.cb_time_killProcess);//定时清理
        tv_killTime_setting = (TextView) findViewById(R.id.tv_killTime_setting);//定时清理时间间隔设置

        show_sys = mPref.getBoolean("show_sys", true);
        cb_show_sysProcess.setChecked(show_sys);

        cb_show_sysProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cb_show_sysProcess.setText("当前状态: 显示系统进程");
                    //mPref.edit().putBoolean("show_sys", isChecked).commit();
                } else {

                    cb_show_sysProcess.setText("当前状态: 不显示系统进程");
                    //mPref.edit().putBoolean("show_sys", isChecked).commit();
                }
                mPref.edit().putBoolean("show_sys", isChecked).commit();
            }
        });


        //判断服务是否在运行
        if (ServiceStatusUtils.isServiceRunning(this, "com.wfy.mobilesafe.service.AutoKillService")) {
            cb_lockScreen_switch.setChecked(true);
            cb_lockScreen_switch.setText("当前状态: 锁屏清理已开启");
        } else {
            cb_lockScreen_switch.setChecked(false);
            cb_lockScreen_switch.setText("当前状态: 锁屏清理已关闭");
        }

        cb_lockScreen_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //  cb_lockScreen_switch.setChecked(isChecked);
                    startService(new Intent(TaskManagerSettingActivity.this, AutoKillService.class));
                    cb_lockScreen_switch.setText("当前状态: 锁屏清理已开启");
                } else {
                    // cb_lockScreen_switch.setChecked(isChecked);
                    stopService(new Intent(TaskManagerSettingActivity.this, AutoKillService.class));
                    cb_lockScreen_switch.setText("当前状态: 锁屏清理已关闭");
                }
                cb_lockScreen_switch.setChecked(isChecked);
            }
        });

        //判断定时清理服务是否在运行
        if (ServiceStatusUtils.isServiceRunning(this, "com.wfy.mobilesafe.service.TimerClearService")) {
            cb_time_killProcess.setChecked(true);
            tv_killTime_setting.setEnabled(true);
            tv_killTime_setting.setBackgroundColor(Color.WHITE);
            tv_killTime_setting.setTextColor(Color.BLACK);
        } else {
            cb_time_killProcess.setChecked(false);
            tv_killTime_setting.setEnabled(false);
            tv_killTime_setting.setBackgroundColor(Color.DKGRAY);
            tv_killTime_setting.setBackgroundColor(Color.GRAY);
        }

        //定时清理
        cb_time_killProcess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tv_killTime_setting.setEnabled(true);
                    tv_killTime_setting.setBackgroundColor(Color.WHITE);
                    tv_killTime_setting.setTextColor(Color.BLACK);
                } else {
                    stopService(new Intent(TaskManagerSettingActivity.this, TimerClearService.class));
                    tv_killTime_setting.setEnabled(false);
                    tv_killTime_setting.setBackgroundColor(Color.DKGRAY);
                    tv_killTime_setting.setTextColor(Color.GRAY);
                }
            }
        });
        tv_killTime_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskManagerSettingActivity.this);
                builder.setTitle("设置清理时间间隔");
                builder.setSingleChoiceItems(new String[]{"两小时", "四小时", "六小时"},
                        mPref.getInt("id", 0), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long time = 1;
                                int id = -1;
                                isTime = true;
                                switch (which) {
                                    case 0:
                                        time = 2;
                                        id = which;
                                        break;
                                    case 1:
                                        time = 4;
                                        id = which;
                                        break;
                                    case 2:
                                        time = 6;
                                        id = which;
                                        break;
                                }
                                mPref.edit().putInt("id", which).commit();
                                Intent intent = new Intent(TaskManagerSettingActivity.this, TimerClearService.class);
                                intent.putExtra("time", time);
                                startService(intent);
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isTime = false;
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    //返回
    public void save(View v) {
        if (cb_time_killProcess.isChecked() && !isTime) {
            Toast.makeText(TaskManagerSettingActivity.this, "未设置时间间隔", Toast.LENGTH_SHORT).show();
            return;
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (cb_time_killProcess.isChecked() && !isTime) {
            Toast.makeText(TaskManagerSettingActivity.this, "未设置时间间隔", Toast.LENGTH_SHORT).show();
            return;
        }
        finish();
    }
}
