package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wfy.domain.BlackNumberInfo;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.adapter.MyAdapter;
import com.wfy.mobilesafe.db.dao.BlackNumberDao;

import java.util.List;

//这是拦截的分页实现
public class CallSafeActivity2 extends Activity {

    //拿到ListView对象
    private ListView list_view;
    //存放服务端返回的数据
    private List<BlackNumberInfo> allBlackNumber;
    //这是进度圆圈的布局，当数据加载完毕，隐藏圆圈
    private LinearLayout ll_pb;

    //当前页码
    private int currentPageNum = 0;
    //一页显示20条数据
    private int pageSize = 20;
    //总的记录数，页数
    private int totalPageNumber;
    //定义跳转，需要的变量
    private TextView tv_page_number;
    private EditText et_page_jumb;
    //这是获取数据的Dao层对象
    private BlackNumberDao blackNumberDao;
    //适配器对象
    private CallSafeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_safe2);

        //初始化UI，集中获取UI对象，方便管理
        initUI();
        //初始化数据
        initData();
    }

    //更新UI
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //隐藏进度圆圈
            ll_pb.setVisibility(View.INVISIBLE);
            tv_page_number.setText((currentPageNum + 1) + "/" + totalPageNumber);
            adapter = new CallSafeAdapter(allBlackNumber, CallSafeActivity2.this);
            list_view.setAdapter(adapter);//把数据展示到listview上
        }
    };

    //初始化数据
    private void initData() {
        //开启子线程，获取数据
        new Thread() {
            @Override
            public void run() {
                blackNumberDao = new BlackNumberDao(CallSafeActivity2.this);
                //allBlackNumber = blackNumberDao.findAllBlackNumber();
                allBlackNumber = blackNumberDao.findPar(currentPageNum, pageSize);

                //拿到总的记录数，在计算总的页数
                totalPageNumber = blackNumberDao.getTotalPageNumber();
                if (totalPageNumber % pageSize != 0) {
                    totalPageNumber = (totalPageNumber / pageSize) + 1;
                } else {
                    totalPageNumber = totalPageNumber / pageSize;
                }

                //获取数据完成，发消息，让UI线程更新UI
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    //初始化UI
    private void initUI() {
        ll_pb = (LinearLayout) findViewById(R.id.ll_pb);
        //展示进度圆圈
        ll_pb.setVisibility(View.VISIBLE);
        list_view = (ListView) findViewById(R.id.list_view);
        //跳转需要的UI对象
        tv_page_number = (TextView) findViewById(R.id.tv_page_number);
        et_page_jumb = (EditText) findViewById(R.id.et_page_jumb);
    }

    //适配器
    private class CallSafeAdapter extends MyAdapter<BlackNumberInfo> {
        public CallSafeAdapter() {
            super();
        }

        public CallSafeAdapter(List<BlackNumberInfo> lists, Context mContext) {
            super(lists, mContext);
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            ViewHolder holder = null;
            if (view == null) {
                //填充view对象
                view = View.inflate(CallSafeActivity2.this, R.layout.item_call_safe, null);

                TextView tvNumber = (TextView) view.findViewById(R.id.tv_number);
                TextView tvMode = (TextView) view.findViewById(R.id.tv_mode);
                ImageView ivDelete = (ImageView) view.findViewById(R.id.iv_delete);

                //把view中所有的子组件都封装到ViewHolder中，并保存到每一个view中
                holder = new ViewHolder();
                holder.tvNumber = tvNumber;
                holder.tvMode = tvMode;
                holder.ivDelete = ivDelete;

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.tvNumber.setText(lists.get(i).getNumber());
            /**
             * mode:
             *  1: 电话拦截
             *  2: 短信拦截
             *  3: 电话拦截+短信拦截
             */
            switch (lists.get(i).getMode()) {
                case "1":
                    holder.tvMode.setText("电话拦截");
                    break;
                case "2":
                    holder.tvMode.setText("短信拦截");
                    break;
                case "3":
                    holder.tvMode.setText("电话拦截+短信拦截");
                    break;
            }

            //给图片设置点击监听
            final BlackNumberInfo info = lists.get(i);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String number = info.getNumber();
                    boolean result = blackNumberDao.delete(number);
                    if (result) {
                        Toast.makeText(CallSafeActivity2.this, "删除成功", Toast.LENGTH_SHORT).show();
                        lists.remove(info);
                        //通知list更新数据，，本人觉得这样处理并不好，只是把本页面的数据更新而已，比没有改变总页数
                        //当把本页面的数据删完，就是空白，但是这样对用户体验不好，我觉得应该调用initData方法从新从服务端获取另一页的数据
                        // ，更新数据
                        adapter.notifyDataSetChanged();
                        // initData();//本人推荐使用
                    } else {
                        Toast.makeText(CallSafeActivity2.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return view;
        }

    }

    static class ViewHolder {
        TextView tvNumber;
        TextView tvMode;
        ImageView ivDelete;
    }

    /**
     * 下一页
     *
     * @param v
     */
    public void nextPage(View v) {
        if (currentPageNum >= totalPageNumber - 1) {
            Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            return;
        }
        System.out.println(currentPageNum + ";total = " + totalPageNumber);

        currentPageNum++;
        initData();
    }

    /**
     * 上一页
     *
     * @param v
     */
    public void prePage(View v) {
        if (currentPageNum <= 0) {
            Toast.makeText(this, "已经是第一页了", Toast.LENGTH_SHORT).show();
            return;
        }
        currentPageNum--;
        initData();
    }

    /**
     * 跳转到指定页
     *
     * @param v
     */
    public void jumb(View v) {

        if (!TextUtils.isEmpty(et_page_jumb.getText().toString().trim())) {
            int pageNum = Integer.parseInt(et_page_jumb.getText().toString().trim());
            if (pageNum <= 0 || pageNum > totalPageNumber) {
                Toast.makeText(this, "请输入正确页码", Toast.LENGTH_SHORT).show();
                return;
            }
            currentPageNum = pageNum - 1;
            initData();
        } else {
            Toast.makeText(CallSafeActivity2.this, "请输入页码", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 添加黑名单
     */
    public void addBlackNum(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final View inflate = View.inflate(this, R.layout.dialog_add_blacknum, null);
        dialog.setView(inflate, 0, 0, 0, 0);//版本兼容

        //拿到布局中的按钮
        Button btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        Button btn_ok = (Button) inflate.findViewById(R.id.btn_OK);
        //为按钮设置监听器
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //拿到组件对象
                EditText et_add_num = (EditText) inflate.findViewById(R.id.et_add_num);
                CheckBox cb_call = (CheckBox) inflate.findViewById(R.id.cb_call);
                CheckBox cb_sms = (CheckBox) inflate.findViewById(R.id.cb_sms);

                String et_number = et_add_num.getText().toString().trim();
                if (TextUtils.isEmpty(et_number)) {
                    Toast.makeText(CallSafeActivity2.this, "请输入黑名单", Toast.LENGTH_SHORT).show();
                    return;
                }
                String mode = "";
                if (cb_call.isChecked() && cb_sms.isChecked()) {
                    mode = "3";//全部拦截
                } else if (cb_call.isChecked()) {
                    mode = "1";//电话拦截
                } else if (cb_sms.isChecked()) {
                    mode = "2";//短信拦截
                } else {
                    Toast.makeText(CallSafeActivity2.this, "请选择拦截模式", Toast.LENGTH_SHORT).show();
                    return;
                }

                BlackNumberInfo bnif = new BlackNumberInfo(et_number, mode);
                blackNumberDao.add(et_number, mode);
                allBlackNumber.add(0, bnif);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
