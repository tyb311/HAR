package com.tyb.prolbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.tyb.service.ServiceLbs;

public class TestLBS extends AppCompatActivity {
//	private LocationClient locationClient;
	TextView txt_lbs;
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			txt_lbs.setText(msg.obj.toString());
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lbs);
		txt_lbs = findViewById(R.id.txt_lbs);
		findViewById(R.id.btn_gps).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				txt_lbs.setText("最近更新地址：\n"+ ServiceLbs.getLatestGPSInfo());
			}
		});


		startService(new Intent(this, ServiceLbs.class));
		ServiceLbs.setHandler(handler);
//		locationClient = new LocationClient(getApplicationContext());
//		locationClient.registerLocationListener(new MyLocationListener());
//		requestLocation();
	}
//
//	private void requestLocation(){
////		initLocation();
//		LocationClientOption option = new LocationClientOption();
//		option.setScanSpan(1000);
//		//强制只使用GPS进行定位，即传感器模式
//		option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
//		//设置位置可描述
//		option.setIsNeedAddress(true);
//		option.setIsNeedLocationDescribe(true);
//		option.setOpenGps(true);
//		locationClient.setLocOption(option);
//		//开始监听
//		locationClient.start();
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		locationClient.stop();
		stopService(new Intent(this, ServiceLbs.class));
	}

//
//	public class MyLocationListener implements BDLocationListener{
//		@Override
//		public void onReceiveLocation(final BDLocation bdLocation) {
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					StringBuilder curPos = new StringBuilder();
//					//经纬度
//					curPos.append("维度："+bdLocation.getLatitude()+"\n");
//					curPos.append("经度："+bdLocation.getLongitude()+"\n");
//					//详细描述
//					curPos.append("地址："+bdLocation.getAddrStr()+"\n");
//					curPos.append("描述："+bdLocation.getLocationDescribe()+"\n");
//
//					if(bdLocation.getLocType()==BDLocation.TypeGpsLocation){
//						curPos.append("GPS定位");
//					}else if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation){
//						curPos.append("网络定位");
//					}
//					txt_lbs.setText(curPos);
//				}
//			});
//		}
//	}
}
/*
					curPos.append("国家：").append(bdLocation.getCountry()).append("\n");
					curPos.append("省市：").append(bdLocation.getProvince()).append("\n");
					curPos.append("城市：").append(bdLocation.getCity()).append("\n");
					curPos.append("区域：").append(bdLocation.getDistrict()).append("\n");
					curPos.append("城镇：").append(bdLocation.getTown()).append("\n");
					curPos.append("街道："+bdLocation.getStreet()+"\n");
 */