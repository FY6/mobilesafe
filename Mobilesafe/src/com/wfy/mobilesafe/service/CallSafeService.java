package com.wfy.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.ITelephony;
import com.wfy.mobilesafe.db.dao.BlackNumberDao;

import java.lang.reflect.Method;

/**
 * 黑名单拦截服务
 */
public class CallSafeService extends Service {

    //拦截短信，广播接受者，来电使用服务监听拦截
    private BlackNumberReceiver receiver;
    private IntentFilter intentFilter;
    private BlackNumberDao blackNumberDao;
    private TelephonyManager tm;
    private MyPhoneStateListener listener;

    public CallSafeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        blackNumberDao = new BlackNumberDao(this);
        // 动态注册广播接受者
        receiver = new BlackNumberReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(receiver, intentFilter);

        //拿到电话管理器
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //监听
        listener = new MyPhoneStateListener();
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        //当服务被停止，也取消监听来电在onDestroy方法中
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE://闲置
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://摘机
                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃
                    String mode = blackNumberDao.findBlackNumber(incomingNumber);
                    System.out.println(mode);
                    if ("1".equals(mode) || "3".equals(mode)) {
                        //如果单单使用内容解析者去删除，会出现删除有时成功，有时不成功的情况，因为立刻把电话挂断了，
                        // 但呼叫的生成并不是同步的代码；它是一个异步的代码
                        // 解决方案用观察者去监听来电记录产生后再去删除
                        Uri uri = Uri.parse("content://call_log/calls");
                        MyContentObServer myContentObServer = new MyContentObServer(new Handler(), incomingNumber);
                        getContentResolver().registerContentObserver(uri, true, myContentObServer);
                        //挂断电话
                        endCall();
                    }
                    break;
            }
        }
    }

    //删除来电记录，内容观察者
    private class MyContentObServer extends ContentObserver {
        private String incomingNumber;

        public MyContentObServer(Handler handler, String incomingNumber) {
            super(handler);
            this.incomingNumber = incomingNumber;
        }

        @Override
        public void onChange(boolean selfChange) {
            Uri uri = Uri.parse("content://call_log/calls");
            getContentResolver().delete(uri, "number = ?", new String[]{incomingNumber});
            //取消注册
            getContentResolver().unregisterContentObserver(this);
        }
    }

    /**
     * 要调用系统服务的endCall方法挂掉电话，我们就要拿到ITelephony的对象，
     * 首先通过ServiceManager.getService(Context.TELEPHONY_SERVICE)静态方法返回IBinder（中间人对象），
     * 其中中间人对象实现一个aidl接口和IBinder
     * 通过中间人对象可以调用系统服务中的方法，
     * 这是问题是如何拿到中间人对象
     * ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
     * 一定要加权限<uses-permission android:name="android.permission.CALL_PHONE" />
     */
    private void endCall() {

        try {
            Class<?> clazz = getClassLoader().loadClass("android.os.ServiceManager");
            Method method = clazz.getDeclaredMethod("getService", String.class);
            IBinder mBinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
            ITelephony iTelephony = ITelephony.Stub.asInterface(mBinder);
            iTelephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mode:
     * 1: 电话拦截
     * 2: 短信拦截
     * 3: 电话拦截+短信拦截
     */
    //ctrl+O
    //黑名单接收者
    private class BlackNumberReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收SMS是以pdu传送
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");

            for (Object pdu : pdus) {
                // 通过pdu获取每一条短信内容
                SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                // 拿到电话号码
                String originatingAddress = message.getOriginatingAddress();
                // 拿到短信内容
                String messageBody = message.getMessageBody();

                //通过电话号码在数据库中查找对应的拦截模型mode，如果返回2,3则需要拦截短信
                String mode = blackNumberDao.findBlackNumber(originatingAddress);
                if (!TextUtils.isEmpty(mode)) {
                    if (mode.equals("2") || mode.equals("3")) {
                        //终止短信传播
                        abortBroadcast();
                    }
                }

                //智能拦截,,,分词
                if ("zhifubao".equals(messageBody)) {
                    abortBroadcast();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        //取消来电监听
        if (listener != null) {
            tm.listen(listener, PhoneStateListener.LISTEN_NONE);
            listener = null;
        }
    }
}
