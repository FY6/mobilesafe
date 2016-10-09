package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.service.AddressService;
import com.wfy.mobilesafe.service.CallSafeService;
import com.wfy.mobilesafe.service.WhatchDogService;
import com.wfy.mobilesafe.utils.ServiceStatusUtils;
import com.wfy.mobilesafe.view.SettingClickView;
import com.wfy.mobilesafe.view.SettingItemView;

/**
 * 设置中心
 * 自定义属性：参考android自带的D:\adt-bundle-windows\adt-bundle-windows-x86_64-20140624
 * \sdk\platforms\android-18\data\res\values
 *
 * @author wfy
 */
public class SettingActivity extends Activity {
    private SettingItemView sivUpdate;
    // private TextView tvDesc;
    // private TextView tvTitle;
    private SharedPreferences mPref;
    private SettingItemView sivAddress;
    private SettingClickView sivAddressStyle;
    private SettingItemView sivBlackNumber;
    private SettingItemView sivWhatchDog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mPref = getSharedPreferences("config", MODE_PRIVATE);

        initUpdateView();
        // 归属地
        initAdderssView();
        initAddressStyleView();
        initAddressLocation();
        initBlackNumberView();

        //看门狗
        initWhatchDog();
    }
    //看门狗
    private void initWhatchDog() {
        sivWhatchDog = (SettingItemView) findViewById(R.id.siv_whatch_dog);

        boolean isRunning = ServiceStatusUtils.isServiceRunning(this,
                "com.wfy.mobilesafe.service.WhatchDogService");
        if (isRunning) {
            sivWhatchDog.setStatus(true);
        } else {
            sivWhatchDog.setStatus(false);
        }
        sivWhatchDog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivWhatchDog.isChecked()) {
                    sivWhatchDog.setStatus(false);
                    //停止服务
                    stopService(new Intent(SettingActivity.this,
                            WhatchDogService.class));
                } else {
                    sivWhatchDog.setStatus(true);
                    //开启服务
                    startService(new Intent(SettingActivity.this,
                            WhatchDogService.class));
                }
            }
        });
    }

    /**
     * 初始化升级更新开关
     */
    private void initUpdateView() {
        sivUpdate = (SettingItemView) findViewById(R.id.siv_update);
        // sivUpdate.setTitle("设置自动更新");

        boolean autoUpdate = mPref.getBoolean("auto_update", true);
        // if (autoUpdate) {
        // //sivUpdate.setDesc("设置自动更新 已开启");
        // sivUpdate.setStatus(autoUpdate);
        // } else {
        // //sivUpdate.setDesc("设置自动更新 已关闭");
        // sivUpdate.setStatus(autoUpdate);
        // }
        sivUpdate.setStatus(autoUpdate);
        sivUpdate.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (sivUpdate.isChecked()) {
                    // sivUpdate.setDesc("设置自动更新 已关闭 ");
                    sivUpdate.setStatus(false);

                    mPref.edit().putBoolean("auto_update", false).commit();
                } else {
                    // sivUpdate.setDesc("设置自动更新 已开启");
                    sivUpdate.setStatus(true);
                    mPref.edit().putBoolean("auto_update", true).commit();
                }
            }
        });
    }

    /**
     * 初始化归属地开关
     */
    private void initAdderssView() {

        sivAddress = (SettingItemView) findViewById(R.id.siv_address);
        // 只有当服务正在运行时，才能监听来电状态,,,根据服务是否正在运行，更新checkbox状态
        boolean serviceRunning = ServiceStatusUtils.isServiceRunning(this,
                "com.wfy.mobilesafe.service.AddressService");
        if (serviceRunning) {
            sivAddress.setStatus(true);
        } else {
            sivAddress.setStatus(false);
        }

        sivAddress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sivAddress.isChecked()) {
                    sivAddress.setStatus(false);
                    // 停止服务
                    stopService(new Intent(SettingActivity.this,
                            AddressService.class));
                } else {
                    sivAddress.setStatus(true);
                    // 开启服务
                    startService(new Intent(SettingActivity.this,
                            AddressService.class));
                }
            }
        });
    }

    // 单选框的选项
    String[] items = new String[]{"半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿"};
    private SettingClickView sivAddressLocation;

    // 初始化提示框风格
    private void initAddressStyleView() {
        sivAddressStyle = (SettingClickView) findViewById(R.id.siv_address_style);
        sivAddressStyle.setTitle("归属地提示框风格");

        final int style = mPref.getInt("address_style", 0);
        sivAddressStyle.setDesc(items[style]);

        sivAddressStyle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        SettingActivity.this);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setTitle("提示框风格");

                builder.setSingleChoiceItems(items, style,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mPref.edit().putInt("address_style", which)
                                        .commit();
                                dialog.dismiss();
                                // 更新提示框描述
                                sivAddressStyle.setDesc(items[which]);
                            }
                        });
                builder.setNegativeButton("取消", null);
                builder.show();
            }
        });
    }

    // 初始化归属地提示框显示位置
    private void initAddressLocation() {
        sivAddressLocation = (SettingClickView) findViewById(R.id.siv_address_location);
        sivAddressLocation.setTitle("归属地提示框位置");
        sivAddressLocation.setDesc("设置归属地提示框的显示位置");

        sivAddressLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转至拖拽activity
                startActivity(new Intent(SettingActivity.this,
                        DragViewActivity.class));
            }
        });
    }

    /**
     * 初始化黑名单view
     */
    private void initBlackNumberView() {
        sivBlackNumber = (SettingItemView) findViewById(R.id.siv_black_number);

        boolean isRunning = ServiceStatusUtils.isServiceRunning(this,
                "com.wfy.mobilesafe.service.CallSafeService");
        // 只有当服务正在运行时，才能监听来电状态,,,根据服务是否正在运行，更新checkbox状态
        if (isRunning) {
            sivBlackNumber.setStatus(true);
        } else {
            sivBlackNumber.setStatus(false);
        }
        sivBlackNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivBlackNumber.isChecked()) {
                    sivBlackNumber.setStatus(false);
                    //停止服务
                    stopService(new Intent(SettingActivity.this,
                            CallSafeService.class));
                } else {
                    sivBlackNumber.setStatus(true);
                    //开启服务
                    startService(new Intent(SettingActivity.this,
                            CallSafeService.class));
                }
            }
        });
    }
}
