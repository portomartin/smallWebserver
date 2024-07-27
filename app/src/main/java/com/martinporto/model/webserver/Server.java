package com.martinporto.model.webserver;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Formatter;

import androidx.lifecycle.MutableLiveData;

import com.martinporto.App;
import com.martinporto.model.JdiFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {
	
	public interface OnEventListener {
		void OnRequestIncoming(ServerRequest jdiWebServerRequest);
		
		void OnStop();
		
		void OnStart();
	}
	
	MutableLiveData<Integer> folderTotalFiles;
	MutableLiveData<Long> folderTotalSize;
	MutableLiveData<String> uptime = new MutableLiveData<>();
	MutableLiveData<Boolean> isRunning;
	
	Date startTime;
	Timer statusTimer = new Timer();
	
	private static final Integer port = 8080;
	private OnEventListener onEventListener;
	private static String wwwRootPath = "smallWebServerRoot1";
	
	public MutableLiveData<String> getUptime() {
		return uptime;
	}
	
	public Server() throws IOException {
		
		super(port);
		setOnEventListener(onEventListener);
		
		folderTotalFiles = new MutableLiveData<>();
		folderTotalSize = new MutableLiveData<>();
		startTime = new Date();
		isRunning = new MutableLiveData<>(false);
		
		/* create root if not exists */
		File dir = new File(getWwwRootPath());
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	public void setOnEventListener(OnEventListener onEventListener) {
		this.onEventListener = onEventListener;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public void launch() {
		
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
			startTime = new Date();
			launchStatusTimer();
			onEventListener.OnStart();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void toggle() {
		
		if (isAlive()) {
			stop();
			onEventListener.OnStop();
		} else {
			launch();
		}
	}
	
	@Override
	public Response serve(IHTTPSession session) {
		return this.staticServe(session);
	}
	
	public static String getWwwRootPath() {
		
		File sourcePath = Environment.getExternalStorageDirectory();
		String path = sourcePath + "/" + wwwRootPath;
		return path;
	}
	
	public static Integer getTotalFiles() {
		
		File dir = new File(wwwRootPath);
		if (dir.listFiles() == null) {
			return 0;
		}
		
		return dir.listFiles().length;
	}
	
	public static long getFolderSize(File f) {
		
		long size = 0;
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				size += getFolderSize(file);
			}
		} else {
			size = f.length();
		}
		return size;
	}
	
	public static long getTotalSize() {
		
		File dir = new File(wwwRootPath);
		return getFolderSize(dir);
	}
	
	public Response staticServe(IHTTPSession session) {
		
		String fileName = session.getUri();
		
		Boolean isHomePage = false;
		
		/* only domain name */
		if (fileName.equals("/")) {
			isHomePage = true;
			fileName = fileName + "index.html";
		}
		
		/* uri ends with slash */
		String lastChar = fileName.substring(fileName.length() - 1);
		if (lastChar.equals("/")) {
			fileName = fileName + "index.html";
		}
		
		String remoteSessionId = session.getCookies().read("sessionID");
		CookieHandler cookieHandler = new CookieHandler(session.getHeaders());
		if (remoteSessionId == null) {
			remoteSessionId = UUID.randomUUID().toString();
			cookieHandler.set("sessionID", remoteSessionId, 1);
		}
		
		if (isHomePage) {
			ServerRequest request = new ServerRequest(session);
			request.setSessionId(remoteSessionId);
			onEventListener.OnRequestIncoming(request);
		}
		
		fileName = getWwwRootPath() + fileName;
		String extension = fileName.substring(fileName.lastIndexOf("."));
		
		ServerResponse serverResponse = new ServerResponse(this);
		
		switch (extension) {
			
			case ".png":
				return serverResponse.buildImgJpgResponse(JdiFile.getImageContent(fileName));
			case ".css":
				return serverResponse.buildJsonResponseX(JdiFile.getTextContent(fileName), "text/css");
			case ".js":
				return serverResponse.buildJsonResponseX(JdiFile.getTextContent(fileName), "application/javascript");
			case ".json":
				return serverResponse.buildJsonResponseX(JdiFile.getTextContent(fileName), "application/json");
		}
		
		NanoHTTPD.Response r = serverResponse.buildResponse(JdiFile.getTextContent(fileName));
		cookieHandler.unloadQueue(r);
		return r;
	}
	
	public String getLocationMessage() {
		
		/* wifi ip address */
		WifiManager wifiManager = (WifiManager) App.getContext().getSystemService(Context.WIFI_SERVICE);
		String ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
		
		/* uri */
		String uri = "http://" + ip + ":" + getPort();
		String text = "<a href='" + uri + "'>" + uri + "</a>";
		return text;
	}
	
	public MutableLiveData<Integer> getDirectoryCount() {
		
		String source = Server.getWwwRootPath() + "/";
		
		/* using Handler() version */
		Handler handler = new Handler();
		new Thread(() -> {
			Integer total = new JdiFile().getDirectoryCount(source);
			handler.post(() -> folderTotalFiles.postValue(total));
		}).start();
		
		return folderTotalFiles;
	}
	
	public MutableLiveData<Long> getDirectorySize() {
		
		String source = Server.getWwwRootPath() + "/";
		/* using observer seems not need to use a Handler() */
		new Thread(() -> {
			Long t = new JdiFile().getDirectorySize(source);
			folderTotalSize.postValue(t);
		}).start();
		
		return folderTotalSize;
	}
	
	public void launchStatusTimer() {
		
		TimerTask statusTimerTask = new TimerTask() {
			
			@Override
			public void run() {
				
				isRunning.postValue(isAlive());
				if (!isRunning.getValue()) {
					uptime.postValue(String.format("%s %s", "-", ""));
				} else {
					
					long start = startTime.getTime();
					long finish = System.currentTimeMillis();
					long timeElapsed = finish - start;
					
					//Timber.d("timeElapsed %s %s %s", timeElapsed, statusTimer, this);
					
					String unit = "minutes";
					long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
					
					if (minutes == 0) {
						unit = "seconds";
						minutes = TimeUnit.MILLISECONDS.toSeconds(timeElapsed);
					}
					
					uptime.postValue(String.format("%s %s", minutes, unit));
				}
			}
		};
		
		statusTimer.scheduleAtFixedRate(statusTimerTask, 1000, 3000);
	}
	
	public MutableLiveData<Boolean> isRunning() {
		return isRunning;
	}
}

