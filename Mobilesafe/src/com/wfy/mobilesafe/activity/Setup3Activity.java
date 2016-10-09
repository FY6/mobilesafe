package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.ToastUtils;

/**
 * 第3个向导页面
 * 
 * @author wfy
 * 
 */
public class Setup3Activity extends BaseActivity {
	private EditText etPhone = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup3);
		etPhone = (EditText) findViewById(R.id.et_phone);
		
		String phone = mPref.getString("safe_phone", "");
		etPhone.setText(phone);
	}

	@Override
	public void showNextPage() {
		
		String phone = etPhone.getText().toString().trim();
		if(TextUtils.isEmpty(phone)){
			ToastUtils.showToast(this, "安全号码不能为空!");
			return;
		}
		
		mPref.edit().putString("safe_phone", phone).commit();
		
		startActivity(new Intent(this, Setup4Activity.class));
		finish();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_next_in, R.anim.trans_next_out);
	}

	@Override
	public void showPreviousPage() {
		startActivity(new Intent(this, Setup2Activity.class));
		finish();

		// 兩個界面切換的动画
		overridePendingTransition(R.anim.trans_previous_in,
				R.anim.trans_previous_out);

	}

	public void selectContact(View v) {
		Intent intent = new Intent(this, SelectContactActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == Activity.RESULT_OK) {
			String phone = data.getStringExtra("phone");
			phone = phone.replaceAll("-", "").replaceAll(" ", "");
			etPhone.setText(phone);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
