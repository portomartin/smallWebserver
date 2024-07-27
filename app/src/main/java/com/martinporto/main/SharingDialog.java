package com.martinporto.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.martinporto.smallwebserver.R;

public class SharingDialog extends DialogFragment {

	String address;
	String demoName;
	String ip;
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setDemoName(String demoName) {
		this.demoName = demoName;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		LayoutInflater i = getActivity().getLayoutInflater();
		View v = i.inflate(R.layout.sharing_dialog, null);
		
		TextView txtAddress = v.findViewById(R.id.txtAddress);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
			
			.setTitle("Share website")
			
			.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					SharingDialog.this.getDialog().cancel();
				}
			});
		
		/* wifi ip address */
		Context context = getActivity();
		WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		ip = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
		String uri = "http://"  + ip + ":8080";
		txtAddress.setText(String.format("Scan this QR code and access the site:\n%s", uri));
		
		/* qr */
		ImageView imageCode =  v.findViewById(R.id.imageCode);
		imageCode.setImageBitmap(makeQrCode(uri));
		
		builder.setView(v);
		return builder.create();
	}
	
	private Bitmap makeQrCode(String address) {
		
		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, 550, 550);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		
		int w = bitMatrix.getWidth();
		int h = bitMatrix.getHeight();
		int[] pixels = new int[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
		return bitmap;
		
	}
}