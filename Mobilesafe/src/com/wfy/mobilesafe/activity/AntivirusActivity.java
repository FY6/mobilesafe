package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wfy.domain.Antivirus;
import com.wfy.domain.AntivirusScannerInfo;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.db.dao.AntivirusDao;
import com.wfy.mobilesafe.utils.MD5Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 病毒查杀
 * Created by wfy on 2016/8/2.
 */
public class AntivirusActivity extends Activity {

    private static final int BEGINING = 0;//开始扫描
    private static final int SCANNING = 1;//正在扫描
    private static final int FINISH = 2;//扫描结束
    private static final int NETWORKANYIVIRUS = 3;//联网云查杀
    private ImageView act_scaning;
    private TextView tv_scanner_title;
    private ProgressBar pb_scanner_bar;
    private LinearLayout ll_content;
    private ScrollView scrollview;
    private TextView tv_antivirus_num;
    private TextView tv_scanner_num;
    public static final long ANIMATIONTIME = 3000;//动画运行一个周期的时间
    private ArrayList<AntivirusScannerInfo> antivirus;//存放扫描发现的病毒

    private AntivirusDao dao;//病毒Dao
    private RotateAnimation ra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antivirus);

        initUI();//初始化UI
        initData();//初始化数据
    }

    /**
     * 更新病毒数据库
     *
     * @param v
     */
    public void update(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AntivirusActivity.this);
        builder.setTitle("更新病毒库");
        builder.setMessage("是否更新病毒库？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateAntivirus();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 注意：
     * Android 6.0以后，使用xUtils部分报错，是因为google在6.0以后强制使用HttpURLConnection。
     * 解决：手动把库就到项目中，studio：在build.gradle中加入
     * android{useLibrary 'org.apache.http.legacy'}
     * eclipse：加入 org.apache.http.legacy jar文件
     * 更新病毒数据库
     */
    private void updateAntivirus() {

        dao = new AntivirusDao();
        //192.168.56.1
        String url = "http://192.168.56.1:8080/antivirus.json";
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.send(HttpRequest.HttpMethod.GET, url, new RequestCallBack<String>() {
            //请求服务器成功，此方法被调用
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                System.out.println(responseInfo.result);
                   /* JSONObject jsonObject = new JSONObject(responseInfo.result);
                    String md5 = (String) jsonObject.get("MD5");
                    String desc = (String) jsonObject.get("desc");
                    String type = (String) jsonObject.get("type");
                    String name = (String) jsonObject.get("name");
                     Antivirus antivirus = new Antivirus(md5, desc, type, name);
                    */
                //使用google GSON框架，解析JSON数据
                Gson gson = new Gson();
                /**
                 * fromJson(arg1, arg2):
                 * arg1:json字符串
                 * arg2:封装Json数据的javabean
                 */
                Antivirus antivirus = gson.fromJson(responseInfo.result, Antivirus.class);
                //判断病毒库中是否已经存在该病毒的MD5码，如果没有就更新病毒库
                if (dao.findByMD5(antivirus.getMD5())) {
                    Toast.makeText(AntivirusActivity.this, "病毒库已经是最新版本!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dao.add(antivirus);
            }

            //请求服务器失败，此方法被调用
            @Override
            public void onFailure(HttpException e, String s) {
                Toast.makeText(AntivirusActivity.this, "网络错误，请检查网络连接!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 消息处理机制
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BEGINING:
                    antivirus = new ArrayList<AntivirusScannerInfo>();
                    tv_scanner_num.setText("扫描数:" + AntivirusScannerInfo.scannerNum);
                    tv_scanner_title.setText("开始快速查杀病毒....");
                    break;
                case SCANNING://正在扫描
                    scanning(msg);
                    break;
                case FINISH://完成扫描
                    Finish();
                    break;
                case NETWORKANYIVIRUS:
                    tv_scanner_title.setText("开始联网查杀病毒....");
                    ll_content.removeAllViews();//把LinearLayout所有child全部移除
                    connNetWorkAntivirus();
                    break;
            }
        }
    };

    //联网查杀病毒
    private void connNetWorkAntivirus() {
        if (!antivirus.isEmpty()) {
            for (AntivirusScannerInfo antivirusInfo : antivirus) {
                View view = View.inflate(AntivirusActivity.this, R.layout.item_antivirus_list, null);
                TextView appName = (TextView) view.findViewById(R.id.item_appName);
                TextView info = (TextView) view.findViewById(R.id.item_info);
                CheckBox cb_yn = (CheckBox) view.findViewById(R.id.cb_yn);

                appName.setTextColor(Color.RED);
                appName.setText(antivirusInfo.getAppName());
                info.setText(antivirusInfo.getDesc());
                cb_yn.setChecked(false);

                ll_content.addView(view, 0);
            }
        } else {
            TextView textView = new TextView(AntivirusActivity.this);
            textView.setText("没有发现病毒...");
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(20);

            ll_content.addView(textView);
        }
    }

    //完成扫描
    private void Finish() {
        pb_scanner_bar.setProgress(AntivirusScannerInfo.scannerNum);
        //如果已经扫描结束，停止动画
        act_scaning.clearAnimation();

        //ra.cancel();
        //如果扫描发现病毒，弹出对话框提示用户是否清楚病毒
        if (antivirus != null && antivirus.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AntivirusActivity.this);
            builder.setCancelable(false);
            builder.setTitle("清理提示框");
            builder.setMessage("检测到" + antivirus.size() + "个危险软件！");
            builder.setPositiveButton("立即清理病毒", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("稍后清理", null);
            builder.show();
        } else {
            Toast.makeText(AntivirusActivity.this, "你的手机很安全，放心使用！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 正在扫描
     *
     * @param msg
     */
    private void scanning(Message msg) {
        AntivirusScannerInfo scanner = (AntivirusScannerInfo) msg.obj;
        //TextView textView = new TextView(AntivirusActivity.this);
        //textView.setTextSize(12);
                   /* if (scanner.isAntivirus()) {
                        textView.setTextColor(Color.RED);
                        textView.setText(scanner.getAppName() + "               " + scanner.getDesc());
                    } else {
                        textView.setText(scanner.getAppName() + "               安全");
                    }*/

        //这里做不了优化，因为linearlayout不能添加相同的view孩子（child）
        View view = View.inflate(AntivirusActivity.this, R.layout.item_antivirus_list, null);
        TextView appName = (TextView) view.findViewById(R.id.item_appName);
        TextView info = (TextView) view.findViewById(R.id.item_info);
        CheckBox cb_yn = (CheckBox) view.findViewById(R.id.cb_yn);

        if (scanner.isAntivirus()) {
            antivirus.add(scanner);//加入病毒库中
            tv_antivirus_num.setText("病毒软件:" + scanner.getAntivirusNum());
            appName.setTextColor(Color.RED);
            appName.setText(scanner.getAppName());
            info.setText(scanner.getDesc());
            cb_yn.setChecked(false);
        } else {
            appName.setText(scanner.getAppName());
            info.setText("安全");
            cb_yn.setChecked(true);

        }
        //ll_content.addView(view, 0);
        //scrollview.fullScroll(View.FOCUS_UP);
        ll_content.addView(view);
        //自动滚动
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * dataDir: 分配给此Application的存放数据的位置。通常是：/data/data/packageName/
     * SourceDir: 安装这个包后的存放位置。 因为APK安装后，
     * 会将archive文件存放在某个目录(一般程序和//root程序位置不同)。
     * 作为读取资源是的位置。此位置通常在/data/app/pakcageName.apk
     */
    private void initData() {
        new Thread() {
            @Override
            public void run() {
                PackageManager mPackageManager = getPackageManager();
                //获取到所有安装应用
                List<PackageInfo> mPackageManagerInstalledPackages = mPackageManager.getInstalledPackages(0);
                //扫描数
                AntivirusScannerInfo.scannerNum = mPackageManagerInstalledPackages.size();

                long startTime = System.currentTimeMillis();


                SystemClock.sleep(2000);
                //发送开始扫描消息
                Message msg = handler.obtainMessage();
                msg.what = BEGINING;
                handler.sendMessage(msg);
                //病毒数
                int antivirusNum = 0;

                //初始化进度条最大值
                pb_scanner_bar.setMax(mPackageManagerInstalledPackages.size());
                int progress = 0;
                for (PackageInfo mPackage : mPackageManagerInstalledPackages) {
                    //创建病毒扫描消息，封装扫描过程中产生的消息
                    AntivirusScannerInfo scanner = new AntivirusScannerInfo();
                    //获取应用名称
                    String appName = (String) mPackage.applicationInfo.loadLabel(mPackageManager);
                    scanner.setAppName(appName);
                    String packageName = mPackage.applicationInfo.packageName;
                    scanner.setPackageName(packageName);
                    //获取应用程序的apk文件的资源目录
                    String sourceDir = mPackage.applicationInfo.sourceDir;//--sout--->/data/app/com.wfy.mobilesafe-1.apk

                    //拿到应用程序的md5,即：对apk文件进行md5加密
                    String apkMD5 = MD5Utils.getMD5File(sourceDir);
                    //AntivirusDao.checkAntivirus(apkMD5)：返回true为病毒，否则 不是病毒
                    scanner.setAntivirus(AntivirusDao.checkAntivirus(apkMD5, scanner));

                    System.out.println(sourceDir + "--" + apkMD5);//d8c98ea4ec172af717d342f736e67279,17869cbb9f3ecf022be71c9a9aef118d

                    if (AntivirusDao.checkAntivirus(apkMD5, scanner)) {
                        antivirusNum++;
                        scanner.setAntivirusNum(antivirusNum);
                    }
                    //让程序睡眠
                    SystemClock.sleep(300);
                    //更新进度条
                    progress++;
                    pb_scanner_bar.setProgress(progress);

                    //发送消息
                    msg = handler.obtainMessage();
                    msg.what = SCANNING;
                    msg.obj = scanner;
                    handler.sendMessage(msg);
                }

                SystemClock.sleep(5000);
                //联网云查杀
                msg = handler.obtainMessage();
                msg.what = NETWORKANYIVIRUS;
                handler.sendMessage(msg);
                for (int i = 0; i < 50; i++) {
                    SystemClock.sleep(60);
                    pb_scanner_bar.setProgress(i + 5);
                }

                long endTime = System.currentTimeMillis();
                long runTime = endTime - startTime;
                //发送扫描完毕消息，如果扫描比规定时间短，则发送延迟消息，解决扫描比动画提前结束出现跳动效果，停止动画
                //即：必须让动画完成周期旋转
                msg = handler.obtainMessage();
                msg.what = FINISH;
               /* if (runTime <= ANIMATIONTIME) {
                    handler.sendMessageDelayed(msg, ANIMATIONTIME - runTime);
                } else if (runTime > ANIMATIONTIME) {
                    handler.sendMessageDelayed(msg, ANIMATIONTIME - (runTime % ANIMATIONTIME));
                }*/
                //拿到延迟的时间
                long mDelayed = runTime <= ANIMATIONTIME ? ANIMATIONTIME - runTime :
                        ANIMATIONTIME - (runTime % ANIMATIONTIME);
                handler.sendMessageDelayed(msg, mDelayed);
            }
        }.start();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        act_scaning = (ImageView) findViewById(R.id.iv_act_scanning);
        tv_scanner_title = (TextView) findViewById(R.id.tv_scanner_title);
        pb_scanner_bar = (ProgressBar) findViewById(R.id.pb_scanner_bar);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        scrollview = (ScrollView) findViewById(R.id.scrollview);
        tv_antivirus_num = (TextView) findViewById(R.id.tv_antivirus_num);
        tv_scanner_num = (TextView) findViewById(R.id.tv_scanner_num);

        //旋转动画
        ra = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(ANIMATIONTIME);
        ra.setRepeatCount(Animation.INFINITE);//只要小于0就无限循环

        act_scaning.startAnimation(ra);
    }
}
