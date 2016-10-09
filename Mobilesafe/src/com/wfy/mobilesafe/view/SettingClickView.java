package com.wfy.mobilesafe.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wfy.mobilesafe.R;

/**
 * 自定义组合控件 其实每个view对象都是对应着java中的一个类，当布局文件被加载到内存中时，就创建该view的对象 自定义View
 * 
 * @author wfy
 * 
 *         自定义SettingItemView的属性 如何使用？ 直接在需要使用该view的布局文件中，声明命名空间：如：xmlns:wfy=
 *         "http://schemas.android.com/apk/res/com.wfy.mobilesafe" 例如：wfy:title
 *         在SettingItemView类中的构造方法中通过Attribute拿到布局文件中的name和value
 * 
 *         然后修改private TextView tvTitle; private TextView tvDesc; private
 *         CheckBox cbStatus; 这三个view的值即可
 * 
 *         流程： 1、自定一个view，继承viewgroup 例如RelativeLayout 2、编写组合控件的布局文件，在自定义view中加载
 *         View.inflate(getContext(), R.layout.view_setting_item, this); 3、自定义属性
 * 
 */

/**
 * 这是组合控件
 * 
 */
@SuppressLint("NewApi")
public class SettingClickView extends RelativeLayout {

	private TextView tvTitle;
	private TextView tvDesc;

	public SettingClickView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView();
	}

	// 有style调用该构造方法
	public SettingClickView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	// 有属性调用该构造方法
	public SettingClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	// 在代码中new 调用次构造方法
	public SettingClickView(Context context) {
		super(context);
		initView();
	}

	// 初始化view
	private void initView() {
		// 将自定义还得布局文件设置给当前SettingItemView
		View view = View
				.inflate(getContext(), R.layout.view_setting_click, this);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvDesc = (TextView) view.findViewById(R.id.tv_desc);
	}

	// 设置TextView的内容
	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	// 设置TextView的内容
	public void setDesc(String desc) {
		tvDesc.setText(desc);
	}
}
