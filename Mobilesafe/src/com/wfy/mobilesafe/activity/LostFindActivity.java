package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wfy.mobilesafe.R;

/**
 * 手机防盗页面
 * 
 * @author wfy
 * 
 */
public class LostFindActivity extends Activity {
	private SharedPreferences mPref;
	private TextView tvPhone;
	private ImageView iv_locks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPref = getSharedPreferences("config", MODE_PRIVATE);

		boolean setuped = mPref.getBoolean("setuped", false);

		// 如果已经设置过向导，直接进入手机防盗页面，如果没有设置则进入向导页面
		if (setuped) {
			setContentView(R.layout.activity_lost_find);

			tvPhone = (TextView) findViewById(R.id.tv_phone);
			iv_locks = (ImageView) findViewById(R.id.iv_locks);
			String safePhone = mPref.getString("safe_phone", "");
			tvPhone.setText(safePhone);

			Boolean protect = mPref.getBoolean("protect", false);
			if (protect) {
				iv_locks.setImageResource(R.drawable.lock);
			} else {
				iv_locks.setImageResource(R.drawable.unlock);
			}

		} else {
			startActivity(new Intent(this, Setup1Activity.class));
			// 销毁当前activity
			finish();
		}

	}

	// 重新 进入向导页面
	public void enterSetup(View v) {
		startActivity(new Intent(this, Setup1Activity.class));
		finish();
	}
}
