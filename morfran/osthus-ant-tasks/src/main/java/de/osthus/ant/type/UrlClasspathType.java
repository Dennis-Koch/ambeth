package de.osthus.ant.type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;

import com.google.common.io.ByteStreams;

public class UrlClasspathType extends DataType implements ResourceCollection {

	private String url;
	
	private String username;
	
	private String password; 
	

	@Override
	public Iterator<Resource> iterator() {
		CredentialsAuthenticator authenticator = new CredentialsAuthenticator(username, password);
		
		List<Resource> resources = new ArrayList<Resource>(); 
		
		if ( url.toLowerCase().endsWith(".properties") ) {
			UrlResource propertyResource = new UrlResource(this.url, authenticator);
			Properties properties = new Properties();			
			String baseUrl = this.url.substring(0, this.url.lastIndexOf("/"));
			try {
				properties.load(propertyResource.getInputStream());
				ByteStreams.copy(propertyResource.getInputStream(), new FileOutputStream(new File("c:/output")));
				
				for ( Object value: properties.values() ) {
					if ( value != null && value.toString().toLowerCase().endsWith(".jar") ) {
						resources.add(new UrlResource(baseUrl + "" + value, authenticator));
						System.out.println(baseUrl + "/" + value);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new BuildException("Unable to load properties from URL: " + this.url, e);
			}
			
		} else {
			resources.add(new UrlResource(this.url, authenticator));
		}
		
		System.out.println("resources in classpath: " + resources.size());
		return resources.iterator();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isFilesystemOnly() {
		return false;
	}
	
	
	private class CredentialsAuthenticator extends Authenticator {		
		public CredentialsAuthenticator(String username, String password) {
			new PasswordAuthentication(username, password.toCharArray());
		}
	}
	
	
	public static void main(String[] args) {
		UrlClasspathType cpt = new UrlClasspathType();
		cpt.setUrl("http://ci.member.osthus.de:8080/view/All%20Ambeth/job/jAmbeth%202.0%20Head/lastSuccessfulBuild/artifact/jars/jAmbeth.properties");
		cpt.setUsername("daniel.mueller");
		cpt.setPassword("");
		
		cpt.iterator();
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
}
