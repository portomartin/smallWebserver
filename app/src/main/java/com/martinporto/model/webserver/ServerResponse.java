package com.martinporto.model.webserver;

import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class ServerResponse {
	
	NanoHTTPD nanoHTTPD;
	
	public ServerResponse(NanoHTTPD nanoHTTPD) {
		this.nanoHTTPD = nanoHTTPD;
	}
	
	public NanoHTTPD.Response buildResponse(String message) {
		
		NanoHTTPD.Response responseHttp = nanoHTTPD.newFixedLengthResponse(message);
		responseHttp.addHeader("Access-Control-Allow-Origin", "*");
		responseHttp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
		responseHttp.addHeader("Access-Control-Allow-Headers", "*");
		responseHttp.addHeader("Access-Control-Allow-Credentials", "true");
		responseHttp.addHeader("Access-Control-Max-Age", "*");
		responseHttp.addHeader("Access-Control-Expose-Headers", "*");
		return responseHttp;
	}
	
	public NanoHTTPD.Response buildJsonResponseX(String message, String mimeType) {
		NanoHTTPD.Response responseHttp = nanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType, message);
		return responseHttp;
	}
	
	public NanoHTTPD.Response buildImgJpgResponse(InputStream is) {
		
		try {
			NanoHTTPD.Response res = nanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "image/png", is, is.available());
			return res;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return buildResponse("{\"error\":\"Invalid image\"}");
	}
	

}
