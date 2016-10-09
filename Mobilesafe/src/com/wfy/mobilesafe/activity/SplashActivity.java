package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.StreamUtils;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 闪屏页
 *
 * @author wfy
 */
public class SplashActivity extends Activity {

    // 请求成功
    private static final int REQUEST_OK = 200;
    // 闪屏展现时间，以毫秒为单位
    private static final int SPLASH_SHOW_TIME = 2000;
    // 弹版本升级对话框
    protected static final int CODE_UPDATE_DIALOG = 0;
    // 弹Toast Url出现错误
    protected static final int CODE_URL_ERROR = 1;
    // 弹Toast 网络出现错误
    protected static final int CODE_NET_ERROR = 2;
    // 弹Toast JSON解析出现错误
    protected static final int CODE_JSON_ERROR = 3;
    // 当没有最新版本升级时，进入主页面
    protected static final int CODE_ENTER_HOME = 4;

    // 控件
    private TextView tvVersion;
    private TextView tvProgress;
    // 服务器的信息
    private String mVersionName;// 版本名
    private int mVersionCode;// 版本还
    private String mDescription;// 版本描述
    private String mdoWnloadUrl;// apk文件下载地址
    // 定义Handler，因为子线程不能更新UI，UI只能主线程中更新，则子线程发送消息给主线程，让主线程更新UI
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CODE_UPDATE_DIALOG:// 弹对话框
                    showUpdateDialog();
                    break;
                case CODE_URL_ERROR:// Url出错，弹吐司告诉用户，Url错误
                    Toast.makeText(SplashActivity.this, "URL错误", Toast.LENGTH_SHORT)
                            .show();
                    enterHome();
                    break;
                case CODE_NET_ERROR:// 网络出错
                    Toast.makeText(SplashActivity.this, "网络错误", Toast.LENGTH_SHORT)
                            .show();
                    enterHome();
                    break;
                case CODE_JSON_ERROR:// 数据解析失败
                    Toast.makeText(SplashActivity.this, "数据解析失败",
                            Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                // 进入主页面
                case CODE_ENTER_HOME:
                    enterHome();
                    break;
            }
        }

        ;
    };
    private SharedPreferences mPref;
    private RelativeLayout rl_root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


//        有米配配置
        AdManager.getInstance(this).init("5208af6bc5d167ae",
                "70ae5163b725763e", false);

