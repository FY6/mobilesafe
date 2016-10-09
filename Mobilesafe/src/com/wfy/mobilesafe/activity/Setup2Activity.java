package com.wfy.mobilesafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.ToastUtils;
import com.wfy.mobilesafe.view.SettingItemView;

/**
 * 第二个向导页面
 * 
 * @author wfy
 * 
 */
public class Setup2Activity extends BaseActivity {

	private SettingItemView siv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup2);
		siv = (SettingItemView) findViewById(R.id.siv_sim);

		String sim = mPref.getString("sim", null);
		if (!TextUtils.isEmpty(sim)) {
			siv.setStatus(true);
		} else {
			siv.setStatus(false);
		}

		siv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (siv.isChecked()) {
					siv.setStatus(false);
					mPref.edit().remove("sim").commit();
				} else {
					siv.setStatus(true);
					// 获取sim卡序列号

					TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
					String simSerialNumber = manager.getSimSerialNumber();
					// System.out.println(simSerialNumber);

					mPref.edit().putString("sim", simSerialNumber).commit();
				}
			}
		});
	}

	@Override
	public void showNextPage() {

		String sim = mPref.getString("sim", null);
		if (TextUtils.isEmpty(sim)) {
			ToastUtils.showToast(this, "必须绑定sim卡");
			return;
		}

		startActivity(new Intent(this, Setup3Activity.class));
		finish();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);
	}

	@Override
	public void showPreviousPage() {
		startActivity(new Intent(this, Setup1Activity.class));
		finish();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_previous_in,
				R.anim.trans_previous_out);
	}

}
