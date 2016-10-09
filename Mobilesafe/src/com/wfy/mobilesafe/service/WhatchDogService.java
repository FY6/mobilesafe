package com.wfy.mobilesafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import com.wfy.mobilesafe.activity.PassWordActivity;
import com.wfy.mobilesafe.db.dao.AppLocksDao;

import java.util.ArrayList;
import java.util.List;

/**
 * 程序狗服务
 */
public class WhatchDogService extends Service {

    //标志已经对密码校验通过了就不能再校验了，如服务重新初始化
    public static List<String> runFlag = new ArrayList<>();

    private boolean isDogRunning = true;
    private AppLocksDao dao;
    private ActivityManager am;

    public WhatchDogService() {
    }

    @Override
    public void onCreate() {
        System.out.println("关门狗服务开启");
        dao = new AppLocksDao(this);
        new Thread() {
            @Override
            public void run() {
                while (isDogRunning) {
                    am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    ActivityManager.RunningTaskInfo runningTaskInfo = am.getRunningTasks(1).get(0);
                    ComponentName topActivity = runningTaskInfo.topActivity;
                    String packageName = topActivity.getPackageName();


                    //System.out.println(packageName + "----");
                    //只要返回桌面的清空runFlag集合
                    if (!WhatchDogService.runFlag.isEmpty() && "com.android.launcher".equals(packageName)) {
                        WhatchDogService.runFlag.clear();
                    }

                    if (dao.find(packageName) && !WhatchDogService.runFlag.contains(packageName)) {
                        WhatchDogService.runFlag.clear();
                        WhatchDogService.runFlag.add(packageName);
                        //开启密码界面activity
                        System.out.println(packageName);
                        Intent intent = new Intent(WhatchDogService.this, PassWordActivity.class);
                        intent.putExtra("packageName", packageName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    SystemClock.sleep(1000);
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("关门狗服务关闭");
        isDogRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
