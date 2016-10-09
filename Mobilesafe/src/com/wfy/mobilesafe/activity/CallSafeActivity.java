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
import android.widget.AbsListView;
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
public class CallSafeActivity extends Activity {

    //拿到ListView对象
    private ListView list_view;
    //存放服务端返回的数据
    private List<BlackNumberInfo> allBlackNumber;
    //这是进度圆圈的布局，当数据加载完毕，隐藏圆圈
    private LinearLayout ll_pb;

    private int startIndex = 0;
    private int maxCount = 20;

    //这是获取数据的Dao层对象
    private BlackNumberDao blackNumberDao;
    //适配器对象
    private CallSafeAdapter adapter;
    private int totalNumberCount;
    /**
     * tvNotData:
     * 判断从数据库取回的数据allBlackNumber是否有数据，
     * 如果没有数据则显示‘暂时没有黑名单数据\n快快添加吧!’信息，提示用户添加数据
     */
    private TextView tvNotData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_safe);

        //初始化UI，集中获取UI对象，方便管理
        initUI();
        //初始化数据
        initData();
    }

    //更新UI
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //删除完成检测是否有数据，如果没有就首先是‘暂时没有黑名单数据\n快快添加吧!’
            if (allBlackNumber != null && allBlackNumber.size() > 0) {
                tvNotData.setVisibility(View.INVISIBLE);
            } else {
                tvNotData.setVisibility(View.VISIBLE);
            }
            //隐藏进度圆圈，以加载数据就展示进度圆圈，加载完成就隐藏
            ll_pb.setVisibility(View.INVISIBLE);

            if (adapter == null) {
                adapter = new CallSafeAdapter(allBlackNumber, CallSafeActivity.this);
                list_view.setAdapter(adapter);//把数据展示到listview上
            } else {
                //通知listview，（就像数据库观察者）数据已经改变，请刷新本身
                adapter.notifyDataSetChanged();
            }

        }
    };

    /**
     * adapterde notifyDataSetChanged():
     * 1、数据源没有更新，调用notifyDataSetChanged无效。
     * <p/>
     * 2、数据源更新了，但是它指向新的引用，调用notifyDataSetChanged无效。
     * <p/>
     * 3、数据源更新了，但是adpter没有收到消息通知，无法动态更新列表。
     */
    //初始化数据
    private void initData() {
        blackNumberDao = new BlackNumberDao(CallSafeActivity.this);
        //获取总的条目数
        totalNumberCount = blackNumberDao.getTotalPageNumber();
        //以加载数据就展示进度圆圈，加载完成就隐藏
        ll_pb.setVisibility(View.VISIBLE);
        //开启子线程，获取数据
        new Thread() {
            @Override
            public void run() {
                if (allBlackNumber == null) {
                    allBlackNumber = blackNumberDao.findPar2(startIndex, maxCount);
                } else {
                    allBlackNumber.addAll(blackNumberDao.findPar2(startIndex, maxCount));
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
        //ll_pb.setVisibility(View.VISIBLE);
        list_view = (ListView) findViewById(R.id.list_view);
        tvNotData = (TextView) findViewById(R.id.tv_not_data);
        //给listView设置滚动监听
        list_view.setOnScrollListener(new AbsListView.OnScrollListener() {

            /**
             *
             * @param absListView ListView对象
             * @param scrollState 滚动状态
             * 滚动状态发生改变是回调此方法，处于滚动中此方法时时调用
             * 有静止状态到滑动状态，手指按下来并滑动：1(SCROLL_STATE_TOUCH_SCROLL)触摸滚动状态
             * 由滚动状态到静止就是，正在处于滚动，是抬起手指立即调用：0(SCROLL_STATE_IDLE)闲置状态
             * 处于惯性滚动：2 (SCROLL_STATE_FLING)
             */
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING://惯性
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸滚动
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE://闲置
                        //拿到listview最后一条可见的条目位置
                        int lastVisiblePosition = list_view.getLastVisiblePosition();
                        if (lastVisiblePosition >= totalNumberCount - 1) {
                            Toast.makeText(CallSafeActivity.this, "已经到底了...", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        /**
                         * 当最后可见item的位置等于容器大小，说明需要从服务端请求数据。
                         * 加载更多的数据。 更改加载数据的开始位置startIndex
                         */
                        if (lastVisiblePosition == allBlackNumber.size() - 1) {
                            // 加载更多的数据。 更改加载数据的开始位置
                            startIndex += maxCount;
                            /**
                             * 如果开始的位置，已经大于服务器端的记录条数，那么说明已经没有数据可更新
                             */
                            if (startIndex >= totalNumberCount) {
                                Toast.makeText(getApplicationContext(),
                                        "没有更多的数据了。", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            initData();
                        }
                        break;
                }
            }

            /**
             * 正在滚动回调此方法，手指触摸到屏幕也调用此方法
             *
             * @param absListView  ListView的父类，当处于滚动状态，闯进来的就是ListView对象
             * @param firstVisibleItem
             * @param visibleItemCount
             * @param totalItemCount
             */
            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
//                System.out.println(absListView + "--" + firstVisibleItem + "--" +
//                        totalItemCount + "--" + visibleItemCount);
            }
        });
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
                view = View.inflate(CallSafeActivity.this, R.layout.item_call_safe, null);

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

            //给图片设置点击监听,删除条目
            final BlackNumberInfo info = lists.get(i);
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String number = info.getNumber();
                    //删除数据库中的数据，如果成功，则更新页面
                    boolean result = blackNumberDao.delete(number);
                    if (result) {
                        Toast.makeText(CallSafeActivity.this, "成功删除 " + number, Toast.LENGTH_SHORT).show();
                        lists.remove(info);
                        //通知list更新数据，，本人觉得这样处理并不好，只是把本页面的数据更新而已，比没有改变总页数
                        //当把本页面的数据删完，就是空白，但是这样对用户体验不好，我觉得如果第一页被删完应该调用initData方法从新从服务端获取另一页的数据
                        // ，更新数据，通知ListView更新数据，
                        if (allBlackNumber.size() <= 0) {
                            //如果数据被删除完，则从服务器从新加载一批新数据回来填充容器
                            startIndex = 0;
                            initData();
                        } else {
                            //如果容器里还有数据，则只是刷新页面
                            adapter.notifyDataSetChanged();//只是把容器中的数据更新到listview账显示
                        }
                    } else {
                        Toast.makeText(CallSafeActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                    //删除完成检测是否有数据，如果没有就首先是‘暂时没有黑名单数据\n快快添加吧!’
                    if (allBlackNumber != null && allBlackNumber.size() > 0) {
                        tvNotData.setVisibility(View.INVISIBLE);//隐藏
                    } else {
                        tvNotData.setVisibility(View.INVISIBLE);//展示
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
                    Toast.makeText(CallSafeActivity.this, "请输入黑名单", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CallSafeActivity.this, "请选择拦截模式", Toast.LENGTH_SHORT).show();
                    return;
                }

                BlackNumberInfo bnif = new BlackNumberInfo(et_number, mode);
                boolean isSuccess = blackNumberDao.add(et_number, mode);
                if (!isSuccess) {
                    Toast.makeText(CallSafeActivity.this, "添加失败，请从新添加!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(CallSafeActivity.this, "成功添加 " + bnif.getNumber(), Toast.LENGTH_SHORT).show();
                }
                allBlackNumber.add(0, bnif);
                adapter.notifyDataSetChanged();
                //添加完成检测是否有数据，如果没有就显示是‘暂时没有黑名单数据\n快快添加吧!’
                if (allBlackNumber != null && allBlackNumber.size() > 0) {
                    tvNotData.setVisibility(View.INVISIBLE);//隐藏
                } else {
                    tvNotData.setVisibility(View.VISIBLE);//展示
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
