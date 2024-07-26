package com.martinporto.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JdiZip {
	
	public interface OnRunningListener {
		void OnBeforeUnzip(String source, String destination);
		
		void OnDoingUnzip(String fileName, Integer counter);
		
		void OnAfterUnzip();
	}
	
	static OnRunningListener onRunningListener;
	
	public static void setOnRunningListener(OnRunningListener onRunningListener) {
		JdiZip.onRunningListener = onRunningListener;
	}
	
	public JdiZip() {
	}
	
	public static void unzip(String zipFile, String destFolder) throws IOException {
		
		onRunningListener.OnBeforeUnzip(zipFile, destFolder);
		
		JdiFile.emptyDirectory(destFolder);
		
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			
			ZipEntry entry;
			byte[] buffer = new byte[1024];
			
			Integer counter = 0;
			while ((entry = zis.getNextEntry()) != null) {
				
				File newFile = new File(destFolder + File.separator + entry.getName());
				
				if (entry.isDirectory()) {
					
					newFile.mkdirs();
					
				} else {
					
					new File(newFile.getParent()).mkdirs();
					onRunningListener.OnDoingUnzip(newFile.getAbsolutePath(), counter++);
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						int length;
						while ((length = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, length);
						}
					}
				}
			}
		}
		
		onRunningListener.OnAfterUnzip();
	}
}

