package com.koch.ambeth.service.rest;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.util.collections.Tuple3KeyHashMap;
import com.koch.ambeth.util.io.FastByteArrayOutputStream;

public class HttpPerformanceIT {
	@SuppressWarnings("resource")
	@Test
	public void performance() throws Throwable {

		int count = 20000;
		FastByteArrayOutputStream bos = new FastByteArrayOutputStream(4096);

		long start1 = System.currentTimeMillis();
		for (int a = count; a-- > 0;) {
			URL url = new URL(
					"http://localhost:8181/services/services-dynamic/com.koch.ambeth.server.rest.EventServiceREST/getCurrentServerSession");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestProperty("Accept", Constants.AMBETH_MEDIA_TYPE);
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			if (responseCode != HttpStatus.SC_OK) {
				throw new IllegalStateException("ResponseCode: " + responseCode);
			}
			try (InputStream is = con.getInputStream()) {
				bos.reset();
				int oneByte;
				while ((oneByte = is.read()) != -1) {
					bos.write(oneByte);
				}
			}
		}
		CloseableHttpClient httpClient = HttpClientBuilder.create().evictExpiredConnections().build();
		try {
			long start2 = System.currentTimeMillis();
			Tuple3KeyHashMap<String, Integer, String, HttpHost> hostMap = new Tuple3KeyHashMap<>();
			for (int a = count; a-- > 0;) {
				URL url = new URL(
						"http://localhost:8181/services/services-dynamic/com.koch.ambeth.server.rest.EventServiceREST/getCurrentServerSession");
				HttpHost httpHost = hostMap.get(url.getHost(), url.getPort(), url.getProtocol());
				if (httpHost == null) {
					httpHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
					hostMap.put(url.getHost(), url.getPort(), url.getProtocol(), httpHost);
				}
				HttpUriRequest request = RequestBuilder.create("GET").setUri(url.getPath())
						.setHeader("Accept", Constants.AMBETH_MEDIA_TYPE).build();
				try (CloseableHttpResponse response = httpClient.execute(httpHost, request)) {
					int responseCode = response.getStatusLine().getStatusCode();
					if (responseCode != HttpStatus.SC_OK) {
						throw new IllegalStateException("ResponseCode: " + responseCode);
					}
					bos.reset();
					response.getEntity().writeTo(bos);
				}
			}
			long start3 = System.currentTimeMillis();

			System.out
					.println((start3 - start2) + "ms (httpClient), " + (start2 - start1) + "ms (naive)");

			Assert.assertTrue(start3 - start2 < start2 - start1);
		}
		finally {
			httpClient.close();
		}
	}
}
