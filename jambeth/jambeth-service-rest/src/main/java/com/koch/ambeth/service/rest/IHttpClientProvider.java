package com.koch.ambeth.service.rest;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;


public interface IHttpClientProvider {

	HttpClient getHttpClient();

	HttpHost getHttpHost(String host, int port, String protocol);
}