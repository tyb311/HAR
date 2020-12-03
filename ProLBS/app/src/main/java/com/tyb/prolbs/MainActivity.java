package com.tyb.prolbs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.tyb.utils.UtilPackage;
import com.tyb.utils.UtilPreferences;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private DrawerLayout drawerLayout;
	private NavigationView navView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setMainLayout();

		drawerLayout = findViewById(R.id.drawer_layout);
		navView = findViewById(R.id.nav_view);
		navView.setCheckedItem(R.id.nav_act);
		navView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "head is clicked!", Toast.LENGTH_SHORT).show();
			}
		});
		navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
				if(menuItem.getItemId()==R.id.nav_act)
					startActivity(new Intent(MainActivity.this, ActMonitor.class));
				else if(menuItem.getItemId()==R.id.nav_set)
					startActivity(new Intent(MainActivity.this, ActSettings.class));
//				elif(menuItem.getItemId()==R.id.nav_sos)
//					startActivity(new Intent(MainActivity.this, ActSettings.class));
//				elif(menuItem.getItemId()==R.id.nav_gps)
//					startActivity(new Intent(MainActivity.this, TestLBS.class));
				else if(menuItem.getItemId()==R.id.nav_out)finish();
				else if(menuItem.getItemId()==R.id.nav_inc) {
					AlertDialog.Builder pd1 = new AlertDialog.Builder(MainActivity.this);
					pd1.setTitle("跌倒检测");
					pd1.setIcon(R.drawable.adl);
					pd1.setMessage(R.string.app_inc);
					pd1.setCancelable(true);
					pd1.show();
				}
				drawerLayout.closeDrawers();
				return true;
			}
		});

//		getSupportActionBar().hide();
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.openDrawer(navView);
			}
		});

		getPermissions();
//		startActivity(new Intent(MainActivity.this, TestGit.class));
//		startActivity(new Intent(MainActivity.this, ActMonitor.class));
		Log.i("程序", "开始");
	}


	TextView txt_info,txt_gitpage;
	void setMainLayout(){
		txt_info = findViewById(R.id.txt_info);
		txt_gitpage = findViewById(R.id.txt_gitpage);
		//设置超链接可点击
//		String hrefStr = "<a href=\"https://pan.baidu.com/s/1vM9Mvtze4-ssDnzL3V8NTg\">";
//		String html = String.format("%s下载更新(当前版本=%s)</a>", hrefStr, UtilPackage.getVersionName(this));
		String hrefStr = "<a href=\"https://tyb311.github.io/HAR/\">";
		String html = String.format("%sGitHub与视频演示</a>", hrefStr);
		txt_gitpage.setText(Html.fromHtml(html));
		txt_gitpage.setMovementMethod(LinkMovementMethod.getInstance());
		txt_gitpage.setVisibility(View.VISIBLE);

		findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getApplicationContext(), ActMonitor.class);
				startActivity(intent);
			}
		});
	}

	private void getPermissions(){
		List<String> permissionList = new ArrayList<>();
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
			permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
		}
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
			permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
		}
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
			permissionList.add(Manifest.permission.READ_PHONE_STATE);
		}
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
			permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED) {
			permissionList.add(Manifest.permission.CALL_PHONE);
		}
		if(!permissionList.isEmpty()){
			String[] permissions = permissionList.toArray(new String[permissionList.size()]);
			ActivityCompat.requestPermissions(this, permissions, 1);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (1==requestCode){
			if(grantResults.length>0){
				for(int result:grantResults){
					if(result!= PackageManager.PERMISSION_GRANTED){
						Toast.makeText(this, "必须同意所有权限才能使用本程序！", Toast.LENGTH_SHORT).show();
						finish();
						return;
					}
				}
			}else{
				Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		UtilPreferences preferences = new UtilPreferences(this);
		String number = (String) preferences.getParam(ActSettings.PREFERENCE_PHONE, "10086");
		txt_info.setText("紧急联系人："+number);
	}
}
