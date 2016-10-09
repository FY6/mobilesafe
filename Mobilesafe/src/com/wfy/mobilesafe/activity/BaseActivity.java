package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Toast;

/**
 * 抽取4个向导页，相同逻辑，封装到该抽象类
 * 
 * 不需要再清单文件中注册，因为该页面不展示
 * 
 * @author wfy
 * 
 */
public abstract class BaseActivity extends Activity {
	private GestureDetector mDetector;
	public SharedPreferences mPref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPref = getSharedPreferences("config", MODE_PRIVATE);

		// 手势识别器
		mDetector = new GestureDetector(this, new SimpleOnGestureListener() {

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {

				// 判断纵向滑动是否过大
				if (Math.abs(e2.getRawY() - e1.getRawY()) > 60) {
					Toast.makeText(BaseActivity.this, "不能这样划!",
							Toast.LENGTH_SHORT).show();
					return true;
				}
				// 判断速度是否过慢
				if (Math.abs(velocityX) < 100) {
					Toast.makeText(BaseActivity.this, "速度太屌慢了!",
							Toast.LENGTH_SHORT).show();
					return true;
				}

				// 向右滑，上一页
				if (e2.getRawX() - e1.getRawX() > 70) {
					showPreviousPage();
					return true;
				}
				// 向左滑，下一页
				if (e1.getRawX() - e2.getRawX() > 70) {
					showNextPage();
					return true;
				}

				return super.onFling(e1, e2, velocityX, velocityY);
			}

		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);// 委托给识别器
		return super.onTouchEvent(event);
	}

	// 强制子类实现
	public abstract void showNextPage();

	// 强制子类实现
	public abstract void showPreviousPage();

	// 鼠标点击下一页
	public void next(View v) {
		showNextPage();
	}

	// 鼠标点击上一页
	public void previous(View v) {
		showPreviousPage();
	}

}
