package com.wfy.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 监听手机开机广播
 * 
 * @author wfy
 * 
 */
public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences sp = context.getSharedPreferences("config",
				Context.MODE_PRIVATE);
		Boolean protect = sp.getBoolean("protect", false);
		// 只有防盗保护的前提下，才进行sim卡判断
		if (protect) {
			String sim = sp.getString("sim", null);

			//
			if (!TextUtils.isEmpty(sim)) {
				// 拿到系统服务
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				String currentSim = tm.getSimSerialNumber();

				if (currentSim.equals(sim)) {
					System.out.println("手机安全");
				} else {
					System.out.println("sim卡发生变化，发送报警短信！");

					// 拿到安全号码
					String safePhone = sp.getString("safe_phone", "");

					// 发送短信
					SmsManager smsManager = SmsManager.getDefault();
					smsManager.sendTextMessage(safePhone, null,
							"sim card is changed！", null, null);
				}

			}

		}
	}

}
