package com.wfy.mobilesafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.wfy.mobilesafe.R;

/**
 * 第4个向导页面
 * 
 * @author wfy
 * 
 */
public class Setup4Activity extends BaseActivity {

	private CheckBox cb_protect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup4);
		cb_protect = (CheckBox) findViewById(R.id.cb_protect);
		
		Boolean protect = mPref.getBoolean("protect", false);
		if (protect) {
			cb_protect.setChecked(true);
			cb_protect.setText("防盗保护已经开启");
		} else {
			cb_protect.setChecked(false);
			cb_protect.setText("防盗保护已经关闭");
		}

		cb_protect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				//System.out.println(isChecked);
				if (isChecked) {
					cb_protect.setText("防盗保护已经开启");
					mPref.edit().putBoolean("protect", true).commit();
				} else {
					cb_protect.setText("防盗保护已经关闭");
					mPref.edit().putBoolean("protect", false).commit();
				}
				cb_protect.setChecked(isChecked);
			}
		});
	}

	@Override
	public void showNextPage() {
		startActivity(new Intent(this, LostFindActivity.class));
		finish();
		// 保存是否已经设置过向导的状态
		mPref.edit().putBoolean("setuped", true).commit();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);

	}

	@Override
	public void showPreviousPage() {
		startActivity(new Intent(this, Setup3Activity.class));
		finish();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_previous_in,
				R.anim.trans_previous_out);

	}
}
