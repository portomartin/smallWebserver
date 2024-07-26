package com.martinporto.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.martinporto.App;
import com.martinporto.model.JdiActivityLog;
import com.martinporto.model.JdiDatabase;
import com.martinporto.model.JdiPermissions;
import com.martinporto.model.JdiWebServer;
import com.martinporto.smallwebserver.AboutDialog;
import com.martinporto.smallwebserver.ConfigDialog;
import com.martinporto.smallwebserver.R;
import com.martinporto.smallwebserver.SharingDialog;

import java.util.ArrayList;

import timber.log.Timber;

public class LabActivity extends AppCompatActivity implements JdiWebServer.OnEventListener {
	
	LifecycleOwner context;
	Context context1;
	TextView txtTotalFiles;
	TextView txtTotalSize;
	TextView txtUptime;
	TextView txtUri;
	TextView txtStatus;
	Button btnStatus;
	Button btnSetup;
	//JdiWebServer jdiWebServer;
	WebView webview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lab_activity);
		context = this;
		
		//setHasOptionsMenu(true);
		
		ArrayAdapter arrayAdapter;
		
		JdiPermissions.requestRuntimePermission(this);
		
		txtTotalFiles = findViewById(R.id.txtTotalFiles);
		txtTotalSize = findViewById(R.id.txtTotalSize);
		txtUptime = findViewById(R.id.txtUptime);
		txtUri = findViewById(R.id.txtUri);
		txtStatus = findViewById(R.id.txtStatus);
		btnStatus = findViewById(R.id.btnStatus);
		btnSetup = findViewById(R.id.btnSetup);
		webview = findViewById(R.id.webview);
		
		App app = (App) getApplicationContext();
		app.getJdiWebServer().setOnEventListener(this);
		app.getJdiWebServer().launch();
		
		app.getJdiWebServer().getUptime().observe(context,
			s -> txtUptime.setText(String.format("%s", s)));
		
		txtUri.setText(String.format("%s", Html.fromHtml(app.getJdiWebServer().getLocationMessage())));
		
		app.getJdiWebServer().isRunning().observe(context, isRunning ->
		{
			
			Integer teal = App.getContext().getResources().getColor(R.color.teal_700);
			
			if (isRunning) {
				txtStatus.setText(String.format("%s", "RUNNING"));
				btnStatus.setText("Stop");
				btnStatus.setBackgroundColor(Color.RED);
				txtStatus.setTextColor(teal);
			} else {
				txtStatus.setText(String.format("%s", "STOPED"));
				btnStatus.setText("Start");
				Integer color = App.getContext().getResources().getColor(R.color.teal_700);
				btnStatus.setBackgroundColor(color);
				txtStatus.setTextColor(Color.RED);
			}
		});
		
		webView();
		
		/* btnStatus */
		btnStatus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				app.getJdiWebServer().toggle();
			}
		});
		
		/* btnSetup */
		btnSetup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ConfigDialog configDialog = new ConfigDialog();
				configDialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
			}
		});
		
		/* listView */
		ListView listViewActivity;
		listViewActivity = findViewById(R.id.listLogs);
		arrayAdapter = new ArrayAdapter<>(this, R.layout.log_adapter, R.id.txtFile, JdiActivityLog.get().getLogs());
		listViewActivity.setAdapter(arrayAdapter);
		
		/* logs */
		JdiActivityLog.get().getmLogs().observe(this, new Observer<ArrayList<String>>() {
			@Override
			public void onChanged(ArrayList<String> strings) {
				arrayAdapter.notifyDataSetChanged();
				//Integer total = JdiDatabase.get().getStats();
				//txtStats.setText("Total hits:" + total);
			}
		});
		
		/* stats */
		TextView txtStats = findViewById(R.id.txtStats);
		JdiDatabase.get().getStats().observe(this, new Observer<Integer>() {
			@Override
			public void onChanged(Integer total) {
				txtStats.setText("Hits today:" + total);
			}
		});
		
	}
	
	public void webView() {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		
		webview.setPadding(0, 0, 0, 0);
		webview.setInitialScale(getScale());
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			
			}
		});
		
		WebSettings settings = webview.getSettings();
		webview.getSettings().setJavaScriptEnabled(true);
		settings.setLoadWithOverviewMode(true);
		//settings.setUseWideViewPort(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		
		webview.setWebChromeClient(new WebChromeClient());
		webview.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.devices_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.btnAbout) {
			AboutDialog aboutDialog = new AboutDialog();
			aboutDialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
			return true;
		}
		
		if (item.getItemId() == R.id.btnShare) {
			SharingDialog sharingDialog = new SharingDialog();
			sharingDialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
			return true;
		}
		
		return super.onOptionsItemSelected(item);
		
	}
	
	private int getScale() {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		Double val = new Double(width) / new Double(2000);
		val = val * 100d;
		return val.intValue();
	}
	
	@Override
	public void OnRequestIncoming(JdiWebServer.Request request) {
		
		JdiActivityLog.get().addLog(request.getInfo());
		JdiDatabase.get().addNewLog(request.toString());
	}
	
	@Override
	public void OnStop() {
		Timber.d("STOP");
		
	}
	
	@Override
	public void OnStart() {
		
		String uriBase = "http://127.0.0.1:8080/index.html";
		webview.loadUrl(uriBase);
	}
	
	
}
