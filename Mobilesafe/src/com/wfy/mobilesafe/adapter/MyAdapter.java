package com.wfy.mobilesafe.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by wfy on 2016/6/2.
 *
 * 我们抽取了适配器，共有的功能，因为我们在继承Adapter是只是关注getView方法
 */
public abstract class MyAdapter<T> extends BaseAdapter {
    public List<T> lists;
    public Context mContext;

    public MyAdapter(List<T> lists, Context mContext) {
        this.lists = lists;
        this.mContext = mContext;
    }

    public MyAdapter() {
        super();
    }
    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int i) {
        return lists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
}
