package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.wfy.domain.AppInfo;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.AppInfosUtils;

import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends Activity {
    /**
     * 通过xUtils拿到组件,
     * 完全注解方式就可以进行UI绑定和事件绑定。
     * 无需findViewById和setClickListener等。
     */
    @ViewInject(R.id.list_view)
    private ListView list_view;
    @ViewInject(R.id.tv_rom)
    private TextView tv_rom;
    @ViewInject(R.id.tv_sd)
    private TextView tv_sd;
    @ViewInject(R.id.tv_app)
    private TextView tv_app;

    private List<AppInfo> appInfos;//所有app信息
    private AppManagerAdapter adapter;//适配器
    private List<AppInfo> systemInfos;
    private List<AppInfo> userInfos;
    private PopupWindow popupWindow;
    private AppInfo clickAppinfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化UI
        initUI();
        //初始化数据
        initData();
    }

    //创建handler处理消息
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (adapter == null) {
                adapter = new AppManagerAdapter();
                list_view.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };

    /**
     * 初始化数据
     */
    private void initData() {
        //开启子线程获取数据
        new Thread() {
            @Override
            public void run() {
                //获取所有app信息
                appInfos = AppInfosUtils.getAppInfos(AppManagerActivity.this);
                userInfos = new ArrayList<AppInfo>();
                systemInfos = new ArrayList<AppInfo>();
                for (AppInfo infos : appInfos) {
                    if (infos.isUserApp()) {
                        userInfos.add(infos);
                    } else {
                        systemInfos.add(infos);
                    }
                }
                handler.sendEmptyMessage(0);//发送空消息更新UI
            }
        }.start();
    }

    //初始化UI
    private void initUI() {
        setContentView(R.layout.activity_app_manager);
        ViewUtils.inject(this);//注入view和事件

        //获取手机内部存储空间romd-----data/data....目录
        // getDataDirectory()返回 /data 目录
        long rom_freeSpace = Environment.getDataDirectory().getFreeSpace();
        //获取手机sd卡存储空间,getExternalStorageDirectory()返回sd卡目录
        final long sd_freeSpace = Environment.getExternalStorageDirectory().getFreeSpace();

        tv_rom.setText("内存可用: " + Formatter.formatFileSize(this, rom_freeSpace));
        tv_sd.setText("SD卡可用: " + Formatter.formatFileSize(this, sd_freeSpace));

        //给listview设置点击监听
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            /**
             *通过一个帧布局，来实现切换
             * @param view
             * @param firstVisibleItem 第一个可见的item
             * @param visibleItemCount 可见item的个数
             * @param totalItemCount 所有item的个数
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //关闭popupwindwon
                closePopupWindwon();
                if (userInfos != null && systemInfos != null) {
                    if (firstVisibleItem > userInfos.size()) {
                        tv_app.setText("系统程序(" + systemInfos.size() + ")个");
                    } else if (systemInfos != null) {
                        tv_app.setText("用户程序(" + userInfos.size() + ")个");
                    }
                }
            }
        });

        //给listviewitem设置点击监听,弹出Popupwindown
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (position == 0 || position == userInfos.size() + 1) {
                    return;
                }
                //关闭popupwindwon
                closePopupWindwon();

                View v = View.inflate(AppManagerActivity.this, R.layout.popup_app_manager, null);
                //拿到组件
                LinearLayout uninstall = (LinearLayout) v.findViewById(R.id.ll_uninstatll);//卸载
                LinearLayout run = (LinearLayout) v.findViewById(R.id.ll_run);//运行
                LinearLayout shared = (LinearLayout) v.findViewById(R.id.ll_shared);//分享
                LinearLayout detail = (LinearLayout) v.findViewById(R.id.ll_detail);//应用详细信息


                clickAppinfo = (AppInfo) parent.getItemAtPosition(position);//调用的是getItem方法

                //设置点击监听
                //卸载
                uninstall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position <= userInfos.size()) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse("package:" + clickAppinfo.getApkPackageName()));
                            // startActivity(intent);
                            startActivityForResult(intent, 0);//卸载完程序，就重新获取数据，并刷新页面
                            closePopupWindwon();
                        } else {
                            Toast.makeText(AppManagerActivity.this,
                                    "卸载系统程序需要Root权限，请先获取Root权限!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                /**
                 * 运行程序只需要，对应的包名即可
                 */
                run.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (clickAppinfo != null) {
                            Intent start_intent = AppManagerActivity.this.getPackageManager().
                                    getLaunchIntentForPackage(clickAppinfo.getApkPackageName());
                            startActivity(start_intent);
                            closePopupWindwon();
                        }
                    }
                });
                //分享
                shared.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("shared");
                        //                       Intent intent = new Intent(Intent.ACTION_SEND);
//                      shared_intent.putExtra(Intent.EXTRA_SUBJECT,"分享");
//                      shared_intent.putExtra(Intent.EXTRA_TEXT,"Hi,推荐您使用软件: "+clickAppinfo.getApkName());
                        if (clickAppinfo != null) {
                            Intent shared_intent = new Intent("android.intent.action.SEND");
                            shared_intent.addCategory("android.intent.category.DEFAULT");
                            shared_intent.setType("text/plain");
                            shared_intent.putExtra("android.intent.extra.SUBJECT", "分享");
                            shared_intent.putExtra("android.intent.extra.TEXT", "Hi,推荐您使用软件: " + clickAppinfo.getApkName() +
                                    " 下载地址: " + "https://play.google.com/store/apps/details?id=" + clickAppinfo.getApkPackageName());
                            startActivity(Intent.createChooser(shared_intent, "分享"));
                            closePopupWindwon();
                        }
                    }
                });
