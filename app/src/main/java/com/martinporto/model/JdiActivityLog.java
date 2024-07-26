package com.martinporto.model;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;

public class JdiActivityLog {
	
	private ArrayList<String> logs;
	private MutableLiveData<ArrayList<String>> mLogs;
	private static JdiActivityLog ourInstance = new JdiActivityLog();
	private Integer counter = 0;
	
	public static JdiActivityLog get() {
		return ourInstance;
	}
	
	public MutableLiveData<ArrayList<String>> getmLogs() {
		return mLogs;
	}
	
	public JdiActivityLog() {
		
		logs = new ArrayList<>();
		mLogs = new MutableLiveData<>();
	}
	
	public ArrayList<String> getLogs() {
		ArrayList<String> tempElements = new ArrayList<>(logs);
		Collections.reverse(tempElements);
		return logs;
	}
	
	public void addLog(String log) {
		
		if (log.contains("127")) {
			return;
		}
		
		logs.add(0, counter++ + " " + log);
		mLogs.postValue(logs);
	}
}
