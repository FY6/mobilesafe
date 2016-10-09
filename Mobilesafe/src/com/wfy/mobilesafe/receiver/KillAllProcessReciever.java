package com.wfy.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wfy.mobilesafe.utils.TaskInfoParser;

/**
 * Created by wfy on 2016/8/1.
 */
public class KillAllProcessReciever extends BroadcastReceiver {
    public KillAllProcessReciever() {
        super();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        TaskInfoParser.killAll(context);
    }
}
