package com.martinporto.main;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.martinporto.App;
import com.martinporto.model.webserver.WebAppInterface;
import com.martinporto.smallwebserver.R;

public class WebFragment extends Fragment {

	public WebFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.web_dialog, container, false);
		return rootView;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		
		WebView webview = view.findViewById(R.id.webview);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}
		
		webview.addJavascriptInterface(new WebAppInterface(App.getContext()), "Android");
		WebSettings webSettings = webview.getSettings();
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setJavaScriptEnabled(true);
		
		webview.requestFocus();
		webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
		
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (progress < 100) {
					//progressDialog.show();
				}
				
				if (progress == 100) {
					//progressDialog.dismiss();
				}
			}
		});
		
		String uriBase = "http://127.0.0.1:8080/index.html";
		webview.loadUrl(uriBase);
	}
}
