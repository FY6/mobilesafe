package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wfy.mobilesafe.R;

/**
 * 第一个向导页面
 * 
 * @author wfy
 * 
 */
public class Setup1Activity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup1);
	}

	@Override
	public void showNextPage() {
		startActivity(new Intent(this, Setup2Activity.class));
		finish();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);
	}

	@Override
	public void showPreviousPage() {
	}
}
