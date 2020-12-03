package com.tyb.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;

import java.util.LinkedList;
import java.util.List;

public class ServiceAcc extends Service {
	private SensorManager sensorManager;
	private Sensor sensor;//加速度计
	public static List<float[]> values;

	public ServiceAcc() {
		values = new LinkedList<float[]>();
	}

	public static Handler handler;
	public static void setHandler(Handler _handler){
		handler = _handler;
	}

	public static float[] data;
	public static float[] getBatch(boolean rand){
		data = new float[3*ServiceAcc.SIZE_CACHE];
		if(rand){
			for(int i=0;i<data.length;i++)
				data[i] = (float) Math.random()/2;
		}else{
			for(int i=0;i<values.size();i++){
				data[i] = values.get(i)[0]/20f;
				data[i+ServiceAcc.SIZE_CACHE] = values.get(i)[1]/20f;
				data[i+ServiceAcc.SIZE_CACHE2] = values.get(i)[2]/20f;
			}
		}
		return data;
	}
	public static List<float[]> getSecond(){
		int idxStart = values.size()-SIZE_RENDE;
		return values.subList(idxStart, values.size());
	}

	private int data_cnt = 0;
	public static int SIZE_CACHE = 151;
	public static int SIZE_CACHE2 = 151*2;
	public static int SIZE_BATCH = 50;
	public static int SIZE_RENDE = 25;
	public static final int MSG_RENDER = 666;
	public static final int MSG_DETECT = 667;
	private SensorEventListener sensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			values.add(event.values.clone());
			if(values.size()> SIZE_CACHE)
				values.remove(0);

			if(values.size()>SIZE_RENDE)
				handler.sendEmptyMessage(MSG_RENDER);

			data_cnt = (data_cnt+1)%SIZE_BATCH;
			if(data_cnt==0 && values.size()>=SIZE_CACHE)
				handler.sendEmptyMessage(MSG_DETECT);
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//获取Sensor对象
		sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(sensorEventListener,sensor,
				SensorManager.SENSOR_DELAY_GAME);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		sensorManager.unregisterListener(sensorEventListener);
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
