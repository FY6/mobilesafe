package com.wfy.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.service.LocationService;

/**
 * 接收短信广播，拦截短信
 * 
 * @author wfy
 * 
 */
public class SmsReceiver extends BroadcastReceiver {

	private DevicePolicyManager mDPM;
	private ComponentName mDeviceAdminSample;

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

			if ("#*alarm*#".equals(messageBody)) {
				// 播放报警音乐, 即使手机调为静音,也能播放音乐, 因为使用的是媒体声音的通道,和铃声无关
				MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);

				// 把音量调节到最大
				player.setVolume(1f, 1f);
				// 设置媒体播放是否循环
				player.setLooping(true);
				// 开始播放
				player.start();
				// 终止短信，中断短信的传递, 从而系统短信app就收不到内容了
				abortBroadcast();
			} else if ("#*location*#".equals(messageBody)) {// 获取经纬度

				// 开启定位服务
				context.startService(new Intent(context, LocationService.class));

				SharedPreferences sp = context.getSharedPreferences("config",
						context.MODE_PRIVATE);
				String location = sp.getString("location",
						"getting location...");
				System.out.println("location: " + location);
				// 终止短信，中断短信的传递, 从而系统短信app就收不到内容了
				abortBroadcast();
			} else if ("#*wipedata*#".equals(messageBody)) {// 远程删除数据

				// 拿到系统设备管理器
				mDPM = (DevicePolicyManager) context
						.getSystemService(Context.DEVICE_POLICY_SERVICE);
				// 设备管理组件
				mDeviceAdminSample = new ComponentName(context,
						DeviceAdmin.class);

				// 判断设备管理器是否激活
				if (mDPM.isAdminActive(mDeviceAdminSample)) {
					// 清楚数据，恢复出厂设置
					mDPM.wipeData(0);
				} else {
					Toast.makeText(context, "设备管理器没有激活，请激活设备管理器",
							Toast.LENGTH_SHORT).show();
				}

				abortBroadcast();
			} else if ("#*lockscreen*#".equals(messageBody)) {// 远程锁屏
				// 拿到系统设备管理器
				mDPM = (DevicePolicyManager) context
						.getSystemService(Context.DEVICE_POLICY_SERVICE);
				// 设备管理组件
				mDeviceAdminSample = new ComponentName(context,
						DeviceAdmin.class);

				// 判断设备管理器是否激活
				if (mDPM.isAdminActive(mDeviceAdminSample)) {
					// 立即锁屏
					mDPM.lockNow();
					// 重置密码
					mDPM.resetPassword("", 0);
				} else {
					Toast.makeText(context, "设备管理器没有激活，请激活设备管理器",
							Toast.LENGTH_SHORT).show();
				}

				abortBroadcast();
			}
		}

	}

}
