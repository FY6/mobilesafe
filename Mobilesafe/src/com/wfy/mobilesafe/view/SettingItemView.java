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
public class SettingItemView extends RelativeLayout {

	// 命名空间，---com.wfy.mobilesafe 应用包名
	private static final String NAMESPACE = "http://schemas.android.com/apk/res/com.wfy.mobilesafe";
	private TextView tvTitle;
	private TextView tvDesc;
	private CheckBox cbStatus;

	private String title;
	private String descOn;
	private String descOff;

	public SettingItemView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView();
	}

	// 有style调用该构造方法
	public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}

	// 有属性调用该构造方法
	public SettingItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 获取我们自定义组件的布局文件中属性的值
		title = attrs.getAttributeValue(NAMESPACE, "title");// 根据属性名称获取属性的值
		descOn = attrs.getAttributeValue(NAMESPACE, "desc_on");
		descOff = attrs.getAttributeValue(NAMESPACE, "desc_off");

		initView();
	}

	// 在代码中new 调用次构造方法
	public SettingItemView(Context context) {
		super(context);
		initView();
	}

	// 初始化view
	private void initView() {
		// 将自定义还得布局文件设置给当前SettingItemView，和当前ViewGroup关联，传个this
		View view = View
				.inflate(getContext(), R.layout.view_setting_item, this);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvDesc = (TextView) view.findViewById(R.id.tv_desc);
		cbStatus = (CheckBox) view.findViewById(R.id.cb_status);

		setTitle(title);
	}

	// 设置TextView的内容
	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	// 设置TextView的内容
	public void setDesc(String desc) {
		tvDesc.setText(desc);
	}

	// 设置checkbox的选中状态
	public void setStatus(boolean check) {
		cbStatus.setChecked(check);
		// 根据选择的状态，来改变描述的值
		if (check) {
			setDesc(descOn);
		} else {
			setDesc(descOff);
		}
	}

	// 判断checkbob是否选中
	public boolean isChecked() {
		return cbStatus.isChecked();
	}

}
