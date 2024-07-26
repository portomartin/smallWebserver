package com.martinporto.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.martinporto.App;
import java.util.ArrayList;
import java.util.List;

public class JdiPermissions {
	
	static Context context;
	
	private static final int REQUEST_PERMISSION = 100;
	private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;
	
	private static String[] PERMISSIONS_STORAGE = {
		android.Manifest.permission.READ_EXTERNAL_STORAGE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
		android.Manifest.permission.BLUETOOTH_SCAN,
		android.Manifest.permission.BLUETOOTH_CONNECT,
		android.Manifest.permission.BLUETOOTH_PRIVILEGED
	};
	private static String[] PERMISSIONS_LOCATION = {
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
		android.Manifest.permission.BLUETOOTH_SCAN,
		android.Manifest.permission.BLUETOOTH_CONNECT,
		Manifest.permission.BLUETOOTH_PRIVILEGED
	};
	
	public JdiPermissions()  {
		context = App.getContext();
	}
	
	public static void checkBluetoothPermissions(Activity activity) {
		
		int permission1 = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int permission2 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN);
		
		if (permission1 != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 1);
		} else if (permission2 != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, 1);
		}
	}
	
	public static  void requestRuntimePermission(Activity activity) {
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return;
		}
		
		List<String> requestPermissions = new ArrayList<>();
		
		int permissionStorage = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionStorage == PackageManager.PERMISSION_DENIED) {
			requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		
		if (!requestPermissions.isEmpty()) {
			ActivityCompat.requestPermissions(activity, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
		}
	}
	
	public static void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults,Activity activity) {
		
		if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
			return;
		}
		
		List<String> requestPermissions = new ArrayList<>();
		
		for (int i = 0; i < permissions.length; i++) {
			if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				&& grantResults[i] == PackageManager.PERMISSION_DENIED) {
				requestPermissions.add(permissions[i]);
			}
		}
		
		if (!requestPermissions.isEmpty()) {
			ActivityCompat.requestPermissions(activity, requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSION);
		}
	}
	
	public static void checkStoragePermission(Activity activity) {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			if (!Environment.isExternalStorageManager()) {
				takePermission(activity);
			}
		} else {
			if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
			}
		}
	}
	
	
	private static void takePermission(Activity activity) {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			
			try {
				Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setData(Uri.parse(String.format("package:%s", context.getPackageName())));
				activity.startActivity(intent);
				
			} catch (Exception e) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
				activity.startActivity(intent);
			}
		}
	}
}



