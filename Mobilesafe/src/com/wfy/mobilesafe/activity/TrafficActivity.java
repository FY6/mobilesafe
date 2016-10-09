package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.wfy.domain.AppInfo;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.AppInfosUtils;

import java.util.List;

/**
 * 流量统计:
 * 获取的流量手机重启就会清零
 */
public class TrafficActivity extends Activity {
    @ViewInject(R.id.tv_used_trasffic)
    private TextView tv_used_trasffic;//已使用移动数据流量
    @ViewInject(R.id.tv_total_traffic)
    private TextView tv_total_traffic;//已使用总流量
    @ViewInject(R.id.list_view)
    private ListView list_view;
    private List<AppInfo> appInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        initData();
    }

    private void initData() {
        new Thread() {
            @Override
            public void run() {
                appInfos = AppInfosUtils.getAppInfos(TrafficActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list_view.setAdapter(new TrafficAdapter());
                    }
                });
            }
        }.start();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        setContentView(R.layout.activity_traffic);
        ViewUtils.inject(this);
        long totalTraffic = getTotalTraffic();//获取已使用的总流量
        long mobileTraffic = getMobileTraffic();//获得已使用总的移动数据
        long appTraffic = getAppTraffic(1000);//获取指定应用使用的总流量

        tv_total_traffic.setText("已使用总流量: " + Formatter.formatFileSize(this, totalTraffic));
        tv_used_trasffic.setText("已使用移动总可流量: " + Formatter.formatFileSize(this, mobileTraffic));
    }

    /**
     * 获取指定应用使用的总流量
     *
     * @return
     */
    private long getAppTraffic(int uid) {
        long uidRxBytes = TrafficStats.getUidRxBytes(uid);//指定应用的下载流量
        long uidTxBytes = TrafficStats.getUidTxBytes(uid);//指定应用的上传流量
        long appTotalTraffic = uidRxBytes + uidTxBytes;
        if (appTotalTraffic < 0) {
            appTotalTraffic = 0;
        }
        return appTotalTraffic;
    }

    /**
     * 获得已使用总的移动数据
     *
     * @return
     */
    private long getMobileTraffic() {
        long mobileRxBytes = TrafficStats.getMobileRxBytes();//下载3G流量
        long mobileTxBytes = TrafficStats.getMobileTxBytes();//上传3G流量
        long mobileTraffic = mobileRxBytes + mobileTxBytes;

        if (mobileTraffic < 0) {
            mobileTraffic = 0;
        }
        return mobileTraffic;
    }

    /**
     * 获取已使用的总流量
     *
     * @return
     */
    private long getTotalTraffic() {
        long totalRxBytes = TrafficStats.getTotalRxBytes();//下载总流量 wifi+3G
        long totalTxBytes = TrafficStats.getTotalTxBytes();//上传总流量 wifi+3G
        long totalTraffic = totalRxBytes + totalTxBytes;
        if (totalTraffic < 0) {
            totalTraffic = 0;
        }
        return totalTraffic;
    }

    //适配器
    class TrafficAdapter extends BaseAdapter {
        public TrafficAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return appInfos.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return appInfos.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(TrafficActivity.this, R.layout.item_traffic_listview, null);
                ImageView iv_app_icno = (ImageView) convertView.findViewById(R.id.iv_app_icno);
                TextView tv_app_name = (TextView) convertView.findViewById(R.id.tv_app_name);
                TextView tv_mobile_traffic = (TextView) convertView.findViewById(R.id.tv_mobile_traffic);

                holder.iv_app_icno = iv_app_icno;
                holder.tv_app_name = tv_app_name;
                holder.tv_mobile_traffic = tv_mobile_traffic;

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PackageManager mPM = getPackageManager();
            String apkPackageName = appInfos.get(position).getApkPackageName();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = mPM.getApplicationInfo(apkPackageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


            System.out.println(getAppTraffic(applicationInfo.uid) + " --- " + applicationInfo.uid);
            holder.iv_app_icno.setImageDrawable(appInfos.get(position).getApkIcon());
            holder.tv_app_name.setText(appInfos.get(position).getApkName());
            holder.tv_mobile_traffic.setText("已使用流量: " +
                    Formatter.formatFileSize(TrafficActivity.this, getAppTraffic(applicationInfo.uid)));

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView iv_app_icno;
        TextView tv_app_name;
        TextView tv_mobile_traffic;
    }

}
