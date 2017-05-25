package com.koch.ambeth.service.rest;

/*-
 * #%L
 * jambeth-service-rest
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.typeinfo.TypeInfoItemUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.IOfflineListener;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.remote.IRemoteInterceptor;
import com.koch.ambeth.service.remote.IRemoteTargetProvider;
import com.koch.ambeth.service.rest.config.RESTConfigurationConstants;
import com.koch.ambeth.service.transfer.AmbethServiceException;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.FastByteArrayOutputStream;
import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.XmlTypeNotFoundException;
import com.koch.ambeth.xml.ioc.XmlModule;

import net.sf.cglib.proxy.MethodProxy;

public class RESTClientInterceptor extends AbstractSimpleInterceptor
		implements IRemoteInterceptor, IInitializingBean, IOfflineListener, IDisposableBean {
	public static final String DEFLATE_MIME_TYPE = "application/octet-stream";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuthenticationHolder authenticationHolder;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IHttpClientProvider httpClientProvider;

	@Autowired(optional = true)
	protected IRESTClientServiceUrlBuilder restClientServiceUrlBuilder;

	@Property(name = ServiceConfigurationConstants.ServiceBaseUrl)
	protected String serviceBaseUrl;

	@Property(name = RESTConfigurationConstants.HttpUseClient, defaultValue = "false")
	protected boolean httpUseClient;

	@Property(name = RESTConfigurationConstants.HttpAcceptEncodingZipped, defaultValue = "true")
	protected boolean httpAcceptEncodingZipped;

	@Property(name = RESTConfigurationConstants.HttpContentEncodingZipped, defaultValue = "true")
	protected boolean httpContentEncodingZipped;

	@Property
	protected String serviceName;

	protected final Lock writeLock = new ReentrantLock();

	protected final Condition connectionChangeCond = writeLock.newCondition();

	protected volatile boolean connectionChangePending;

	protected volatile boolean disposed;

	protected final AtomicLong requestCounter = new AtomicLong();

	protected String authorizationValue;

	protected final IdentityHashSet<Thread> responsePendingThreadSet = new IdentityHashSet<>();

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(serviceName, IRemoteTargetProvider.SERVICE_NAME_PROP);
	}

	@Override
	public void destroy() throws Throwable {
		disposed = true;
		writeLock.lock();
		try {
			connectionChangeCond.signalAll();
			for (Thread responsePendingThread : responsePendingThreadSet) {
				responsePendingThread.interrupt();
			}
		}
		finally {
			writeLock.unlock();
		}

	}

	@Override
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy)
			throws Throwable {
		if (disposed) {
			throw new IllegalStateException("Bean already disposed");
		}
		if (toStringMethod.equals(method)) {
			return "Stub for '" + serviceName + "' remoting to REST endpoint '" + serviceBaseUrl + "/"
					+ serviceName + "'";
		}
		if (guiThreadHelper != null && guiThreadHelper.isInGuiThread()) {
			throw new Exception("It is not allowed to call this interceptor from GUI thread");
		}
		boolean threadAdded;
		writeLock.lock();
		try {
			while (connectionChangePending) {
				if (disposed) {
					throw new IllegalStateException("Bean already disposed");
				}
				// Wait till the connection change finished
				connectionChangeCond.await();
			}
			threadAdded = responsePendingThreadSet.add(Thread.currentThread());
		}
		finally {
			writeLock.unlock();
		}
		try {
			long m1 = System.currentTimeMillis();

			long localRequestId = requestCounter.incrementAndGet();
			URL url;
			try {
				url = restClientServiceUrlBuilder != null
						? restClientServiceUrlBuilder.buildURL(serviceBaseUrl, serviceName, method, args)
						: null;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e, "Error occured while building URL call for service '"
						+ serviceName + "/" + method.getName() + "' with base at '" + serviceBaseUrl);
			}
			if (url == null) {
				url = new URL(serviceBaseUrl + "/" + serviceName + "/" + method.getName());
			}
			HttpClient httpClient = httpClientProvider.getHttpClient();
			HttpHost httpHost =
					httpClientProvider.getHttpHost(url.getHost(), url.getPort(), url.getProtocol());

			Object result = null;

			RequestBuilder rb;
			if (args.length > 0) {
				rb = RequestBuilder.post();
				rb.setHeader("Content-Type", Constants.AMBETH_MEDIA_TYPE);
				EntityBuilder eb = EntityBuilder.create();
				if (httpContentEncodingZipped) {
					eb.gzipCompress();
				}
				FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
				cyclicXmlHandler.writeToStream(bos, args);
				eb.setStream(new ByteArrayInputStream(bos.getRawByteArray(), 0, bos.size()));
				rb.setEntity(eb.build());
				if (log.isDebugEnabled()) {
					log.debug(url + " " + localRequestId + " => "
							+ new String(bos.getRawByteArray(), 0, bos.size(), "UTF-8"));
				}
			}
			else {
				rb = RequestBuilder.get();
			}
			rb.setHeader("Accept", Constants.AMBETH_MEDIA_TYPE);
			if (httpAcceptEncodingZipped) {
				rb.setHeader("Accept-Encoding", "gzip");
			}
			setAuthorization(rb);

			rb.setUri(url.getPath());
			HttpUriRequest request = rb.build();

			HttpResponse response = httpClient.execute(httpHost, request);
			if (disposed) {
				throw new IllegalStateException("Bean already disposed");
			}
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode != HttpStatus.SC_OK) {
				throw new IllegalStateException(
						"Response (" + responseCode + ") when calling '" + url + "'");
			}
			FastByteArrayOutputStream memoryStream = new FastByteArrayOutputStream();
			response.getEntity().writeTo(memoryStream);
			if (log.isDebugEnabled()) {
				log.debug(url + " " + localRequestId + " <= "
						+ new String(memoryStream.getRawByteArray(), 0, memoryStream.size(), "UTF-8"));
			}
			try {
				result = cyclicXmlHandler.readFromStream(
						new ByteArrayInputStream(memoryStream.getRawByteArray(), 0, memoryStream.size()));
			}
			catch (XmlTypeNotFoundException e) {
				throw e;
			}
			if (result instanceof AmbethServiceException) {
				Exception exception = parseServiceException((AmbethServiceException) result);
				RuntimeExceptionUtil.fillInClientStackTraceIfPossible(exception);
				throw exception;
			}
			return convertToExpectedType(method.getReturnType(), method.getGenericReturnType(), result);
		}
		finally

		{
			if (threadAdded) {
				writeLock.lock();
				try {
					responsePendingThreadSet.remove(Thread.currentThread());
				}
				finally {
					writeLock.unlock();
				}
			}
		}
	}

	protected Exception parseServiceException(AmbethServiceException serviceException) {
		AmbethServiceException serviceCause = serviceException.getCause();
		Exception cause = null;
		if (serviceCause != null) {
			cause = parseServiceException(serviceCause);
		}
		return new IllegalStateException(
				serviceException.getMessage() + "\n" + serviceException.getStackTrace(), cause);
	}

	/// <summary>
	/// Parse the given WebException and create a new ApplicationException with the parsed error
	/// message
	/// </summary>
	/// <param name="webException">The WebException</param>
	/// <returns>An ApplicationException (if the WebException could be parsed) or the original
	/// WebException</returns>
	// protected Exception ParseWebException(WebException webException)
	// {
	// Exception exception = null;
	// HttpWebResponse httpResponse = webException.Response as HttpWebResponse;
	//
	// if (httpResponse != null && httpResponse.StatusCode == HttpStatusCode.InternalServerError)
	// {
	// // handle internal server error (500)
	// string failureReason = null;
	// using (Stream responseStream = GetResponseStream(httpResponse,
	// httpResponse.GetResponseStream()))
	// {
	// if (responseStream != null && responseStream.CanRead)
	// {
	// using (StreamReader reader = new StreamReader(responseStream))
	// {
	// failureReason = reader.ReadToEnd();
	// }
	// }
	// }
	//
	// if (failureReason != null)
	// {
	// exception = new WebException(failureReason, webException);
	// }
	// else
	// {
	// exception = webException;
	// }
	// }
	// else
	// {
	// // handle other than internal server error (500)
	// exception = webException;
	// }
	// return exception;
	// }
	//
	// protected void HandleException(Exception e, HttpWebResponse response) {
	// if (response != null) {
	// if (response.StatusCode == HttpStatusCode.Unauthorized) {
	// throw new UnauthorizedAccessException("The username or password is wrong while calling '"
	// + response.Method + " " + response.ResponseUri.ToString() + "'", e);
	// }
	// else {
	// throw new Exception("The http request returned http error '" + (int) response.StatusCode
	// + "(" + response.StatusCode.ToString() + ") while calling '" + response.Method + " "
	// + response.ResponseUri.ToString() + "'", e);
	// }
	// }
	// else {
	// throw new Exception("The http request returned an error", e);
	// }
	// }

	protected InputStream getResponseStream(URLConnection response, InputStream responseStream)
			throws IOException {
		String contentEncoding = response.getContentEncoding();
		if ("gzip".equals(contentEncoding)) {
			return new GZIPInputStream(responseStream);
		}
		else {
			return responseStream;
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected Object convertToExpectedType(Class<?> expectedType, Type genericType, Object result) {
		if (void.class.equals(expectedType) || result == null) {
			return null;
		}
		if (Collection.class.isAssignableFrom(expectedType)) {
			Collection targetCollection = ListUtil.createCollectionOfType(expectedType);

			Class<?> itemType = TypeInfoItemUtil.getElementTypeUsingReflection(expectedType, genericType);

			if (result instanceof Iterable) {
				for (Object item : (Iterable<?>) result) {
					Object convertedItem = conversionHelper.convertValueToType(itemType, item);
					targetCollection.add(convertedItem);
				}
			}
			return targetCollection;
		}
		else if (expectedType.isArray()) {
			ArrayList<Object> list = new ArrayList<>();
			if (result instanceof Iterable) {
				for (Object item : (Iterable<?>) result) {
					list.add(item);
				}
			}

			Object array = Array.newInstance(expectedType.getComponentType(), list.size());
			for (int a = 0, size = list.size(); a < size; a++) {
				Array.set(array, a, list.get(a));
			}
			return array;
		}
		return conversionHelper.convertValueToType(expectedType, result);
	}

	protected void setAuthorization(RequestBuilder request) throws UnsupportedEncodingException {
		String[] authentication = authenticationHolder.getAuthentication();
		String userName = authentication[0];
		String password = authentication[1];
		if (userName == null && password == null) {
			return;
		}
		String authInfo = userName + ":" + password; // userName + ":" +
																									// Encryption.Encrypt(.password;//TODO
																									// Encryption.encrypt(password);
		authInfo = Base64.encodeBytes(authInfo.getBytes("UTF-8"));
		request.setHeader("Authorization", "Basic " + authInfo);
	}

	@Override
	public void beginOnline() {
		beginOffline();
	}

	@Override
	public void handleOnline() {
		handleOffline();
	}

	@Override
	public void endOnline() {
		endOffline();
	}

	@Override
	public void beginOffline() {
		writeLock.lock();
		try {
			connectionChangePending = true;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void handleOffline() {
		// Intended blank
	}

	@Override
	public void endOffline() {
		writeLock.lock();
		try {
			connectionChangePending = false;
			connectionChangeCond.signalAll();
		}
		finally {
			writeLock.unlock();
		}
	}
}
