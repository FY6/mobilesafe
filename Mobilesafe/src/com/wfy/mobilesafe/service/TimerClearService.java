package com.wfy.mobilesafe.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.wfy.mobilesafe.utils.TaskInfoParser;

import java.util.Timer;
import java.util.TimerTask;

public class TimerClearService extends Service {

    private Timer timer;

    public TimerClearService() {
    }

    @Override
    public void onCreate() {
        killProcess(0);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // System.out.println(intent.getIntExtra("time", 0));
        long time = intent.getLongExtra("time", 1);
        killProcess(time);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println(intent.getIntExtra("time", 0));
        return null;
    }

    public void killProcess(final long time) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("--------");
                TaskInfoParser.killAll(TimerClearService.this);
            }
        }, 0, 24 * 1000 * 60 * 60);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
