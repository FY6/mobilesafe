package com.wfy.mobilesafe.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

/**
 * 获取位置的服务
 */
public class LocationService extends Service {

    private LocationManager lm;
    private MyLocationListener listener;
    private SharedPreferences mPref;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPref = getSharedPreferences("config", MODE_PRIVATE);

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);// 是否允许花费，比如4G网络
        criteria.setAccuracy(Criteria.ACCURACY_FINE);// 最佳精确度
        String bestProvider = lm.getBestProvider(criteria, true);// 获取最佳位置提供者

        listener = new MyLocationListener();
        lm.requestLocationUpdates(bestProvider, 0, 0, listener);
    }

    class MyLocationListener implements LocationListener {

        // 位置发生改变时，调用此方法
        @Override
        public void onLocationChanged(Location location) {

            String l = "j:" + location.getLongitude() + "w:"
                    + location.getLatitude();
            // 将经纬度保存在sp中
            mPref.edit().putString("location", l).commit();

            // 停掉service
            stopSelf();
        }

        // 定位设备状态发生改变时调用，即，设备从无法定位到能够定位状态改变
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("status changed");
        }

        // 用户打开GPS
        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("open GPS devices");
        }

        // 用户关闭GPS
        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("disabled GPS devices");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        lm.removeUpdates(listener);// 当服务销毁，停止位置更新，节省电量
    }

}
