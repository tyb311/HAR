package com.tyb.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.tyb.prolbs.ActSettings;
import com.tyb.prolbs.R;

public class UtilAlarm {
	public static final String strFALL = "您跌倒了吗？即将拨号求救，如有误报请点取消";
	public static final String strSOS = "检测到您发出了求救信号，现在为您拨号求救";
	public static final String strTXT = "！！！已开启拨号求救模式！！！";

	/**
	 * 拨打电话（直接拨打电话）
	 */
	public static void callPhone(Context ctx) {
		String phoneNum = (String) new UtilPreferences(ctx).getParam(ActSettings.PREFERENCE_PHONE, "10086");
		callPhone(ctx, phoneNum);
	}
	public static void callPhone(Context ctx, String phoneNum) {
		Intent intent = new Intent(Intent.ACTION_CALL);
		Uri data = Uri.parse("tel:" + phoneNum);
		intent.setData(data);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (ctx.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    Activity#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for Activity#requestPermissions for more details.

				ActivityCompat.requestPermissions((Activity) ctx, new String[]{Manifest.permission.CALL_PHONE},0);
				return;
			}
		}

		//Calling startActivity() from outside of an Activity  context requires the FLAG_ACTIVITY_NEW_TASK fla
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
		ctx.startActivity(intent);
	}


	/**
	 * 拨打电话（跳转到拨号界面，用户手动点击拨打）
	 */
	public void dialPhone(Context ctx, String phoneNum) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		Uri data = Uri.parse("tel:" + phoneNum);
		intent.setData(data);
		ctx.startActivity(intent);
	}


	public static final int MSG_ALARM = 404;
	private static boolean alarming = false;
	private static int alarm_max = 12;
	public static boolean isAlarming(){return  alarming;}
	public static void alarmService(Context ctx, final Handler handler){
		alarming =true;
		alarm_max = (int) new UtilPreferences(ctx).getParam(ActSettings.PREFERENCE_TIME, alarm_max);

		final ProgressDialog dialog = new ProgressDialog(ctx);
		handler.post(new Runnable() {
			@Override
			public void run() {
				dialog.setTitle("警报倒计时");
				dialog.setIcon(R.drawable.adl);
				dialog.setMessage(strFALL);
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setMax(alarm_max);
				dialog.setCancelable(false);
				dialog.setButton("取消报警", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whic) {
						dialog.cancel();
						alarming =false;
					}
				});

				//8.0系统加强后台管理，禁止在其他应用和窗口弹提醒弹窗，如果要弹，必须使用TYPE_APPLICATION_OVERLAY，否则弹不出
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
				}else {
					dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
				}
				dialog.show();


				for(int i=0;i<alarm_max;i++){
					if(alarming ==false)break;
					try {
						dialog.setProgress(i);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(alarming) handler.sendEmptyMessage(MSG_ALARM);
				dialog.cancel();
				alarming =false;
			}
		});
	}


	public static void toast(Context ctx, String str){
		Toast toast = Toast.makeText(ctx, "", Toast.LENGTH_SHORT);
		toast.setText(str);
		toast.show();
	}
	public static void sos(Handler handler){
		if(alarming)return;
		handler.sendEmptyMessage(UtilAlarm.MSG_ALARM);
		UtilAlarm.alarming = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
					UtilAlarm.alarming=false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	public static void alarm(Context ctx, final Handler handler){
		if(alarming)return;
		alarming =true;
		final ProgressDialog dialog = new ProgressDialog(ctx);
		dialog.setTitle("警报倒计时");
		dialog.setIcon(R.drawable.adl);
		dialog.setMessage(strFALL);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		alarm_max = (int) new UtilPreferences(ctx).getParam(ActSettings.PREFERENCE_TIME, alarm_max);
		dialog.setMax(alarm_max);
		dialog.setCancelable(false);
		dialog.setButton("取消报警", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whic) {
				dialog.cancel();
				alarming =false;
			}
		});
		dialog.show();
		new Thread(new Runnable(){
			@Override
			public void run() {
				for(int i=0;i<alarm_max;i++){
					if(alarming ==false)break;
					try {
						dialog.setProgress(i);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(alarming) handler.sendEmptyMessage(MSG_ALARM);
				dialog.cancel();
				alarming =false;
			}
		}).start();
	}

	public static AlertDialog getTestDialog(Context ctx){
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("service中弹出Dialog了")
				.setMessage("是否关闭dialog？")
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
							}
						})
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
												int whichButton) {
							}
						});
		//下面这行代码放到子线程中会 Can't create handler inside thread that has not called Looper.prepare()
		AlertDialog dialog = builder.create();
		//设置点击其他地方不可取消此 Dialog
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
}
