package com.martinporto.smallwebserver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class AboutDialog extends DialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		LayoutInflater i = getActivity().getLayoutInflater();
		View v = i.inflate(R.layout.about_dialog, null);
		
		TextView txtVersion = v.findViewById(R.id.txtVersion);
		TextView txtGithub = v.findViewById(R.id.txtGithub);
		TextView txtLicense = v.findViewById(R.id.txtLicense);
		
		txtVersion.setText("v1.20");
		txtGithub.setText("asdasd");
		txtLicense.setText("MIT");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
			
			.setTitle("About").setNegativeButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					AboutDialog.this.getDialog().cancel();
				}
			});
		
		builder.setView(v);
		return builder.create();
	}
}