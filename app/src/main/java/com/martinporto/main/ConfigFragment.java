package com.martinporto.main;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.martinporto.model.JdiDatabase;
import com.martinporto.model.JdiToast;
import com.martinporto.model.JdiWebServer;
import com.martinporto.smallwebserver.ConfigDialog;
import com.martinporto.smallwebserver.R;
import com.martinporto.smallwebserver.SharingDialog;

import java.util.ArrayList;

import timber.log.Timber;

public class ConfigFragment extends Fragment implements DialogInterface.OnDismissListener {
	
	TextView txtPort;
	
	public ConfigFragment() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.config_fragment, container, false);
		return rootView;
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		
		/* btnShare */
		Button btnShare = view.findViewById(R.id.btnShare);
		btnShare.setOnClickListener(view12 -> {
			
			SharingDialog demoSharingDialog = new SharingDialog();
			demoSharingDialog.setDemoName("bsduDemo.getName()");
			demoSharingDialog.setAddress("bsduDemo.getAddress()");
			demoSharingDialog.show(getActivity().getSupportFragmentManager(), "NoticeDialogFragment");
		});
		
		/* btnReset */
		Button btnReset = view.findViewById(R.id.btnReset);
		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Integer total = JdiDatabase.get().resetStats();
				JdiToast.show("Deleted " + total + " logs");
			}
		});
		
		/* btnChooseWebsite */
		final int SELECT_ZIP_FILE = 3;
		Button btnChooseWebsite = view.findViewById(R.id.btnChooseWebsite);
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
		
		txtPort = view.findViewById(R.id.txtMessage);
		updateServerInformation();

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			
			if (data != null) {
				
				Uri zipData = data.getData();
				String aux[] = zipData.getPath().split(":");
				String source = "/storage/emulated/0/" + aux[1];
				String destination = JdiWebServer.getWwwRootPath() + "/";
				
				ConfigDialog configDialog = new ConfigDialog();
				configDialog.setOnDismissListener(this);
				configDialog.show(getActivity().getSupportFragmentManager(), "NoticeDialogFragment");
				configDialog.unzip(source, destination);
			}
			
		} else {
			
			if (resultCode != RESULT_CANCELED) {
				Toast.makeText(getActivity(), "Sorry, there was an error!", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void updateServerInformation() {
		String message =
			"<b>Port</b>: " + "8080"
				+ "<br><b>wwwroot</b>: " + JdiWebServer.getWwwRootPath()
				+ "<br><b>Total Files</b>: " + JdiWebServer.getTotalFiles()
				+ "<br><b>Total Size</b>: " + JdiWebServer.getTotalSize();
		
		txtPort.setText(Html.fromHtml(message));
	}
	
	@Override
	public void onDismiss(DialogInterface dialogInterface) {
		
		Timber.d("Dismiss");
		updateServerInformation();
	}
}
