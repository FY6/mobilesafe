package com.wfy.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.wfy.mobilesafe.R;
import com.wfy.mobilesafe.db.dao.CommonNumberDao;

import java.util.List;

/**
 * 常用号码查询
 */
public class CommonNumberActivity extends Activity {
    @ViewInject(R.id.expand_lisview)
    private ExpandableListView mExpandListView;
    private List<CommonNumberDao.GroupInfo> mommonNumberGroup;
    private MyExpandListViewAdapter adapter;
    private static MyHandler mHandler;

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (adapter == null) {
                adapter = new MyExpandListViewAdapter();
                mExpandListView.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler();
        initUI();
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        new Thread() {
            @Override
            public void run() {
                mommonNumberGroup = CommonNumberDao.getCommonNumberGroup();

                Message msg = new Message();
                msg.what = 0;
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        setContentView(R.layout.activity_common_number);
        ViewUtils.inject(this);

        //设置孩子的监听
        mExpandListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                CommonNumberDao.ChildInfo childInfo =
                        mommonNumberGroup.get(groupPosition).childInfos.get(childPosition);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + childInfo.number));

                startActivity(intent);
                return true;
            }
        });
    }

    class MyExpandListViewAdapter extends BaseExpandableListAdapter {
        public MyExpandListViewAdapter() {
            super();
        }

        @Override
        public int getGroupCount() {
            return mommonNumberGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mommonNumberGroup.get(groupPosition).childInfos.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mommonNumberGroup.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mommonNumberGroup.get(groupPosition).childInfos.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            TextView textView = new TextView(CommonNumberActivity.this);
            textView.setTextSize(22);
            textView.setTextColor(Color.parseColor("#817a1e"));
            //#RRGGBB
            //#AARRGGBB
            textView.setBackgroundColor(Color.parseColor("#5bd998"));
            CommonNumberDao.GroupInfo groupInfo = mommonNumberGroup.get(groupPosition);
            textView.setText("       " + groupInfo.name);
            return textView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {

            CommonNumberDao.ChildInfo childInfo =
                    mommonNumberGroup.get(groupPosition).childInfos.get(childPosition);
            View view = View.inflate(CommonNumberActivity.this, R.layout.item_comcon_number, null);
            TextView name = (TextView) view.findViewById(R.id.tv_comcon_name);
            TextView num = (TextView) view.findViewById(R.id.tv_comcon_num);
            name.setText(childInfo.name);
            num.setText(childInfo.number);
            return view;
        }

        /**
         * 收缩调用此方法
         *
         * @param groupPosition
         */
        @Override
        public void onGroupCollapsed(int groupPosition) {
            System.out.println("GroupCollapsed" + groupPosition);
        }

        /**
         * 展开是调用此方法
         *
         * @param groupPosition
         */
        @Override
        public void onGroupExpanded(int groupPosition) {
            System.out.println("GroupExpanded" + groupPosition);
        }
    }
}
