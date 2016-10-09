package com.wfy.mobilesafe.receiver;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;

import com.wfy.mobilesafe.service.AutoCleaService;

/**
 * App widget 广播接受者
 * Created by wfy on 2016/7/29.
 */
public class MyAppWidgetProvider extends android.appwidget.AppWidgetProvider {
    public MyAppWidgetProvider() {
        super();
    }

    /**
     * 每一次调用每一个生命周期方法都会调用此方法
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        System.out.println("onReceive");
    }

    /**
     * 每次有桌面小控件生成都会调用
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        System.out.println("onUpdate");
    }

    /**
     * 每次删除一个桌面小控件都会调用一次
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        System.out.println("onDeleted");
    }

    /**
     * 当第一次创建小控件时调用
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Intent intent = new Intent(context, AutoCleaService.class);
        context.startService(intent);
    }

    /**
     * 当桌面所有小控件都删除了，调用
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Intent intent = new Intent(context, AutoCleaService.class);
        context.stopService(intent);
    }
}