//        AdManager.getInstance(this).init("5208af6bc5d167ae", "70ae5163b725763e");
        OffersManager.getInstance(this).onAppLaunch();

        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvVersion.setText("版本名:" + getVersionName());
        tvProgress = (TextView) findViewById(R.id.tv_progress);// 默认隐藏
        // 拿到当前布局文件的根节点
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);

        mPref = getSharedPreferences("config", MODE_PRIVATE);

        // 拷贝数据库文件
        copyDB("address.db");
        copyDB("commonnum.db");
        copyDB("antivirus.db");

        //创建快捷方式
        createShortCut();

        boolean autoUpdate = mPref.getBoolean("auto_update", true);
        if (autoUpdate) {
            checkVersion();
        } else {
            // 延迟延迟2秒发送消息，进入主页面
            handler.sendEmptyMessageDelayed(CODE_ENTER_HOME, 2000);
        }

        // 闪屏也渐变效果
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(2000);
        rl_root.startAnimation(alphaAnimation);

    }

    /**
     * 创建快捷方式
     */
    private void createShortCut() {
        /**
         * 1、想干什么事
         * 2、叫什么
         * 3、长什么样
         */
        Intent intent = new Intent();
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        //是否允许创建多个快捷方式
        intent.putExtra("duplicate", false);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "手机安全卫士");
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        //快捷方式想干的事
//        Intent shortcut_intent = new Intent(this, HomeActivity.class);
        Intent shortcut_intent = new Intent();
        shortcut_intent.setAction("com.wfy.mobilesafe.activity.HomeActivity");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut_intent);

        sendBroadcast(intent);
    }

    // 获取版本名
    private String getVersionName() {
        // 拿到应用包管理器
        PackageManager packageManager = getPackageManager();
        try {
            // 获取包信息
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getPackageName(), 0);
            // 获取版本名
            String versionName = packageInfo.versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取当前应用的版本号
     *
     * @return 版本号 如果没有拿到版本号 返回-1 否则返回 版本号
     */
    private int getVersionCode() {
        // 拿到应用包管理器
        PackageManager packageManager = getPackageManager();
        try {
            // 获取包信息
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    getPackageName(), 0);
            // 获取版本号
            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 检查版本更新: 从服务器获取数据，其中包含了，应用的版本号， 用当前应用的版本号与服务器版本号比较， 如果服务器 版本号大于当前应用的版本号
     * 那么久提示用户需不要友新的版本，需要更新版本。 服务器通过JSON返回数据
     */
    private void checkVersion() {
        // 访问网络的开始时间
        final long startTime = System.currentTimeMillis();

        Thread t = new Thread() {

            private HttpURLConnection conn;

            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                // //这是解决4中情况都不发生时，就不调用
                // msg.what = -1;
                // Gentmotion使用改地址：10.0.3.2:8080
                // eclipse自带模拟器使用：10.0.2.2:8080
                try {
                    URL url = new URL("http://10.0.3.2:8080/update.json");
                    // 打开网络连接
                    conn = (HttpURLConnection) url.openConnection();
                    // 设置请求方式
                    conn.setRequestMethod("GET");
                    // 设置连接超时
                    conn.setConnectTimeout(5000);
                    // 设置响应超时，此时已经和服务器连接上了，但是服务器迟迟不给响应
                    conn.setReadTimeout(5000);
                    // 连接服务器
                    conn.connect();
                    // 获取服务器返回的响应吗，如果等于200说明连接成功
                    int responseCode = conn.getResponseCode();
                    if (responseCode == REQUEST_OK) {
                        // 拿到服务器以流的方式返回的数据
                        InputStream in = conn.getInputStream();
                        // 调用StreamUtils的readFromStream方法把服务器返回的数据转换为String
                        String result = StreamUtils.readFromStream(in);

                        // 解析JSON
                        JSONObject jo = new JSONObject(result);
                        mVersionName = jo.getString("versionName");
                        mVersionCode = jo.getInt("versionCode");
                        mDescription = jo.getString("description");
                        mdoWnloadUrl = jo.getString("downloadUrl");

                        // 判断服务器的版本号是否大于本地app版本号，如果大于说明需要升级
                        if (mVersionCode > getVersionCode()) {
                            // 子线程不能刷新UI，使用handler通知主线程刷新UI
                            // showUpdateDialog();
                            msg.what = CODE_UPDATE_DIALOG;
                        } else {
                            // 进入时间太快，用户没有看到闪屏的效果就进入主页面了。
                            // enterHome();
                            msg.what = CODE_ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    // url错误异常
                    msg.what = CODE_URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    // 网络错误异常
                    msg.what = CODE_NET_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    // JSON解析失败
                    msg.what = CODE_JSON_ERROR;
                    e.printStackTrace();
                } finally {

                    // 解决方案
                    long endTime = System.currentTimeMillis();
                    // timeUsed 访问网路所有的时间
                    long timeUsed = endTime - startTime;

                    // 停留在闪屏页面至少两秒，，强制休眠，让闪屏展现2秒钟
                    if (timeUsed < SPLASH_SHOW_TIME) {
                        try {
                            Thread.sleep(SPLASH_SHOW_TIME - timeUsed);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // 发送消息，告知UI线程更新UI
                    handler.sendMessage(msg);

                    // 关闭网络连接
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

        };
        t.start();

    }

    // 弹出提示版本更新的对话框
    protected void showUpdateDialog() {
        // 拿到对话框创建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置对话框的标题
        builder.setTitle("最新版本" + mVersionName);
        // 设置对话框的正文
        builder.setMessage(mDescription);
        // builder.setCancelable(false);//不让用户取消对话框，用户体验差，尽量不要用
        // 设置确定按钮
        builder.setPositiveButton("马上更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("马上更新");
                // 下载apk文件
                downLoad();
            }
        });
        // 设置取消按钮
        builder.setNegativeButton("以后再说", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHome();
            }
        });
        // 设置取消监听，当用户点击返回键时触发
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // 进入主界面
                enterHome();
            }
        });
        // 可以不用调用此方法，应为show方法中已经调用，请看show方法的源码
        // builder.create();
        builder.show();
    }

    /**
     * 进入主页面
     */
    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        // 销毁当前activity
        finish();
    }

    /**
     * 下载apk文件
     */
    private void downLoad() {

        // gone：真的消失，不占位置
        // invisible:消失了，但是还占着位置
        // visible:显示
        tvProgress.setVisibility(View.VISIBLE);// 展现进度

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            // 保存 到该路径（sdcard根目录下）
            String target = Environment.getExternalStorageDirectory()
                    + "/update.apk";
            HttpUtils utils = new HttpUtils();
            System.out.println(mdoWnloadUrl);
            utils.download(mdoWnloadUrl, target, new RequestCallBack<File>() {

                // 正在下载

                /**
                 * total:下载文件的总大小 current:已下载文件的大小 isUoloading:是否是上传
                 */
                @Override
                public void onLoading(long total, long current,
                                      boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    System.out.println(total + "---" + current);

                    tvProgress.setText("下载进度: " + current * 100 / total + "%");
                }

                // 下载成功
                @Override
                public void onSuccess(ResponseInfo<File> arg0) {
                    Toast.makeText(SplashActivity.this, "下载成功",
                            Toast.LENGTH_SHORT).show();

                    // 隐式
                    // 下载成功就安装
                    // 开启系统安装器安装应用
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setDataAndType(Uri.fromFile(arg0.result),
                            "application/vnd.android.package-archive");

                    // startActivity(intent);
                    startActivityForResult(intent, 0);// 当用户点击取消，会返回结果，并回调onActivityResult方法
                }

                // 下载失败
                @Override
                public void onFailure(HttpException arg0, String arg1) {
                    Toast.makeText(SplashActivity.this, "下载失败",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(SplashActivity.this, "没有sdcard", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // 当用户点击取消，回调此方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHome();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 拷贝数据库文件,,,assets目录下存放的资源代表应用无法直接访问的原生资源，
    // 应用程序通过AssetManager以二进制流的形式来读取资源。
    //首先把有数据的数据库文件放在assets资源目录下边，
    // 然后在apk应用启动的时候，把assets目录下的数据库文件的数据写入到真机的内存中去。
    private void copyDB(String fileName) {

        File destFile = new File(getFilesDir(), fileName);

        if (destFile.exists()) {
            System.out.println("数据库文件 " + fileName + "已经存在");
            return;
        }
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = getAssets().open(fileName);
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int len = 0;

            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
