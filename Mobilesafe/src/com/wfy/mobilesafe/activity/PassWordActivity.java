package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.service.WhatchDogService;
import com.wfy.mobilesafe.utils.MD5Utils;

/**
 * 程序锁，输入密码界面
 * 需要注意，启动模式
 */
public class PassWordActivity extends Activity {

    private PackageManager pm;
    private TextView tv_dog_appname;
    private ImageView iv_dog_appicon;
    private EditText et_dog_pass;
    private Button btn_ok;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_word);
        mPref = getSharedPreferences("config", MODE_PRIVATE);

        System.out.println("passactivity-----");

        pm = getPackageManager();

        tv_dog_appname = (TextView) findViewById(R.id.tv_dog_appname);
        iv_dog_appicon = (ImageView) findViewById(R.id.iv_dog_appicon);
        et_dog_pass = (EditText) findViewById(R.id.et_dog_pass);
        btn_ok = (Button) findViewById(R.id.btn_ok);

        final String packageName = getIntent().getStringExtra("packageName");
        System.out.println(packageName);
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            String appName = applicationInfo.loadLabel(pm).toString();
            tv_dog_appname.setText(appName);
            Drawable icon = applicationInfo.loadIcon(pm);
            iv_dog_appicon.setImageDrawable(icon);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pass = et_dog_pass.getText().toString().trim();

                    String applocks_pass = mPref.getString("applocks_pass", null);

                    if (applocks_pass.equals(MD5Utils.encode(pass))) {
                        finish();
                    } else {
                        Toast.makeText(PassWordActivity.this, "密码错误，密码为：123 ", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
        if (!WhatchDogService.runFlag.isEmpty()) {
            WhatchDogService.runFlag.clear();
        }
    }
}
