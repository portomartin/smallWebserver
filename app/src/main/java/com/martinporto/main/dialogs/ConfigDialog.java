package com.martinporto.main.dialogs;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.martinporto.App;
import com.martinporto.model.JdiDatabase;
import com.martinporto.model.JdiToast;
import com.martinporto.model.webserver.Server;
import com.martinporto.model.JdiZip;
import com.martinporto.smallwebserver.R;

import java.io.IOException;

import timber.log.Timber;

public class ConfigDialog extends DialogFragment {
	
	private Handler handler;
	TextView txtMessage;
	String beforeMessage = "";
	String whileMessage = "";
	String afterMessage = "";
	DialogInterface.OnDismissListener onDismissListener;
	
	public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}
	
	public ConfigDialog() {
		handler = new Handler();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		LayoutInflater i = getActivity().getLayoutInflater();
		View v = i.inflate(R.layout.config_dialog, null);
		
		/* txtMessage */
		txtMessage = v.findViewById(R.id.txtMessage);
		
		/* builder */
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
			
			.setTitle("WebServer Settings")
			.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ConfigDialog.this.getDialog().cancel();
				}
			});
		
		builder.setOnDismissListener(onDismissListener);
		builder.setView(v);
		
		/* btnReset */
		Button btnReset = v.findViewById(R.id.btnReset);
		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Integer total = JdiDatabase.get().resetStats();
				JdiToast.show("Deleted " + total + " logs");
			}
		});
		
		/* btnChooseWebsite */
		final int SELECT_ZIP_FILE = 3;
		Button btnChooseWebsite = v.findViewById(R.id.btnChooseWebsite);
		btnChooseWebsite.setOnClickListener(view1 -> {
			
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("*/*");
				String[] mimetypes = {"application/zip"};
				intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
				
			} else {
				intent.setType("application/zip");
			}
			
			startActivityForResult(intent, SELECT_ZIP_FILE);
			
		});
		
		/* txtWebSiteInfo */
		TextView txtWebSiteInfo = v.findViewById(R.id.txtWebSiteInfo);
		String current = "The entire site must be contained in a valid zip file. \nSelect it from your device, wait the unziping task, and the new site will be running.";
		txtWebSiteInfo.setText(current);
		
		App app = (App) getActivity().getApplicationContext();
		app.getJdiWebServer().getDirectoryCount().observe(getActivity(),
			totalFiles -> {
				txtWebSiteInfo.setText(
					String.format("%s Actually have %s files ", txtWebSiteInfo.getText().toString(), totalFiles.toString()));
			}
		);
		
		app.getJdiWebServer().getDirectorySize().observe(getActivity(),
			totalSize -> txtWebSiteInfo.setText(String.format("%s and %s bytes ", txtWebSiteInfo.getText().toString(), totalSize.toString())));
		
		return builder.create();
	}
	
	public void unzip(String source, String destination) {
		
		/* this listener will be running in background too */
		JdiZip.setOnRunningListener(new JdiZip.OnRunningListener() {
			
			@Override
			public void OnBeforeUnzip(String source, String destination) {
				beforeMessage = ""
					+ "\nFrom:\n " + source
					+ "\n"
					+ "\nTo:\n " + destination;
			}
			
			@Override
			public void OnDoingUnzip(String fileName, Integer counter) {
				
				/* send updates to UI */
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						whileMessage = ""
							+ "\n"
							+ "\nTotal files:\n " + counter
							+ "\n"
							+ "\nUnziping:\n " + fileName;
						txtMessage.setText(beforeMessage + whileMessage);
					}
				});
			}
			
			@Override
			public void OnAfterUnzip() {
				App app = (App) getActivity().getApplicationContext();
				app.getJdiWebServer().stop();
			}
		});
		
		/* unzip in background */
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					JdiZip.unzip(source, destination);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				/* send updates to UI */
				handler.post(new Runnable() {
					@Override
					public void run() {
						Timber.d("source %s", source);
					}
				});
			}
		}).start();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			
			if (data != null) {
				
				Uri zipData = data.getData();
				String aux[] = zipData.getPath().split(":");
				String source = "/storage/emulated/0/" + aux[1];
				String destination = Server.getWwwRootPath() + "/";
				
				//ConfigDialog configDialog = new ConfigDialog();
				//configDialog.setOnDismissListener(this);
				//configDialog.show(getActivity().getSupportFragmentManager(), "NoticeDialogFragment");
				unzip(source, destination);
			}
			
		} else {
			
			if (resultCode != RESULT_CANCELED) {
				//Toast.makeText(getActivity(), "Sorry, there was an error!", Toast.LENGTH_LONG).show();
			}
		}
	}
}