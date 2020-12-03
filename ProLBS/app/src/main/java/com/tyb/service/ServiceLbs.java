package com.tyb.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class ServiceLbs extends Service {
	private MyLocationListener listener;
	private static LocationClient locationClient;
	private static Handler handler;
	public static void setHandler(Handler _handler){
		handler = _handler;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		requestLocation();
		return super.onStartCommand(intent, flags, startId);
	}

	private void requestLocation(){
		//监听设置
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("gcj02" );//百度地图模式bd09ll，gcj02
		option.setIgnoreKillProcess(true);

		//设置定位模式，高精度，低功耗，仅设备
//		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); //默认高精度
//		option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
		option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
		option.setIsNeedAddress(true);
		option.setIsNeedLocationDescribe(true);
//		option.setOpenGps(true);
		option.setScanSpan(1000);//大于等于1000才有效
//		//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
//		option.setOpenAutoNotifyMode();
//		//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
//		option.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
		//开始监听
		locationClient = new LocationClient(getApplicationContext());
		listener = new MyLocationListener();
		locationClient.registerLocationListener(listener);
		locationClient.setLocOption(option);
		locationClient.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		locationClient.unRegisterLocationListener(listener);
		locationClient.stop();
	}

	public static double latitude = 0;
	public static double longitude = 0;
	public static String address = "";
	public static String locdes = "";
	public static final int MSG_BAIDU = 777;
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(final BDLocation location) {
			if (null != location && location.getLocType() != BDLocation.TypeServerError) {
				Message msg = new Message();
				String gpsInfo = gpsInfoFromLocation(location);
				msg.what = MSG_BAIDU;
				msg.obj = gpsInfo;//"定位:\n"+
				handler.sendMessage(msg);
				Log.i("刷新:\n", gpsInfo);
				Log.i("类型:", location.getLocTypeDescription()+String.valueOf(location.getLocType()));
			}
		}
	}

	public static String getLatestGPSInfo(){
		BDLocation location = locationClient.getLastKnownLocation();
		return gpsInfoFromLocation(location);
	}

	private static String gpsInfoFromLocation(BDLocation location){
//		if(location.getLocationDescribe()==null)
//			return "LBS Null";
		StringBuilder curPos = new StringBuilder();
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		address = location.getAddrStr();
		locdes = location.getLocationDescribe();
		//经纬度
		curPos.append("经纬度:("+latitude+","+longitude+")\n");
		//地址描述
		curPos.append(address+"\n"+locdes);

//		if(location.getLocType()==BDLocation.TypeGpsLocation){
//			curPos.append("(GPS定位)");
//		}else if(location.getLocType()==BDLocation.TypeNetWorkLocation){
//			curPos.append("(网络定位)");
//		}
		return String.valueOf(curPos);
	}

	@Override
	public IBinder onBind(Intent intent) {
		requestLocation();
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	public ServiceLbs(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				requestLocation();
				int i=0;
				while (true){
					try {
						Thread.sleep(3000);
//						Message msg = new Message();
//						msg.obj = "Hello, Thread:"+String.valueOf(i);
//						handler.sendMessage(msg);

						locationClient.requestLocation();
//						requestLocation();
						i = (i+1)%99;
						Log.i("Service:", String.valueOf(i));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
