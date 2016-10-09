package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.db.dao.AddressDao;

/**
 * 归属地查询
 *
 * @author wfy
 */
public class AddressActivity extends Activity {
    private EditText etNumber;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        etNumber = (EditText) findViewById(R.id.et_number);
        tvResult = (TextView) findViewById(R.id.tv_result);

        // 给EditText文本内容监听
        etNumber.addTextChangedListener(new TextWatcher() {
            // 内容发生变化调用
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String address = AddressDao.getAddress(s.toString());
                tvResult.setText("查询结果: " + address);
            }

            // 内容发生变化之前调用
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            // 内容发生变化之后调用
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 归属地查询
     *
     * @param v
     */
    public void query(View v) {
        String number = etNumber.getText().toString().trim();

        if (!TextUtils.isEmpty(number)) {

            String address = AddressDao.getAddress(number);
            tvResult.setText("查询结果: " + address);
        } else {
            // 抖动
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);

            // 插补器
            // shake.setInterpolator(new Interpolator() {
            //
            // @Override
            // public float getInterpolation(float input) {
            // return 0;
            // }
            // });
            etNumber.startAnimation(shake);

            vibrate();
        }
    }

    // 手机震动
    private void vibrate() {

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // vibrator.vibrate(2000);// 震动2秒，，权限android.permission.VIBRATE

        // 先等待1秒，在震动2秒，在等待1秒，在震动3秒
        // 参数2：-1表示只执行一次，不循环，0 从头循环
        // 参数2表示从低级个位置开始循环
        vibrator.vibrate(new long[]{1000, 2000, 1000, 3000}, -1);
    }
}
