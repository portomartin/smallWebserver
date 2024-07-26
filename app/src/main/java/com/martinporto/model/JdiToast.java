package com.martinporto.model;

import android.widget.Toast;

import com.martinporto.App;
import com.martinporto.smallwebserver.R;

import io.github.muddz.styleabletoast.StyleableToast;

public class JdiToast {
	
	public static void show(String message) {
		
		StyleableToast.makeText(
			App.getContext(),
			message,
			Toast.LENGTH_LONG,
			R.style.mytoast
		).show();
	}
}
