package com.wfy.mobilesafe;

import android.test.AndroidTestCase;

import com.wfy.mobilesafe.db.dao.AppLocksDao;

/**
 * Created by wfy on 2016/8/5.
 */
public class TestAppLocks extends AndroidTestCase {

    private AppLocksDao dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dao = new AppLocksDao(getContext());
    }

    public void testAdd() {
        dao.add("hahha");
    }

    public void testFind() {
        System.out.println(dao.find("hahha"));
    }

    public void testdelete() {
        System.out.println(dao.deleteAll());
    }

    public void testAll() {
        System.out.println("---" + dao.getAll());
    }
}
