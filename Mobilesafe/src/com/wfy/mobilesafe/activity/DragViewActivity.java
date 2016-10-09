package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.wfy.mobilesafe.R;

/**
 * 修改归属地位置
 * 
 * @author wfy
 * 
 */
public class DragViewActivity extends Activity {
	private TextView tvTop;
	private TextView tvBottom;
	private ImageView ivDrag;

	private int startX;
	private int startY;
	private SharedPreferences mPref;
	private int winWidth;
	private int winHeight;
	private long[] mHits = new long[2];// 数组的长度表示需要点击的次数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drag_view);// 布局文件只是铺在activity的衣服
		mPref = getSharedPreferences("config", MODE_PRIVATE);

		tvTop = (TextView) findViewById(R.id.tv_top);
		tvBottom = (TextView) findViewById(R.id.tv_bottom);
		ivDrag = (ImageView) findViewById(R.id.iv_drag);

		int lastX = mPref.getInt("lastX", 0);
		int lastY = mPref.getInt("lastY", 0);

		// 获取屏幕宽和高
		Display defaultDisplay = getWindowManager().getDefaultDisplay();
		winWidth = defaultDisplay.getWidth();
		winHeight = defaultDisplay.getHeight();

		// onMeasure(测量view) onLayout(安放位置)
		// onDraw(绘制)----因为oncreate方法没有执行完这三个方法不会执行
		// ivDrag.layout(lastX, lastY, lastX + ivDrag.getWidth(),
		// lastY + ivDrag.getHeight());// 不能用这个方法，因为还没有测量完成，就不能安放位置
		// 同理 ivDrag.getWidth() 也会获取不到宽度没这事还没有测量完成

		RelativeLayout.LayoutParams params = (LayoutParams) ivDrag
				.getLayoutParams();
		params.leftMargin = lastX;// 设置左边距
		params.topMargin = lastY;// 设置top边距
		ivDrag.setLayoutParams(params);// 重新设置位置

		// 双击事件，，，注意在onTouch方法必须返回false 让事件往下传递
		ivDrag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
				mHits[mHits.length - 1] = SystemClock.uptimeMillis();// 开机持续的时间
				if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
					// 双击居中
					ivDrag.layout(winWidth / 2 - ivDrag.getWidth() / 2,
							winHeight / 2 - ivDrag.getHeight() / 2, winWidth
									/ 2 + ivDrag.getWidth() / 2, winHeight / 2
									+ ivDrag.getHeight() / 2);

					// 保存居中后的位置
					saveLocation();
				}
			}
		});

		// 设置触摸监听
		ivDrag.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// 获取起点坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					int endX = (int) event.getRawX();
					int endY = (int) event.getRawY();
					// System.out.println(endX + "--" + endX);
					// 偏移量
					int dx = endX - startX;
					int dy = endY - startY;

					int l = ivDrag.getLeft() + dx;
					int r = ivDrag.getRight() + dx;
					int t = ivDrag.getTop() + dy;
					int b = ivDrag.getBottom() + dy;

					// 判断书否超出屏幕边界，，注意状态栏高度，但是入股把activity设置成满屏就不用了
					if (l < 0 || r > winWidth || t < 0 || b > winHeight) {
						break;
					}

					// 根据是否大于或小于屏幕高度的一般，决定显示那个提示框
					if (t > winHeight / 2) {// 上显下隐
						tvTop.setVisibility(View.VISIBLE);
						tvBottom.setVisibility(View.INVISIBLE);
					} else {
						tvTop.setVisibility(View.INVISIBLE);
						tvBottom.setVisibility(View.VISIBLE);
					}

					/**
					 * layout方法部分源码 if ((mPrivateFlags3 &
					 * PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
					 * onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
					 * mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT; }
					 */
					// l Left position, relative to parent
					// t Top position, relative to parent
					// r Right position, relative to parent
					// b Bottom position, relative to parent
					// 更新安放位置
					ivDrag.layout(l, t, r, b);

					// 重新初始化开始坐标
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();

					// 出现跳动,必须使用偏移量，才不会出现跳动

					// * ivDrag.layout((int) event.getRawX(), (int)
					// * event.getRawY(), (int) (event.getRawX() +
					// * ivDrag.getWidth()), (int) (event.getRawY() +
					// * ivDrag.getHeight()));
					break;
				case MotionEvent.ACTION_UP:
					saveLocation();
					break;
				default:
					break;
				}
				return false;
			}
		});
	}

	// 保存位置
	private void saveLocation() {
		Editor edit = mPref.edit();
		edit.putInt("lastX", ivDrag.getLeft());
		edit.putInt("lastY", ivDrag.getTop());
		edit.commit();
	}
}
