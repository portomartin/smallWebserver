package com.martinporto.model;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Formatter;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.martinporto.App;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import timber.log.Timber;

public class JdiWebServer extends NanoHTTPD {
	
	public interface OnEventListener {
		void OnRequestIncoming(Request jdiWebServerRequest);
		
		void OnStop();
		
		void OnStart();
	}
	
	public interface OnResultListener {
		void OnTotal(Integer t);
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
	private OnResultListener onResultListener;
	
	public void setOnResultListener(OnResultListener onResultListener) {
		this.onResultListener = onResultListener;
	}
	
	public MutableLiveData<String> getUptime() {
		return uptime;
	}
	
	public JdiWebServer() throws IOException {
		
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
			runStatusTimer();
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
		
		if (fileName == null) {
			fileName = "index.html";
		}
		
		String remoteSessionId = session.getCookies().read("sessionID");
		CookieHandler cookieHandler = new CookieHandler(session.getHeaders());
		if (remoteSessionId == null) {
			remoteSessionId = UUID.randomUUID().toString();
			cookieHandler.set("sessionID", remoteSessionId, 1);
		}
		
		if (isHomePage) {
			Request request = new Request(session);
			request.sessionId = remoteSessionId;
			onEventListener.OnRequestIncoming(request);
		}
		
		fileName = getWwwRootPath() + fileName;
		String extension = fileName.substring(fileName.lastIndexOf("."));
		
		if (extension.equals(".png")) {
			return buildImgJpgResponse(getImageContent(fileName));
		}
		
		if (extension.equals(".css")) {
			return buildCssResponse(getTextContent(fileName));
		}
		
		if (extension.equals(".js")) {
			return buildJsResponse(getTextContent(fileName));
		}
		
		if (extension.equals(".json")) {
			return buildJsonResponse(getTextContent(fileName));
		}
		
		Response r = buildResponse(getTextContent(fileName));
		cookieHandler.unloadQueue(r);
		return r;
	}
	
	public String getTextContent(String fileName) {
		
		String tContents = null;
		try {
			
			InputStream stream = new FileInputStream(fileName);
			
			int size = stream.available();
			byte[] buffer = new byte[size];
			stream.read(buffer);
			stream.close();
			tContents = new String(buffer);
			if (tContents.startsWith("\uFEFF")) {
				tContents = tContents.substring(1);
			}
			
			
		} catch (IOException e) {
			Timber.d("Fail %s", e.toString());
		}
		
		String fl = tContents.substring(0, 1);
		if (fl.equals("?")) {
			tContents = tContents.substring(1);
		}
		
		return tContents;
	}
	
	public InputStream getImageContent(String fileName) {
		
		InputStream stream;
		
		try {
			stream = new FileInputStream(fileName);
			return stream;
		} catch (IOException e) {
			Timber.d("Fail %s", e.toString());
		}
		
		return null;
	}
	
	private Response buildResponse(String message) {
		
		Response responseHttp = newFixedLengthResponse(message);
		responseHttp.addHeader("Access-Control-Allow-Origin", "*");
		responseHttp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
		responseHttp.addHeader("Access-Control-Allow-Headers", "*");
		responseHttp.addHeader("Access-Control-Allow-Credentials", "true");
		responseHttp.addHeader("Access-Control-Max-Age", "*");
		responseHttp.addHeader("Access-Control-Expose-Headers", "*");
		return responseHttp;
	}
	
	private Response buildCssResponse(String message) {
		Response responseHttp = newFixedLengthResponse(Response.Status.OK, "text/css", message);
		return responseHttp;
	}
	
	private Response buildJsResponse(String message) {
		Response responseHttp = newFixedLengthResponse(Response.Status.OK, "application/javascript", message);
		return responseHttp;
	}
	
	private Response buildJsonResponse(String message) {
		Response responseHttp = newFixedLengthResponse(Response.Status.OK, "application/json", message);
		return responseHttp;
	}
	
	private Response buildImgJpgResponse(InputStream is) {
		
		try {
			Response res = newFixedLengthResponse(Response.Status.OK, "image/png", is, is.available());
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return buildResponse("{\"error\":\"Invalid command\"}");
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
	
	public static class WebAppInterface {
		
		Context mContext;
		
		public WebAppInterface(Context c) {
			mContext = c;
		}
		
		@JavascriptInterface
		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
	}
	
	public static class Request {
		
		private final String remoteIp;
		private String uri;
		private final String userAgent;
		private final String method;
		private String referer;
		private String sessionId;
		private String guestInformation;
		
		public Request(IHTTPSession session) {
			
			remoteIp = session.getRemoteIpAddress();
			uri = session.getUri();
			userAgent = session.getCookies().toString();
			method = session.getMethod().toString();
			getGuestInformation(session);
		}
		
		@NonNull
		@Override
		public String toString() {
			
			String r = String.format(
				"/%s " +
					"http://%s" +
					"%s " +
					"Device:%s",
				method,
				referer,
				uri,
				guestInformation
			);
			
			return r;
		}
		
		public String getInfo() {
			
			String r = String.format(
				"Request incoming: "
					+ " %s "
					//+ " \n\tsessionId:%s"
					+ " \n\tdevice:%s"
					+ " ",
				remoteIp,
				//sessionId,
				guestInformation
			);
			
			//Timber.d("r %s", r);
			
			return r;
		}
		
		private void getGuestInformation(IHTTPSession session) {
			
			Map<String, String> headers = session.getHeaders();
			
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				
				String key = entry.getKey();
				String value = entry.getValue();
				
				if (key.equals("user-agent")) {
					
					String str = value;
					String answer = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
					String[] splited = answer.split(";");
					String[] model = splited[2].split("\\s+");
					guestInformation = splited[1].trim();
					
				}
				
				if (key.equals("host")) {
					referer = value;
				}
			}
		}
	}
	
	public void getDirectoryCount2(String source) {
		
		/* using Handler() & interface version */
		Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Integer t = new JdiFile().getDirectoryCount(source);
				handler.post(new Runnable() {
					@Override
					public void run() {
						onResultListener.OnTotal(t);
					}
				});
			}
		}).start();
	}
	
	public MutableLiveData<Integer> getDirectoryCount(String source) {
		
		/* using Handler() version */
		Handler handler = new Handler();
		new Thread(() -> {
			
			Integer total = new JdiFile().getDirectoryCount(source);
			handler.post(new Runnable() {
				@Override
				public void run() {
					folderTotalFiles.postValue(total);
				}
			});
		}).start();
		
		return folderTotalFiles;
	}
	
	public MutableLiveData<Long> getDirectorySize(String source) {
		
		/* using observer seems not need to use a Handler() */
		new Thread(() -> {
			Long t = new JdiFile().getDirectorySize(source);
			folderTotalSize.postValue(t);
		}).start();
		
		return folderTotalSize;
	}
	
	
	public void runStatusTimer() {
		
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

