package com.wfy.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.db.dao.AddressDao;

/**
 * 来电提醒服务 意区别：来电启动service监听来电状态，，去电使用广播接受者
 * 
 * @author wfy
 * 
 */
public class AddressService extends Service {

	private TelephonyManager tm;
	private MyListener listener;
	private OutCallReceiver receiver;
	private WindowManager mWM;
	private View view;
	private SharedPreferences mPref;

	private int startX;
	private int startY;
	private WindowManager.LayoutParams params;
	private int winWidth;
	private int winHeight;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 监听来电
		tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		listener = new MyListener();
		tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);// 监听打电话的状态

		receiver = new OutCallReceiver();
		IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
		registerReceiver(receiver, filter);// 动态注册广播接受者，监听去电

		mPref = getSharedPreferences("config", MODE_PRIVATE);
	}

	/**
	 * “去电”广播接收者，，，，注意区别：来电启动service监听来电状态，，去电使用广播接受者
	 * 需要權限：android.permission.PROCESS_OUTGOING_CALLS action: <action
	 * android:name="android.intent.action.NEW_OUTGOING_CALL"/>
	 * 
	 * @author wfy
	 * 
	 */
	class OutCallReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String number = getResultData();// 获取电话号码
			String address = AddressDao.getAddress(number);
			// Toast.makeText(context, address, Toast.LENGTH_LONG).show();
			showToast(address);// 浮窗显示归属地
		}

	}

	// 来电监听器
	class MyListener extends PhoneStateListener {

		/**
		 * state：电话状态 incomingNumber:来电号码
		 */
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:// 电话响铃
				System.out.println("电话响铃");
				// 查询归属地
				String address = AddressDao.getAddress(incomingNumber);// 查询来电号码归属地
				// Toast.makeText(getApplicationContext(), address,
				// Toast.LENGTH_LONG).show();
				showToast(address);// 浮窗显示归属地
				break;
			case TelephonyManager.CALL_STATE_IDLE:// 电话闲置
				// 将view移除windown,如果不移出winddown，则将一直在windown上
				if (mWM != null && view != null) {
					mWM.removeView(view);
					view = null;
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:// 电话摘机，接听
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		tm.listen(listener, PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(receiver);// 注销广播接受者
	}

	// 自定义浮窗显示归属地
	private void showToast(String address) {
		params = new WindowManager.LayoutParams();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;// 包裹内容
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;// 包裹内容
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_PHONE; // 电话窗口。它用于电话交互（特别是呼入）。它置于所有应用程序之上，状态栏之下。
		params.setTitle("Toast");
		// 去掉禁止触摸，，把TYPE_Toast换为TYPE_PHONE
		params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		params.gravity = Gravity.LEFT + Gravity.TOP;// 将重心位置甚至为左上方，也就是（0,0）从左上方开始，而不是默认的重心(居中)位置
		mWM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);// 获取系统windown服务

		// 获取屏幕宽高
		Display defaultDisplay = mWM.getDefaultDisplay();
		winWidth = defaultDisplay.getWidth();
		winHeight = defaultDisplay.getHeight();

		int x = mPref.getInt("lastX", 0);
		int y = mPref.getInt("lastY", 0);

		// 设置浮窗位置，基于左上方的偏移量
		params.x = x;
		params.y = y;

		// view = new TextView(this);
		// view.setBackgroundResource(R.drawable.call_locate_blue);
		// view.setText(address);
		// view.setTextColor(Color.RED);
		// view.setTextSize(20);
		// 提示框背景资源
		int[] dgs = new int[] { R.drawable.call_locate_white9,
				R.drawable.call_locate_orange9, R.drawable.call_locate_blue9,
				R.drawable.call_locate_gray9, R.drawable.call_locate_green9 };
		// 获取在StetingActivity保存的对应dgs数组的坐标
		int style = mPref.getInt("address_style", 0);
		// 填充view
		view = View.inflate(this, R.layout.toast_address, null);
		// 更新view的背景
		view.setBackgroundResource(dgs[style]);
		TextView tvNumber = (TextView) view.findViewById(R.id.tv_number);
		// 更新归属地
		tvNumber.setText(address);
		// 把view对象添加到windown上
		mWM.addView(view, params);

		// 设置触摸事件，实现归属地浮窗拖拽,,,当然我需要权限android.permission.SYSTEM_ALERT_WINDOW
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// 获取起点坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					int endX = (int) event.getRawX();
					int endY = (int) event.getRawY();
					// System.out.println(endX + "--" + endX);
					// 计算偏移量
					int dx = endX - startX;
					int dy = endY - startY;

					// 防止坐标偏离屏幕
					if (params.x < 0) {
						params.x = 0;
					}
					if (params.y < 0) {
						params.y = 0;
					}
					if (params.x > winWidth - view.getWidth()) {
						params.x = winWidth - view.getWidth();
					}
					if (params.y > winHeight - view.getHeight()) {
						params.y = winHeight - view.getHeight();
					}

					// 更新浮窗位置
					params.x += dx;
					params.y += dy;

					mWM.updateViewLayout(view, params);

					System.out.println("x: " + params.x + "y: " + params.y);

					// 重新初始化开始坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();

					break;
				case MotionEvent.ACTION_UP:
					saveLocation();
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	// 保存位置
	private void saveLocation() {
		Editor edit = mPref.edit();
		edit.putInt("lastX", params.x);
		edit.putInt("lastY", params.y);
		edit.commit();
	}

}
