package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.utils.MD5Utils;

/**
 * 主页面，以 九宫格布局
 *
 * @author wfy
 */
public class HomeActivity extends Activity {

    private GridView gvHome;

    // GridView 需要的数据
    private String[] mItems = new String[]{"手机防盗", "通讯卫士", "软件管理", "进程管理",
            "流量统计", "手机杀毒", "缓存管理", "高级工具", "设置中心"};
    // 图片的ID
    private int[] mPics = new int[]{R.drawable.home_safe,
            R.drawable.home_callmsgsafe, R.drawable.home_apps,
            R.drawable.home_taskmanager, R.drawable.home_netmanager,
            R.drawable.home_trojan, R.drawable.home_sysoptimize,
            R.drawable.home_tools, R.drawable.home_settings};

    //用于保存简单的数据
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //拿到SharedPreferrences
        mPref = getSharedPreferences("config", MODE_PRIVATE);
        gvHome = (GridView) findViewById(R.id.gv_home);//拿到GridView对象
        gvHome.setAdapter(new HomeAdapter());//GridView设置数据适配器

        // 为GridView的每个条目设置点击监听
        gvHome.setOnItemClickListener(new OnItemClickListener() {
            /**
             * 当点击条目时，回调次方法
             * @param parent 该条目的父视图，这里的父视图就是GridView对象
             * @param view 被点击的view对象
             * @param position 在GridView中处于第几个位置，从0开始
             * @param id 条目的id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch (position) {
                    case 0:// 手机防盗
                        showPasswordDialog();
                        break;
                    case 1://通讯卫士
                        startActivity(new Intent(HomeActivity.this,
                                CallSafeActivity.class));
                        break;
                    case 2://软件管理
                        startActivity(new Intent(HomeActivity.this,
                                AppManagerActivity.class));
                        break;
                    case 3://进程管理
                        startActivity(new Intent(HomeActivity.this,
                                TaskManagerActivity.class));
                        break;
                    case 4://流量统计
                        startActivity(new Intent(HomeActivity.this,
                                TrafficActivity.class));
                        break;
                    case 5://手机杀毒
                        startActivity(new Intent(HomeActivity.this,
                                AntivirusActivity.class));
                        break;
                    case 6://缓存管理
                        startActivity(new Intent(HomeActivity.this,
                                CleanCacheActivity.class));
                        break;
                    case 7:// 高级工具
                        startActivity(new Intent(HomeActivity.this,
                                AToolsActivity.class));
                        break;
                    case 8:// 设置中心
                        startActivity(new Intent(HomeActivity.this,
                                SettingActivity.class));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 设置密码弹窗
     */
    protected void showPasswordDialog() {

        String savedPassword = mPref.getString("password", null);
        // 判断是否设置过密码,,当第一次进来时，就是还没设置密码，就跳到设置密码对话框
        if (!TextUtils.isEmpty(savedPassword)) {
            showPasswordInputDialog();
        } else {
            // 如果没有设置过密码，就弹出设置密码弹窗
            showPasswordSetDialog();
        }
    }

    /**
     * 输入密码弹窗
     */
    private void showPasswordInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.dialog_input_password, null);
        dialog.setView(view, 0, 0, 0, 0);//考虑到版本兼容问题

        // 拿到两个输入框对象
        final EditText etPassword = (EditText) view
                .findViewById(R.id.et_password);
        // 拿到button对象
        Button btOK = (Button) view.findViewById(R.id.bt_OK);
        Button btCancel = (Button) view.findViewById(R.id.bt_cancel);
        // 为btOK设置监听
        btOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                String savedPassword = mPref.getString("password", null);

                if (!TextUtils.isEmpty(password)) {
                    if (MD5Utils.encode(password).equals(savedPassword)) {
                        Toast.makeText(HomeActivity.this, "登陆成功",
                                Toast.LENGTH_SHORT).show();

                        dialog.dismiss();

                        // 跳转到手机防盗页面
                        startActivity(new Intent(HomeActivity.this,
                                LostFindActivity.class));
                    } else {
                        Toast.makeText(HomeActivity.this, "密码错误，请重新输入",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "输入框内容不能为空",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 点击取消则隐藏窗体
        btCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();// 隐藏窗体
            }
        });
        // 显示对话框
        dialog.show();
    }

    /**
     * 设置密码弹窗
     */
    private void showPasswordSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        // 用布局文件填充view对象
        final View view = View
                .inflate(this, R.layout.dialog_set_password, null);
        // dialog.setView(view);//将自定义布局文件设置给dialog
        dialog.setView(view, 0, 0, 0, 0);// 设置边距，保证版本2.x不出现问题
        // 拿到两个输入框对象
        final EditText etPassword = (EditText) view
                .findViewById(R.id.et_password);
        final EditText etPasswordConfirm = (EditText) view
                .findViewById(R.id.et_password_confirm);
        // 拿到button对象
        Button btOK = (Button) view.findViewById(R.id.bt_OK);
        Button btCancel = (Button) view.findViewById(R.id.bt_cancel);
        // 为btOK设置监听
        btOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                String passwordConfirm = etPasswordConfirm.getText().toString()
                        .trim();
                // 判断密码是否为空，如果为空则弹吐司告知用户
                if (!TextUtils.isEmpty(password)
                        && !TextUtils.isEmpty(passwordConfirm)) {

                    if (password.equals(passwordConfirm)) {
                        Toast.makeText(HomeActivity.this, "登陆成功",
                                Toast.LENGTH_SHORT).show();
                        // 将密码保存起来
                        mPref.edit()
                                .putString("password",
                                        MD5Utils.encode(password)).commit();
                        dialog.dismiss();

                        // 跳转到手机防盗页面
                        startActivity(new Intent(HomeActivity.this,
                                LostFindActivity.class));

                    } else {
                        Toast.makeText(HomeActivity.this, "两次密码不一致，请重新输入",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "输入框内容不能为空",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 点击取消则隐藏窗体
        btCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();// 隐藏窗体
            }
        });
        // 显示弹窗
        dialog.show();
    }

    // 适配器
    class HomeAdapter extends BaseAdapter {

        // GridView 条目数量
        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 优化
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(HomeActivity.this,
                        R.layout.home_list_item, null);
                ImageView iv_item = (ImageView) convertView
                        .findViewById(R.id.iv_item);
                TextView tv_item = (TextView) convertView
                        .findViewById(R.id.tv_item);
                // 减少findViewById方法的调用次数
                holder = new ViewHolder();
                holder.iv_item = iv_item;
                holder.tv_item = tv_item;

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 修改内容
            holder.tv_item.setText(mItems[position]);
            holder.iv_item.setImageResource(mPics[position]);

            return convertView;
        }
    }

    // 封装组件
    static class ViewHolder {
        ImageView iv_item;
        TextView tv_item;
    }

}
