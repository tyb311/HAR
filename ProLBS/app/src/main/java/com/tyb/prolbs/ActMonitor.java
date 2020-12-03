package com.tyb.prolbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.tyb.service.ServiceAcc;
import com.tyb.service.ServiceLbs;
import com.tyb.utils.UtilAlarm;
import com.tyb.utils.UtilHar;
import com.tyb.utils.UtilMChartBar;
import com.tyb.utils.UtilMChartLine;
import com.tyb.utils.UtilPaddle;
import com.tyb.utils.UtilTTS;

public class ActMonitor extends AppCompatActivity {
	TextView txt_adl, txt_gps;
	Switch sw_tts;//sw_ret,sw_model,

	LineChart lineChart;
	BarChart barChart;

	UtilTTS utilTTS;
	UtilPaddle netPaddle;
	UtilHar utilHar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		//TXT
		txt_adl = findViewById(R.id.txt_adl);
		txt_gps = findViewById(R.id.txt_gps);
		//图表
		lineChart = (LineChart) findViewById(R.id.chart_line);
		chartLine = new UtilMChartLine(lineChart);
		barChart = (BarChart) findViewById(R.id.chart_bar);
		chartBar = new UtilMChartBar(barChart);

		//推理引擎
		netPaddle = new UtilPaddle(this);
		utilHar = new UtilHar();
		//TTS
		utilTTS = new UtilTTS(getApplicationContext());
		sw_tts = findViewById(R.id.se_tts);
		sw_tts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				utilTTS.isEnable = b;
			}
		});
		sw_tts.setChecked(false);
//
//		sw_model = findViewById(R.id.sw_model);
//		sw_model.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//			@Override
//			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//				usePytorch = b;
//				if(usePytorch)sw_model.setTextOn("PyTorch");
//				else sw_model.setTextOff("TensorFlow");
//			}
//		});
//		sw_model.setChecked(false);
		
		
		//传感器
		ServiceAcc.setHandler(handler);
		startService(new Intent(this, ServiceAcc.class));

		//GPS定位
		ServiceLbs.setHandler(handler);
		startService(new Intent(this, ServiceLbs.class));
	}

	private UtilMChartLine chartLine;
	private UtilMChartBar chartBar;
	float[] acc;
	int ret;
	void monitor(){
		//推理采集到的加速度数据
		float[] data = ServiceAcc.getBatch(false);

		acc = netPaddle.infer(data, new long[]{1,3,151});
		ret = netPaddle.argmax(acc);

		//刷新界面折线图、直方图、TextView
		// chartBar.randomRender();
		chartBar.renderFloats(acc);
		String ttsStr = UtilHar.HAR_LEVEL_TTS[ret];
		txt_adl.setText(ttsStr);

		//HAR监测与报警
		utilHar.update(ret);
		if(utilHar.isFall()){//跌倒报警：等待拨号
			utilTTS.speak(UtilAlarm.strFALL);//speakForcely
			UtilAlarm.alarm(ActMonitor.this, handler);
		}else if (utilHar.isSOS()){//SOS报警：直接拨号
			utilTTS.speak(UtilAlarm.strSOS);
			UtilAlarm.sos(handler);
		} else if(!UtilAlarm.isAlarming()){//无需报警：播报动作
			utilTTS.speak(ttsStr);
		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		@Override
		public void handleMessage(final Message msg){
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					switch (msg.what){
						case ServiceAcc.MSG_RENDER:
							chartLine.renderFloats(ServiceAcc.getSecond());
							break;
						case ServiceAcc.MSG_DETECT:
							monitor();
							break;
						case ServiceLbs.MSG_BAIDU:
							txt_gps.setText(msg.obj.toString());
							break;
						case UtilAlarm.MSG_ALARM:
							Context ctx = ActMonitor.this;
							UtilAlarm.toast(ctx, UtilAlarm.strTXT);
//							Toast.makeText(ctx, , Toast.LENGTH_LONG).show();
							UtilAlarm.callPhone(ctx);
							break;
						default:break;
					}
				}
			});
		}
	};


	@Override
	public void onBackPressed() {
		AlertDialog.Builder pd1 = new AlertDialog.Builder(this);
		pd1.setTitle("跌倒检测");
		pd1.setIcon(R.drawable.adl);
		pd1.setMessage("请在右上角菜单处退出");
		pd1.setCancelable(true);
		pd1.show();
//        super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		menu.add("开始");
		menu.add("停止");
		menu.add("退出");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if(item.getTitle()=="开始"){
			startService(new Intent(this, ServiceAcc.class));
			startService(new Intent(this, ServiceLbs.class));
		}else if(item.getTitle()=="停止"){
			stopService(new Intent(this, ServiceAcc.class));
			stopService(new Intent(this, ServiceLbs.class));
		}else
			finish();
		return super.onOptionsItemSelected(item);
	}
}

