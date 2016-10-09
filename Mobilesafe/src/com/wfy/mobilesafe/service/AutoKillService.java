package com.wfy.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.wfy.mobilesafe.utils.TaskInfoParser;

/**
 * 锁屏自动清理服务
 */
public class AutoKillService extends Service {

    private InnerScreenOff mReciever;

    public AutoKillService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReciever = new InnerScreenOff();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        //注册锁屏广播接收者
        registerReceiver(mReciever, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReciever);
        mReciever = null;
    }

    class InnerScreenOff extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TaskInfoParser.killAll(context);
        }
    }
}
