package com.martinporto.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

public class JdiFile {
	
	int count = 0;
	Long size = 0l;
	
	public JdiFile() {
	}
	
	public Integer getDirectoryCount(String dirPath) {
		count = 0;
		
		recursiveFileCounter(dirPath);
		return count;
	}
	
	private void recursiveFileCounter(String dirPath) {
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		
		if (files != null) {

			for (int i = 0; i < files.length; i++) {
				
				count++;
				File file = files[i];
				
				if (file.isDirectory()) {
					recursiveFileCounter(file.getAbsolutePath());
				}
			}
		}
	}
	
	public Long getDirectorySize(String dirPath) {
		
		size = 0L;
		recursiveFileSizer(dirPath);
		return size;
	}
	
	public static void emptyDirectory(String dirPath) {
		
		File f = new File(dirPath);
		f.delete();
	}
	
	public static String getTextContent(String fileName) {
		
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
	
	public static InputStream getImageContent(String fileName) {
		
		InputStream stream;
		
		try {
			stream = new FileInputStream(fileName);
			return stream;
		} catch (IOException e) {
			Timber.d("Fail %s", e.toString());
		}
		
		return null;
	}
	
	private void recursiveFileSizer(String dirPath) {
		
		File f = new File(dirPath);
		File[] files = f.listFiles();
		
		if (files != null) {
			
			for (int i = 0; i < files.length; i++) {
				
				File file = files[i];
				Long s = file.length();
				size = size + s;
				
				if (file.isDirectory()) {
					recursiveFileSizer(file.getAbsolutePath());
				}
			}
		}
	}
}

