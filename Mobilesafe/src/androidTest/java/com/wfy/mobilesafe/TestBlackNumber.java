package com.wfy.mobilesafe;

import android.content.Context;
import android.test.AndroidTestCase;

import com.wfy.domain.BlackNumberInfo;
import com.wfy.mobilesafe.db.dao.BlackNumberDao;

import java.util.List;
import java.util.Random;

/**
 * 测试黑名单Dao
 */
public class TestBlackNumber extends AndroidTestCase {
    private Context mContext;
    private BlackNumberDao blackNumberDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        blackNumberDao = new BlackNumberDao(mContext);
    }

    public void testAdd() {
        for (int i = 0; i < 500; i++) {
            Long number = 13300000001l + i;
            Random random = new Random();
            blackNumberDao.add(number + "", String.valueOf(random.nextInt(3) + 1));
        }
    }

    public void testDelete() {
        blackNumberDao.delete("13300000001");
    }

    public void testChangNumbrerMode() {
        blackNumberDao.changNumbrerMode("13300000003", "2");
    }

    public void testFindBlackNumber() {
        String mode = blackNumberDao.findBlackNumber("13300000003");
        System.out.println(mode);
    }

    public void testFindAllBlackNumber() {
        List<BlackNumberInfo> blackNumberInfos = blackNumberDao.findAllBlackNumber();
        System.out.println(blackNumberInfos);
    }
    public  void testFindPar(){
        List<BlackNumberInfo> par = blackNumberDao.findPar(39, 5);
       // System.out.println("par "+par.get(4).getNumber());
        System.out.println("par "+par+ "---" + blackNumberDao.getTotalPageNumber());
    }
}