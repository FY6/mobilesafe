package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.wfy.domain.TaskInfo;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.TaskInfoParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 进程管理
 */

public class TaskManagerActivity extends Activity {
    @ViewInject(R.id.tv_task_num)
    private TextView tv_task_num;//进程数
    @ViewInject(R.id.tv_total_mem)
    private TextView tv_total_mem;//剩余/总内存
    @ViewInject(R.id.list_view)
    private ListView list_view;
    @ViewInject(R.id.tv_task_title)
    private TextView tv_task_title;
    private ActivityManager activityManager;
    private List<TaskInfo> taskInfos;//所有的进程信息
    private TaskManagerAdapter adapter;
    private List<TaskInfo> userApp;//用户进程信息
    private List<TaskInfo> systemApp;//系统进程信息
    private int size;//总进程数
    private long availMem;//可用内存
    private long totalMem;//总内存
    private SharedPreferences mPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPref = getSharedPreferences("config", MODE_PRIVATE);
        initUI();//初始化UI
        initData();//初始化数据
    }

    //全选、反选、一键清除、设置 按钮点击监听
    public void click(View v) {
        switch (v.getId()) {
            case R.id.btn_select_all://全选
                selectAll();
                break;
            case R.id.btn_select_back://反选
                selectBack();
                break;
            case R.id.btn_clear://一键清除，杀死后台进程
                killProcess();
                break;
            case R.id.btn_setting://设置
                setting();
                break;
        }
    }

    /**
     * 设置
     */
    private void setting() {
        Intent intent = new Intent(this, TaskManagerSettingActivity.class);
        startActivityForResult(intent, 1);
    }

    //当TaskManagerSettingActivity销毁后，回调此方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();//刷新界面，，重新调用getCount方法
        }
    }

    /**
     * 一键清除进程
     */
    private void killProcess() {

        //如果出了自己以外没有进程了，就不做任何操作
        if (systemApp.isEmpty() && (userApp.size() - 1) == 0) {
            return;
        }

        //拿到进程管理器
        ActivityManager as = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        int killCount = 0;
        long menorySize = 0;

        ListIterator<TaskInfo> iterator = userApp.listIterator();
        while (iterator.hasNext()) {
            TaskInfo taskInfo = iterator.next();
            if (taskInfo.isChecked()) {
                killCount++;
                String appName = taskInfo.getPackageName();
                menorySize += taskInfo.getMemorySize();
                //杀死后台进程,需要加 android.permission.KILL_BACKGROUND_PROCESSES 权限
                as.killBackgroundProcesses(appName);
                iterator.remove();//从列表中移除由 next 或 previous 返回的最后一个元素（可选操作）。
            }
        }

        //允许在迭代期间修改列表
        ListIterator<TaskInfo> taskInfoListIterator = systemApp.listIterator();
        while (taskInfoListIterator.hasNext()) {
            TaskInfo next = taskInfoListIterator.next();
            if (next.isChecked()) {
                killCount++;
                String appName = next.getPackageName();
                menorySize += next.getMemorySize();
                //杀死后台进程,需要加 android.permission.KILL_BACKGROUND_PROCESSES 权限
                as.killBackgroundProcesses(appName);
                taskInfoListIterator.remove();//从列表中移除由 next 或 previous 返回的最后一个元素（可选操作）。
            }
        }

        size -= killCount;
        tv_task_num.setText("运行进程: " + size + "个");
        availMem += menorySize;
        tv_total_mem.setText("剩余/总内存:" + Formatter.formatFileSize(this, availMem) + "/" +
                Formatter.formatFileSize(this, totalMem));


        adapter.notifyDataSetChanged();//刷新UI
        String s = Formatter.formatFileSize(TaskManagerActivity.this, menorySize);//格式化内存文件
        Toast.makeText(TaskManagerActivity.this, "杀死" + killCount + "个进程释放了" + s, Toast.LENGTH_SHORT).show();
    }

    /**
     * 全选
     */
    private void selectAll() {
        boolean isChanged = false;//标志是否有改变，如果有就刷新，没有就不做刷新操作
        for (TaskInfo utf : userApp) {
            if (!utf.isChecked()) {
                utf.setChecked(true);
                isChanged = true;
            }
        }
        for (TaskInfo stf : systemApp) {
            if (!stf.isChecked()) {
                stf.setChecked(true);
                isChanged = true;
            }
        }
        if (isChanged) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 反选
     */
    private void selectBack() {
        for (TaskInfo utf : userApp) {
            utf.setChecked(!utf.isChecked());
        }
        for (TaskInfo stf : systemApp) {
            stf.setChecked(!stf.isChecked());
        }
        if (!userApp.isEmpty() && !systemApp.isEmpty()) {
            adapter.notifyDataSetChanged();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (adapter == null) {
                adapter = new TaskManagerAdapter();
                list_view.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };

    //初始化数据
    private void initData() {
        userApp = new ArrayList<>();
        systemApp = new ArrayList<>();
        //开启新线程获取数据
        new Thread() {
            @Override
            public void run() {
                TaskManagerActivity.this.taskInfos = TaskInfoParser.getTaskInfos(TaskManagerActivity.this);
                //把数据分类（用户进程、系统进程）
                for (TaskInfo task : TaskManagerActivity.this.taskInfos) {
                    if (task.isUserApp()) {
                        userApp.add(task);
                    } else {
                        systemApp.add(task);
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * ActivityManager：活动管理器（任务管理器或进程管理器）
     * PackageManager：包管理器
     */
    //初始化UI
    private void initUI() {
        setContentView(R.layout.activity_task_manager);
        ViewUtils.inject(this);
        //得到进程管理者
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //拿到当前手机上所有正在运行中的进程
        size = getRunningAppProcCount();
        tv_task_num.setText("运行进程: " + size + "个");

        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        //拿到剩余的内存大小
        availMem = getAvailMem(outInfo);
        //拿到总内存
        totalMem = getTotalMem(outInfo);
        tv_total_mem.setText("剩余/总内存:" + Formatter.formatFileSize(this, availMem) + "/" +
                Formatter.formatFileSize(this, totalMem));


        //给ListView设置滚动监听
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userApp != null && systemApp != null) {
                    if (firstVisibleItem >= userApp.size() + 1) {
                        tv_task_title.setText("系统应用:(" + systemApp.size() + ")个");
                    } else {
                        tv_task_title.setText("用户应用:(" + userApp.size() + ")个");
                    }
                }
            }
        });
        //给listview设置点击监听
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /***
             *
             * @param parent 就是ListView
             * @param view 被点击的item，其中这里item是一个布局
             * @param position 被点击的几个条目
             * @param id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskInfo taskInfo = (TaskInfo) parent.getItemAtPosition(position);

                if (taskInfo != null) {

                    //避免把自己的进程杀死,getPackageName()是Context的方法返回去应用的包com.wfy.mobilesafe
                    if (getPackageName().equals(taskInfo.getPackageName())) {
                        return;
                    }

//                    if (taskInfo.isChecked()) {
//                        taskInfo.setChecked(false);
//                    } else {
//                        taskInfo.setChecked(true);
//                    }
                    //adapter.notifyDataSetChanged();//全局更新，性能不好；

                    boolean isChecked = !taskInfo.isChecked();
                    CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox);
                    cb.setChecked(isChecked);
                    taskInfo.setChecked(isChecked);
                }
            }
        });
    }

    /**
     * @param outInfo 内存信息对象
     * @return 获取总的内存大小
     */
    private long getTotalMem(ActivityManager.MemoryInfo outInfo) {
        long totalMem = 0;
        //这是新API ，最小需要API 16才可以用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//判断当前版本是否大于等于16
            totalMem = outInfo.totalMem;
        } else {
            totalMem = getLowTotalMem();
        }
        return totalMem;
    }

    /**
     * @param outInfo 内存的信息
     * @return 返回剩余的内存
     */
    private long getAvailMem(ActivityManager.MemoryInfo outInfo) {
        activityManager.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }

    /**
     * @return 当前手机上所有正在运行中的进程数
     */
    private int getRunningAppProcCount() {
        //拿到当前手机上所有正在运行中的进程
        List<ActivityManager.RunningAppProcessInfo> appTasks = activityManager.getRunningAppProcesses();
        return appTasks.size();
    }

    /**
     * 为了兼容低版本，我们通过读取配置文件meminfo中的内容，拿到总内存totalMem
     *
     * @return 总的内存
     */
    private long getLowTotalMem() {
        File file = new File("/proc/meminfo");
        StringBuffer sb = new StringBuffer("0");
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line = br.readLine();
            char[] chars = line.toCharArray();

            for (char c : chars) {
                if (c >= '0' && c <= '9') {
                    sb.append(c);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.parseLong(sb.toString()) * 1024;
    }

    /**
     * 适配器
     */
    class TaskManagerAdapter extends BaseAdapter {
        public TaskManagerAdapter() {
            super();
        }

        @Override
        public int getCount() {
            boolean show_sys = mPref.getBoolean("show_sys", true);
            if (show_sys) {
                return userApp.size() + systemApp.size() + 2;
            } else {
                return userApp.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            TaskInfo ti = null;
            if (position == 0 || position == userApp.size() + 1) {
                return null;
            }
            if (position <= userApp.size()) {
                ti = userApp.get(position - 1);
            } else {
                ti = systemApp.get(position - 2 - userApp.size());
            }
            return ti;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (position == 0) {
                TextView textView = new TextView(TaskManagerActivity.this);
                textView.setText("用户应用：(" + userApp.size() + ") 个");
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(Color.BLACK);
                return textView;
            }
            if (position == userApp.size() + 1) {
                TextView textView = new TextView(TaskManagerActivity.this);
                textView.setText("系统应用：(" + systemApp.size() + ") 个");
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(Color.BLACK);
                return textView;
            }
            if (convertView == null || (convertView instanceof TextView)) {
                holder = new ViewHolder();
                convertView = View.inflate(TaskManagerActivity.this, R.layout.item_task_manager, null);
                ImageView iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                TextView tv_proc_name = (TextView) convertView.findViewById(R.id.tv_proc_name);
                TextView tv_memory_size = (TextView) convertView.findViewById(R.id.tv_memory_size);
                CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox);

                holder.icon = iv_icon;
                holder.memorySize = tv_memory_size;
                holder.procName = tv_proc_name;
                holder.cb = cb;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TaskInfo ti = null;
            if (position <= userApp.size()) {
                ti = userApp.get(position - 1);
            } else {
                ti = systemApp.get(position - 2 - userApp.size());
            }
            //修改checkbox状态，并保存至TaskInfo中，TaskInfo对应着一条item
            if (ti != null && ti.isChecked()) {
                ti.setChecked(true);
            } else {
                ti.setChecked(false);
            }
            //避免把自己的进程杀死,getPackageName()是Context的方法返回去应用的包com.wfy.mobilesafe
            if (getPackageName().equals(ti.getPackageName())) {
                holder.cb.setVisibility(View.INVISIBLE);
                ti.setChecked(false);
            }


            holder.cb.setChecked(ti.isChecked());
            holder.icon.setImageDrawable(ti.getIcon());
            holder.procName.setText(ti.getAppName());
            holder.memorySize.setText("内存占用：" + Formatter.formatFileSize(TaskManagerActivity.this, ti.getMemorySize()));
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView icon;//进程图标
        TextView procName;//进程名称
        TextView memorySize;//进程占用的内存大小
        CheckBox cb;//选中状态
    }
}
