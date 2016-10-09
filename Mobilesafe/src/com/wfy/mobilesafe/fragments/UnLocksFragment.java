package com.wfy.mobilesafe.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wfy.domain.AppInfo;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.db.dao.AppLocksDao;
import com.wfy.mobilesafe.utils.AppInfosUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 未加锁Fragment
 * Created by wfy on 2016/8/4.
 */
public class UnLocksFragment extends Fragment {

    private ListView list_view;
    private MyAdapter mAdapter;
    private List<AppInfo> appInfos;
    private AppLocksDao appLocksDao;
    private ArrayList<AppInfo> unLocks;
    private TextView unlockNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unlocks, null);//填充fragment布局
        unlockNum = (TextView) view.findViewById(R.id.tv_unlock_num);
        list_view = (ListView) view.findViewById(R.id.list_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        appLocksDao = new AppLocksDao(getActivity());
        appInfos = AppInfosUtils.getAppInfos(getActivity());//获取所有已经安装的应用
        unLocks = new ArrayList<>();//存放没有加锁的应用
        for (AppInfo info : appInfos) {
            //如果不在锁数据库中，就加入未加锁集合
            if (!appLocksDao.find(info.getApkPackageName())) {
                unLocks.add(info);//未加锁
            }
        }
        unlockNum.setText("未加锁(" + unLocks.size() + ")个");
        mAdapter = new MyAdapter();
        list_view.setAdapter(mAdapter);
    }

    class MyAdapter extends BaseAdapter {
        public MyAdapter() {
            super();
        }

        @Override
        public int getCount() {
            return unLocks.size();
        }

        @Override
        public Object getItem(int position) {
            return unLocks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getActivity(), R.layout.item_unlock, null);
                ImageView icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                TextView name = (TextView) convertView.findViewById(R.id.tv_name);
                ImageView iv_unlok = (ImageView) convertView.findViewById(R.id.iv_unlock);

                holder.icon = icon;
                holder.iv_unlok = iv_unlok;
                holder.name = name;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.icon.setImageDrawable(unLocks.get(position).getApkIcon());
            holder.name.setText(unLocks.get(position).getApkName());

            final View finalConvertView = convertView;
            final AppInfo appInfo = unLocks.get(position);

            holder.iv_unlok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //制作位移动画
                    TranslateAnimation translateAnimation = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, 1,
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, 0);
                    translateAnimation.setDuration(500);
                    //开启动画
                    finalConvertView.startAnimation(translateAnimation);

                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        //等待动画结束，如果不等，页面可能会出现交叉效果，效果会好一些
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //操作数据库是耗时动作，开一个子线程操作
                            new Thread() {
                                @Override
                                public void run() {
                                    //runOnUiThread是在主线程执行Rannable的run方法
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //加到锁数据库
                                            appLocksDao.add(appInfo.getApkPackageName());
                                            // 从当前的页面移除对象
                                            unLocks.remove(appInfo);
                                            // 刷新界面
                                            mAdapter.notifyDataSetChanged();
                                            unlockNum.setText("未加锁(" + unLocks.size() + ")个");
                                        }
                                    });
                                }
                            }.start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                   /* //操作数据库是耗时动作，开一个子线程操作
                    new Thread() {
                        @Override
                        public void run() {
                            SystemClock.sleep(500);
                            //runOnUiThread是在主线程执行Rannable的run方法
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //加到锁数据库
                                    appLocksDao.add(appInfo.getApkPackageName());
                                    // 从当前的页面移除对象
                                    unLocks.remove(appInfo);
                                    // 刷新界面
                                    mAdapter.notifyDataSetChanged();
                                    unlockNum.setText("未加锁(" + unLocks.size() + ")个");
                                }
                            });
                        }
                    }.start();*/

                }
            });
            return convertView;
        }
    }


    //定义ViewHolder
    static class ViewHolder {
        ImageView icon;
        TextView name;
        ImageView iv_unlok;
    }
}