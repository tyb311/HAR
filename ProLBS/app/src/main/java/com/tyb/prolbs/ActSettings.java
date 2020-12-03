package com.tyb.prolbs;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tyb.utils.UtilAlarm;
import com.tyb.utils.UtilPackage;
import com.tyb.utils.UtilPreferences;

import java.util.regex.Pattern;

public class ActSettings extends AppCompatActivity{
	public static final String PREFERENCE_PHONE="PHONE";
	public static final String PREFERENCE_TIME="TIME";
	EditText txt_phone;
	TextView txt_info, txt_update;

	UtilPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		txt_phone = findViewById(R.id.edit_phone);
		txt_info = findViewById(R.id.txt_info_settings);
		preferences = new UtilPreferences(this);

		txt_update = findViewById(R.id.txt_update);
		//设置超链接可点击
		String hrefStr = "<a href=\"https://pan.baidu.com/s/1vM9Mvtze4-ssDnzL3V8NTg\">";
		String html = String.format("%s下载更新(当前版本=%s)</a>", hrefStr, UtilPackage.getVersionName(this));
//		String hrefStr = "<a href=\"https://tyb311.github.io/HAR/\">";
//		String html = String.format("%sGitHub与视频演示</a>", hrefStr);
		txt_update.setText(Html.fromHtml(html));
		txt_update.setMovementMethod(LinkMovementMethod.getInstance());
		txt_update.setVisibility(View.VISIBLE);

		findViewById(R.id.btn_save_phone).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String number = txt_phone.getText().toString();
				if(number.isEmpty())number="10086";
				if(isPhone(number)){
					preferences.saveParam(PREFERENCE_PHONE, number);
					if(preferences.getParam(PREFERENCE_PHONE, "10086").equals(number))
						Toast.makeText(ActSettings.this, "设置成功！", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(ActSettings.this, "设置失败！", Toast.LENGTH_SHORT).show();
				}else{
					new AlertDialog.Builder(ActSettings.this)
							.setIcon(R.drawable.adl)
							.setTitle("提示！")
							.setMessage("请输入正确格式的手机号码，作为您的紧急联系人！")
							.show();
				}
				showPreferences();
			}
		});

		findViewById(R.id.btn_dial_phone).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showPreferences();
				String number = (String) preferences.getParam(PREFERENCE_PHONE, "10086");
				UtilAlarm.callPhone(ActSettings.this, number);
			}
		});

		SeekBar bar_time =  findViewById(R.id.bar_wait_time);
		bar_time.setMax(30);
		bar_time.setProgress((int)preferences.getParam(PREFERENCE_TIME, 6));
		bar_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				preferences.saveParam(PREFERENCE_TIME, i);
				showPreferences();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		showPreferences();
	}


	void showPreferences(){
		String number = (String) preferences.getParam(PREFERENCE_PHONE, "10086");
		int time = (int) preferences.getParam(PREFERENCE_TIME, 6);
		txt_info.setText("紧急联系号码："+number+"\n报警等待时间："+time+"秒");
	}

	public boolean isPhone(String number) {
		String PHONE_PATTERN="^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17([0,1,6,7,]))|(18[0-2,5-9]))\\d{8}$";
		boolean isPhone = Pattern.compile(PHONE_PATTERN).matcher(number).matches();
		if(number.length()==5 || number.length()==3 || number.length()==7)isPhone=true;
		Log.i("Phone Check:", isPhone ? "是一个手机号码" : "不是手机号");
		Log.i("Phone:", number);
		return isPhone;
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
