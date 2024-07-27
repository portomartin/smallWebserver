package com.martinporto.model.webserver;

import androidx.annotation.NonNull;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class ServerRequest {
	
	private final String remoteIp;
	private String uri;
	private final String userAgent;
	private final String method;
	private String referer;
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	
	private String sessionId;
	private String guestInformation;
	
	public ServerRequest(NanoHTTPD.IHTTPSession session) {
		
		remoteIp = session.getRemoteIpAddress();
		uri = session.getUri();
		userAgent = session.getCookies().toString();
		method = session.getMethod().toString();
		getGuestInformation(session);
	}
	
	@NonNull
	@Override
	public String toString() {
		
		String r = String.format(
			"/%s " +
				"http://%s" +
				"%s " +
				"Device:%s",
			method,
			referer,
			uri,
			guestInformation
		);
		
		return r;
	}
	
	public String getInfo() {
		
		String r = String.format(
			"ServerRequest incoming: "
				+ " %s "
				//+ " \n\tsessionId:%s"
				+ " \n\tdevice:%s"
				+ " ",
			remoteIp,
			//sessionId,
			guestInformation
		);
		
		return r;
	}
	
	private void getGuestInformation(NanoHTTPD.IHTTPSession session) {
		
		Map<String, String> headers = session.getHeaders();
		
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			
			String key = entry.getKey();
			String value = entry.getValue();
			
			if (key.equals("user-agent")) {
				
				String str = value;
				String answer = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
				String[] splited = answer.split(";");
				String[] model = splited[2].split("\\s+");
				guestInformation = splited[1].trim();
				
			}
			
			if (key.equals("host")) {
				referer = value;
			}
		}
	}
}
