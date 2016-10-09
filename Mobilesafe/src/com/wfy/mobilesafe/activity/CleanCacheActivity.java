package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.wfy.domain.CacheInfo;
import com.wfy.mobilesafe.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 清理缓存:
 */
public class CleanCacheActivity extends Activity {
    @ViewInject(R.id.tv_cache_size)
    private TextView tv_cache_size;
    @ViewInject(R.id.list_view)
    private ListView list_view;
    @ViewInject(R.id.ll_progressbar)
    private LinearLayout ll_progressbar;
    @ViewInject(R.id.btn_all_clean_cache)
    private Button btn_all_clean_cache;
    @ViewInject(R.id.tip)
    private TextView tip;
    private PackageManager mPM;
    private ArrayList<CacheInfo> cacheInfos;
    private CacheAdapter adapter;
    private long mTotalCacheSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_cache);
        ViewUtils.inject(this);//注入view事件
        mPM = getPackageManager();
        cacheInfos = new ArrayList<>();
        initData();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll_progressbar.setVisibility(View.GONE);
            if (cacheInfos.isEmpty()) {
                btn_all_clean_cache.setVisibility(View.GONE);
                tip.setVisibility(View.VISIBLE);
                tip.setText("没有缓存，您的手机很干净.....");
            } else {
                tip.setVisibility(View.GONE);
                btn_all_clean_cache.setVisibility(View.VISIBLE);
            }

            tv_cache_size.setText(cacheInfos.size() + " 款软件有缓存，共:"
                    + Formatter.formatFileSize(CleanCacheActivity.this, mTotalCacheSize));

            if (adapter == null) {
                adapter = new CacheAdapter();
                list_view.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };

    class CacheAdapter extends BaseAdapter {
        public CacheAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return cacheInfos.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(CleanCacheActivity.this, R.layout.item_clean_cache, null);
                ImageView iv_app_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
                TextView tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                TextView tv_cache_size = (TextView) convertView.findViewById(R.id.tv_cache_size);
                ImageView iv_clean = (ImageView) convertView.findViewById(R.id.iv_clean);

                holder.appIcon = iv_app_icon;
                holder.tv_cache_size = tv_cache_size;
                holder.tv_name = tv_name;
                holder.iv_clean = iv_clean;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.appIcon.setImageDrawable(cacheInfos.get(position).getAppIcon());
            holder.tv_name.setText(cacheInfos.get(position).getAppName());
            holder.tv_cache_size.setText(cacheInfos.get(position).getCacheSize());

            final CacheInfo cacheInfo = cacheInfos.get(position);
            holder.iv_clean.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:" + cacheInfo.getPackageName()));
                    startActivityForResult(intent, 1);
                }
            });

            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return cacheInfos.get(position);
        }
    }

    //当应用详情界面被销毁，回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        initData();//重新加载数据
    }

    static class ViewHolder {
        ImageView appIcon;
        TextView tv_name;
        TextView tv_cache_size;
        ImageView iv_clean;
    }

    private void initData() {
        /**
         * packageManager隐藏的方法
         *   @hide
         *  public abstract void getPackageSizeInfo(String packageName, int userHandle,
         *  IPackageStatsObserver observer);
         */
        mTotalCacheSize = 0;
        if (!cacheInfos.isEmpty()) {
            cacheInfos.clear();
        }
        ll_progressbar.setVisibility(View.VISIBLE);
        new Thread() {
            @Override
            public void run() {
                List<PackageInfo> installedPackages = mPM.getInstalledPackages(0);
                for (PackageInfo ps : installedPackages) {
                    getCacheSize(ps);//获取缓存大小
                }
                SystemClock.sleep(3000);
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    //通过包名获取缓存大小
    private void getCacheSize(PackageInfo packageInfo) {
        try {
            //通过反射调用PackageManager隐藏的方法
            Class<?> aClass = getClassLoader().loadClass("android.content.pm.PackageManager");
            Method getPackageSizeInfo = aClass.getDeclaredMethod("getPackageSizeInfo",
                    String.class, IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(mPM, packageInfo.applicationInfo.packageName,
                    new MyIPackageStatsObserver(packageInfo));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //包状态观察者
    class MyIPackageStatsObserver extends IPackageStatsObserver.Stub {
        private PackageInfo packageInfo;

        public MyIPackageStatsObserver(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
        }

        //获取包装的状态完成回调
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            long cacheSize = pStats.cacheSize;
            if (cacheSize > 0) {
                mTotalCacheSize += cacheSize;
                CacheInfo cacheInfo = new CacheInfo();
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(mPM);
                cacheInfo.setAppIcon(appIcon);
                String appName = packageInfo.applicationInfo.loadLabel(mPM).toString();
                cacheInfo.setAppName(appName);
                String packageName = packageInfo.applicationInfo.packageName;
                cacheInfo.setPackageName(packageName);
                cacheInfo.setCacheSize(Formatter.formatFileSize(CleanCacheActivity.this, cacheSize));
                cacheInfos.add(cacheInfo);
            }
        }
    }

    //全部清除
    public void allCleanCache(View v) {
        try {
            Class<?> aClass = getClassLoader().loadClass("android.content.pm.PackageManager");
            Method freeStorageAndNotify = aClass.getDeclaredMethod("freeStorageAndNotify",
                    long.class, IPackageDataObserver.class);
            freeStorageAndNotify.invoke(mPM, Integer.MAX_VALUE, new MyIPackageDataObserver());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //包数据观察者
    class MyIPackageDataObserver extends IPackageDataObserver.Stub {
        @Override
        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
            cacheInfos.clear();
            handler.sendEmptyMessage(0);
        }
    }
}
