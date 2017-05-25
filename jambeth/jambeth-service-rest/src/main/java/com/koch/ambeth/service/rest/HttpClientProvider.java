package com.koch.ambeth.service.rest;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;

public class HttpClientProvider implements IHttpClientProvider, IDisposableBean {

	protected CloseableHttpClient httpClient;

	protected final Tuple3KeyHashMap<String, Integer, String, HttpHost> hostMap =
			new Tuple3KeyHashMap<>();

	protected volatile boolean disposed;

	@Override
	public void destroy() throws Throwable {
		synchronized (this) {
			disposed = true;
			if (httpClient != null) {
				httpClient.close();
				httpClient = null;
			}
		}
	}

	@Override
	public HttpClient getHttpClient() {
		synchronized (this) {
			if (httpClient == null) {
				if (disposed) {
					throw new IllegalStateException("Bean already disposed");
				}
				httpClient = HttpClientBuilder.create().build();
			}
			return httpClient;
		}
	}

	@Override
	public HttpHost getHttpHost(String host, int port, String protocol) {
		synchronized (hostMap) {
			HttpHost httpHost = hostMap.get(host, port, protocol);
			if (httpHost == null) {
				httpHost = new HttpHost(host, port, protocol);
				hostMap.put(host, port, protocol, httpHost);
			}
			return httpHost;
		}
	}
}