//应用详细信息
                detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse("package:" + clickAppinfo.getApkPackageName()));
                        startActivity(intent);
                        closePopupWindwon();
                    }
                });

                popupWindow = new PopupWindow(v, -2, -2);
                //点击小球旁边消失
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);

                //必须的设置背景，不然没有动画
                popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                //动画效果
                //缩放动画
                ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1.0f, 0.8f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                scaleAnimation.setDuration(600);
                //透明动画
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                alphaAnimation.setDuration(850);

                v.startAnimation(alphaAnimation);
                v.startAnimation(scaleAnimation);

                //这里是面向对象
                int[] loaction = new int[2];//存放x，y的值
                view.getLocationInWindow(loaction);//拿到view在窗体中的位置，x，y
                //获取ImageView的宽度，好确定popup的x位置，因为padding = 6，所以减6
                ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
                int locationX = ivIcon.getWidth() + 6;
                // System.out.println("width= " + ivIcon.getWidth());
                if (position != 0 && position != userInfos.size() + 1) {
                    popupWindow.showAtLocation(parent, Gravity.LEFT + Gravity.TOP, locationX, loaction[1]);//显示在左上位置
                }
            }
        });
    }

    //卸载完程序，就重新获取数据，并刷新页面
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initData();
    }

    //关闭PopupWindwon
    private void closePopupWindwon() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    //适配器
    private class AppManagerAdapter extends BaseAdapter {
        @Override
        public Object getItem(int position) {
            AppInfo appInfo = null;
            if (position <= userInfos.size() && position > 0) {
                appInfo = userInfos.get(position - 1);
            } else if (position > userInfos.size() && position > userInfos.size() + 1) {
                appInfo = systemInfos.get(position - userInfos.size() - 2);
            }
            return appInfo;
        }

        @Override
        public int getCount() {
            return userInfos.size() + systemInfos.size() + 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * 使用上一次返回去的view作为缓存
         * System.out: p = 0--android.widget.TextView@b3c10058
         * System.out: p = 1--android.widget.TextView@b3c10058
         * System.out: p = 2--android.widget.LinearLayout@b3f95a10
         * System.out: p = 3--android.widget.LinearLayout@b3f95a10
         * System.out: p = 4--android.widget.LinearLayout@b3f95a10
         * System.out: p = 5--android.widget.TextView@b3ee50c0
         * System.out: p = 6--android.widget.TextView@b3ee50c0
         * System.out: p = 7--android.widget.LinearLayout@b3a905d8
         * System.out: p = 8--android.widget.LinearLayout@b3a905d8
         * 调用该方法两次： View.inflate(AppManagerActivity.this, R.layout.item_app_manager, null);
         * 即特出的view有几次，就会填充几次
         *
         * @param position
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (position == 0) {
                TextView textView = new TextView(AppManagerActivity.this);
                textView.setBackgroundColor(Color.BLACK);
                textView.setTextColor(Color.WHITE);
                textView.setText("用户程序(" + userInfos.size() + ")");
                return textView;
            } else if (position == userInfos.size() + 1) {
                TextView textView = new TextView(AppManagerActivity.this);
                textView.setBackgroundColor(Color.BLACK);
                textView.setTextColor(Color.WHITE);
                textView.setText("系统程序(" + systemInfos.size() + ")");
                return textView;
            }
            //一个app的信息
            AppInfo appInfo = null;
            if (position <= userInfos.size()) {
                appInfo = userInfos.get(position - 1);
            } else {
                appInfo = systemInfos.get(position - userInfos.size() - 2);
            }

            ViewHolder holder = null;
            //这里需要处理一下，会抛出空指针异常，如，当convertView是TextView时就会跑到else里面取出ViewHolder对象，
            // 然而这时便没有ViewHolder对象，所以会报空指针异常着那个
            //如果是concertView = TextView 或者 为null 我们就需要填充
            if (convertView != null && (convertView instanceof LinearLayout)) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(AppManagerActivity.this, R.layout.item_app_manager, null);
                holder = new ViewHolder();

                ImageView apkIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                TextView appName = (TextView) convertView.findViewById(R.id.tv_app_name);
                TextView installLocation = (TextView) convertView.findViewById(R.id.tv_install_location);
                TextView apkSize = (TextView) convertView.findViewById(R.id.tv_apk_size);

                holder.apkIcon = apkIcon;
                holder.appName = appName;
                holder.installLocation = installLocation;
                holder.apkSize = apkSize;

                convertView.setTag(holder);
            }
            //一个app的信息
            //  final AppInfo appInfo = appInfos.get(position);
            holder.apkIcon.setImageDrawable(appInfo.getApkIcon());
            holder.appName.setText(appInfo.getApkName());
            holder.apkSize.setText(Formatter.formatFileSize(AppManagerActivity.this, appInfo.getApkSize()));
            if (appInfo.isRom()) {
                holder.installLocation.setText("手机内存");
            } else {
                holder.installLocation.setText("手机SD卡");
            }

            return convertView;
        }
    }

    //封装item_app_manager布局文件的组件对象，避免多次调用findviewById方法，节省资源，静态内部类不是有外部类的强引用
    static class ViewHolder {
        ImageView apkIcon;
        TextView appName;
        TextView installLocation;
        TextView apkSize;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePopupWindwon();//解决按返回键有出错信息
    }
}
