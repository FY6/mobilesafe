package com.wfy.mobilesafe.receiver;

import com.wfy.mobilesafe.db.dao.AddressDao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * 去电广播接收者，，，，注意区别：来电启动service监听来电状态，，去电使用广播接受者
 * 需要權限：android.permission.PROCESS_OUTGOING_CALLS action: <action
 * android:name="android.intent.action.NEW_OUTGOING_CALL"/>
 * 
 * @author wfy
 * 
 */
public class OutCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String number = getResultData();// 获取电话号码
		String address = AddressDao.getAddress(number);
		Toast.makeText(context, address, Toast.LENGTH_LONG).show();
	}

}
