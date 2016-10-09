package com.wfy.mobilesafe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.receiver.MyAppWidgetProvider;
import com.wfy.mobilesafe.utils.TaskInfoParser;

import java.util.Timer;
import java.util.TimerTask;

public class AutoCleaService extends Service {

    private Timer timer;//定时器
    private AppWidgetManager appWigetManager;//widget管理者
    private TimerTask timerTask;

    public AutoCleaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        appWigetManager = AppWidgetManager.getInstance(this);
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                //远程views
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.process_widget);
                remoteViews.setTextViewText(R.id.process_count, "正在运行的进程：" +
                        TaskInfoParser.getRunningAppProcCount(AutoCleaService.this) + " 个");
                remoteViews.setTextViewText(R.id.process_memory, "可用内存：" +
                        Formatter.formatFileSize(AutoCleaService.this, TaskInfoParser.getAvailMem(AutoCleaService.this)));

                //给按钮设置点击事件，这里需要发广播
                Intent intent = new Intent();
                intent.setAction("com.wfy.mobilesafe");
                //当点击一键清理就会发送广播，让广播接受者清理（即KillAllProcessReciever广播接受者者清理操作）
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);//拿到延迟意图
                remoteViews.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);

                //MyAppWidgetProvider.class： 用哪个AppWidgetProvider处理appwidget
                ComponentName provider = new ComponentName(getApplication(), MyAppWidgetProvider.class);
                appWigetManager.updateAppWidget(provider, remoteViews);//更新appwidget
            }
        };

        new Thread() {
            @Override
            public void run() {
                timer.schedule(timerTask, 0, 3000);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}
