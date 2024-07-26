package com.martinporto.model;

import java.io.File;

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

