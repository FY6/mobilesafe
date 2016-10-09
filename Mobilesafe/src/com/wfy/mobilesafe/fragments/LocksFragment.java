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
 * 加锁Fragment
 */
public class LocksFragment extends Fragment {

    private ListView list_view;
    private List<AppInfo> appInfos;
    private AppLocksDao dao;
    private List<AppInfo> locks;
    private LocksAdapter locksAdapter;
    private TextView lockNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //填充fragment布局
        View view = inflater.inflate(R.layout.fragment_locks, null);
        lockNum = (TextView) view.findViewById(R.id.tv_lock_num);
        list_view = (ListView) view.findViewById(R.id.list_view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //注意调用getActivity()方法，只有当调用onActivityCreateed()完成调用时，才能拿到activity对象不然为null
        dao = new AppLocksDao(getActivity());
        locks = new ArrayList<>();
        appInfos = AppInfosUtils.getAppInfos(getActivity());

        for (AppInfo info : appInfos) {
            if (dao.find(info.getApkPackageName())) {
                locks.add(info);
            }
        }
        lockNum.setText("已加锁(" + locks.size() + ")个");
        locksAdapter = new LocksAdapter();
        list_view.setAdapter(locksAdapter);
    }

    //适配器
    class LocksAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return locks.size();
        }

        @Override
        public Object getItem(int position) {
            return locks.get(position);
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
                convertView = View.inflate(getActivity(), R.layout.item_lock, null);
                ImageView icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                TextView name = (TextView) convertView.findViewById(R.id.tv_name);
                ImageView lock = (ImageView) convertView.findViewById(R.id.iv_lock);

                holder.icon = icon;
                holder.lock = lock;
                holder.name = name;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.name.setText(locks.get(position).getApkName());
            holder.icon.setImageDrawable(locks.get(position).getApkIcon());

            final View finalConvertView = convertView;
            final AppInfo appInfo = locks.get(position);

            holder.lock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //制作位移动画
                    TranslateAnimation translateAnimation = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, -1,
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, 0);
                    translateAnimation.setDuration(500);
                    //开启动画
                    finalConvertView.startAnimation(translateAnimation);


                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        //等待动画结束，才从listview中移除item，不然可能会出现跳跃效果
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //开启子线程，执行耗时操作
                            new Thread() {
                                @Override
                                public void run() {
                                    //runOnUiThread方法是在主线程中执行Rannable的run方法
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //从加锁数据库中移除
                                            dao.delete(appInfo.getApkPackageName());
                                            //从当前页面移除对象
                                            locks.remove(appInfo);
                                            //刷新当前页面
                                            locksAdapter.notifyDataSetChanged();

                                            lockNum.setText("已加锁(" + locks.size() + ")个");
                                        }
                                    });
                                }
                            }.start();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    /*//开启子线程，执行耗时操作
                    new Thread() {
                        @Override
                        public void run() {
                            SystemClock.sleep(500);//等待动画结束，才从listview中移除item，不然可能会出现跳跃效果
                            //runOnUiThread方法是在主线程中执行Rannable的run方法
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //从加锁数据库中移除
                                    dao.delete(appInfo.getApkPackageName());
                                    //从当前页面移除对象
                                    locks.remove(appInfo);
                                    //刷新当前页面
                                    locksAdapter.notifyDataSetChanged();

                                    lockNum.setText("已加锁(" + locks.size() + ")个");
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
        ImageView lock;
    }
}
