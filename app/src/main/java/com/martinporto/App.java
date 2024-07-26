package com.martinporto;

import android.app.Application;
import android.content.Context;

import com.martinporto.model.JdiWebServer;

import java.io.IOException;

import timber.log.Timber;

public class App extends Application {
	
	private static Context mContext;
	private static String VERSION = "v1.00";
	private static String NAME = "Small WebServer";
	JdiWebServer jdiWebServer;
	
	public JdiWebServer getJdiWebServer() {
		return jdiWebServer;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		Timber.plant(new MyDebugTree());
		
		/* jdiWebServer */
		try {
			jdiWebServer = new JdiWebServer();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Context getContext() {
		return mContext;
	}
	
	public static String getVersion() {
		return VERSION;
	}
	
	public static String getName() {
		return NAME;
	}
	
	public static class MyDebugTree extends Timber.DebugTree {
		
		@Override
		protected String createStackElementTag(StackTraceElement element) {
			return String.format("-log- (%s:%s)#%s",
				element.getFileName(),
				element.getLineNumber(),
				element.getMethodName());
		}
	}
}
