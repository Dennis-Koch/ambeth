package de.osthus.ant.type;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.URL;

import org.apache.tools.ant.types.Resource;

public class UrlResource extends Resource {

	private String url;
	private Authenticator authenticator;
	
	public UrlResource() {

	}
	
	public UrlResource(String url, Authenticator authenticator) {
		this.url = url;
		this.authenticator = authenticator;
	}
	
	
	@Override
	public boolean isReference() {
		return false;
	}
	
	@Override
	public boolean isDirectory() {
		return false;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		Authenticator.setDefault(authenticator);
		URL url = new URL(this.url.replaceAll(" ", "%20"));		
		System.out.println("url requested: " + url);
		return url.openStream();
	}
}
