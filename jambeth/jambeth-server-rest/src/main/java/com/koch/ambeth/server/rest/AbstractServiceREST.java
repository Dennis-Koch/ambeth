package com.koch.ambeth.server.rest;

/*-
 * #%L
 * jambeth-server-rest
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.log.AmbethLogger;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.transfer.AmbethServiceException;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.FastByteArrayOutputStream;
import com.koch.ambeth.util.state.AbstractStateRollback;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.XmlModule;

public abstract class AbstractServiceREST {
	/**
	 * this is needed for MS Silverlight applications because in the C# web sandbox it is not allowed
	 * to specify the "Accept-Encoding" header directly.
	 */
	public static final String ACCEPT_ENCODING_WORKAROUND = "Accept-Encoding-Workaround";

	public static final String deflateEncoding = "deflate";

	public static final String gzipEncoding = "gzip";

	public static final String DEFLATE_MIME_TYPE = MediaType.APPLICATION_OCTET_STREAM;

	protected static final Charset utfCharset = Charset.forName("UTF-8");

	protected static final Pattern basicPattern = Pattern.compile("Basic *(.+) *",
			Pattern.CASE_INSENSITIVE);

	protected static final Pattern pattern = Pattern.compile("(.+) *\\: *(.+)");

	private static final Set<String> ignoreExceptions = new HashSet<>();

	static {
		ignoreExceptions.add("org.apache.catalina.connector.ClientAbortException");
	}

	@Context
	protected ServletContext servletContext;

	@Context
	protected HttpHeaders headers;

	protected final AmbethServletAspect aspect = new AmbethServletAspect();

	private ILogger log;

	private IServiceContext beanContext;

	private volatile boolean waitForRebuild;

	private long waitForRebuildMaxDelay = 30000;

	private final Lock writeLock = new ReentrantLock();

	private final Condition rebuildContextCond = writeLock.newCondition();

	public void setBeanContext(IServiceContext beanContext) {
		writeLock.lock();
		try {
			this.beanContext = beanContext;
			aspect.setBeanContext(beanContext);
			log = null;
			if (this.beanContext != null) {
				// notify all paused threads that we now have a valid context (again)
				rebuildContextCond.signalAll();
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	public void prepareBeanContextRebuild() {
		writeLock.lock();
		try {
			beanContext = null;
			waitForRebuild = true;
		}
		finally {
			writeLock.unlock();
		}
	}

	protected ILogger getLog() {
		if (log == null) {
			IProperties properties = getServiceContext().getService(IProperties.class);
			log = LoggerFactory.getLogger(getClass(), properties);
		}
		return log;
	}

	@GET
	@Path("ping")
	@Produces({ MediaType.TEXT_PLAIN })
	public String ping() {
		return "Ping";
	}

	@GET
	@Path("ping2")
	@Produces({ MediaType.TEXT_XML })
	public ObjRef ping2() {
		ObjRef objRef = new ObjRef();
		objRef.setId(5);
		objRef.setIdNameIndex((byte) -1);
		objRef.setVersion(7);
		objRef.setRealType(CreateContainer.class);
		return objRef;
	}

	@SuppressWarnings("unchecked")
	protected <T> IList<T> createList(Class<T> targetType, List<?> list) {
		ArrayList<T> targetList = new ArrayList<>(list.size());
		for (int a = 0, size = list.size(); a < size; a++) {
			targetList.add((T) list.get(a));
		}
		return targetList;
	}

	/**
	 *
	 * @return The singleton IServiceContext which is stored in the context of the servlet
	 */
	protected IServiceContext getServiceContext() {
		writeLock.lock();
		try {
			IServiceContext serviceContext = beanContext;
			long waitUpTo = System.currentTimeMillis() + waitForRebuildMaxDelay;
			while (serviceContext == null && waitForRebuild) {
				try {
					long waitTime = waitUpTo - System.currentTimeMillis();
					if (waitTime <= 0) {
						waitForRebuild = false;
						break;
					}
					rebuildContextCond.await(waitUpTo, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e) {
					Thread.interrupted(); // clear interrupted flag
					continue;
				}
				serviceContext = beanContext;
			}
			if (serviceContext != null) {
				return serviceContext;
			}
		}
		finally {
			writeLock.unlock();
		}
		return (IServiceContext) servletContext
				.getAttribute(AmbethServletListener.ATTRIBUTE_I_SERVICE_CONTEXT);
	}

	protected IStateRollback preServiceCall(final HttpServletRequest request,
			HttpServletResponse response) {
		final ILogger log = getLog();
		final String path = log.isDebugEnabled() ? request.getMethod() + " " + request.getRequestURI()
				: null;
		if (path != null) {
			log.debug("Enter: " + path);
		}
		boolean success = false;
		IStateRollback rollback = aspect.pushServletAspectWithThreadLocals(request, response);
		try {
			if (path != null) {
				rollback = new AbstractStateRollback(rollback) {
					@Override
					protected void rollbackIntern() throws Exception {
						log.debug("Exit:  " + path);
					}
				};
			}
			success = true;
			return rollback;
		}
		finally {
			if (!success) {
				rollback.rollback();
			}
		}
	}

	protected <T> T getService(Class<T> serviceType) {
		return getServiceContext().getService(serviceType);
	}

	protected <T> T getService(String beanName, Class<T> serviceType) {
		return getServiceContext().getService(beanName, serviceType);
	}

	protected String readSingleValueFromHeader(String name) {
		List<String> values = headers.getRequestHeader(name);
		return values != null && !values.isEmpty() ? values.get(0) : null;
	}

	protected List<String> readMultiValueFromHeader(String name) {
		List<String> values = headers.getRequestHeader(name);
		return values != null && !values.isEmpty() ? values : EmptyList.<String>getInstance();
	}

	protected Object[] getArguments(InputStream is, HttpServletRequest request) {
		is = decompressContentIfNecessary(is);

		String contentType = request.getContentType();

		Object args = null;
		if (contentType == null || "application/xml".equals(contentType)
				|| "application/xml+ambeth".equals(contentType)) {
			ICyclicXMLHandler cyclicXmlHandler = getService(XmlModule.CYCLIC_XML_HANDLER,
					ICyclicXMLHandler.class);
			args = cyclicXmlHandler.readFromStream(is);
		}
		else if ("application/json".equals(contentType)) {
			throw new NotSupportedException("'" + contentType + "' not yet supported");

			// ObjectMapper mapper = new ObjectMapper();
			// try
			// {
			// JsonNode node = mapper.readValue(is, JsonNode.class);
			// args = node;
			// }
			// catch (JsonParseException e)
			// {
			// throw new BadRequestException("JSON invalid", e);
			// }
			// catch (JsonMappingException e)
			// {
			// throw new NotSupportedException("JSON not mappable", e);
			// }
			// catch (IOException e)
			// {
			// throw new InternalServerErrorException(e);
			// }
		}
		if (args instanceof Object[]) {
			return (Object[]) args;
		}
		return new Object[] { args };
	}

	protected StreamingOutput createExceptionResult(Throwable e, final HttpServletResponse response) {
		if (e.getClass().getName().equals(MaskingRuntimeException.class.getName())
				&& e.getMessage() == null) {
			e = e.getCause();
		}
		IServiceContext beanContext = this.beanContext;
		if (beanContext != null && beanContext.isRunning() && !beanContext.isDisposing()) {
			logException(e, null);
		}
		AmbethServiceException result = new AmbethServiceException();
		StringBuilder sb = new StringBuilder();
		AmbethLogger.extractFullStackTrace(e, sb);
		result.setMessage(e.getMessage());
		result.setExceptionType(e.getClass().getName());
		result.setStackTrace(sb.toString());

		int errorStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		if (e instanceof SecurityException) {
			errorStatus = HttpServletResponse.SC_FORBIDDEN;
		}
		final int fErrorStatus = errorStatus;
		final StreamingOutput streamingOutput = createResult(result, response);
		response.setStatus(fErrorStatus);
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				response.setStatus(fErrorStatus);
				streamingOutput.write(output);
			}
		};
	}

	protected void writeContent(OutputStream os, Object result) {
		ICyclicXMLHandler cyclicXmlHandler = getService(XmlModule.CYCLIC_XML_HANDLER,
				ICyclicXMLHandler.class);
		cyclicXmlHandler.writeToStream(os, result);
	}

	protected StreamingOutput createResult(Object result, HttpServletResponse response) {
		return createResult(result, response, null, true);
	}

	protected StreamingOutput createSynchronousResult(Object result, HttpServletResponse response) {
		StreamingOutput asyncOutput = createResult(result, response, null, false);
		final FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
		try {
			asyncOutput.write(bos);
			return new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					bos.writeTo(output);
				}
			};
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected StreamingOutput createResult(final Object result, final HttpServletResponse response,
			final IBackgroundWorkerParamDelegate<Throwable> streamingFinishedCallback,
			final boolean cleanupOnFinally) {
		final String contentEncoding = evaluateAcceptedContentEncoding(response);
		return new StreamingOutput() {
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException {
				response.setHeader(HttpHeaders.CONTENT_ENCODING, contentEncoding);
				if (gzipEncoding.equals(contentEncoding)) {
					output = new GZIPOutputStream(output);
				}
				else if (deflateEncoding.equals(contentEncoding)) {
					output = new DeflaterOutputStream(output);
				}
				try {
					writeContent(output, result);
					if (output instanceof DeflaterOutputStream) {
						((DeflaterOutputStream) output).finish();
					}
					if (streamingFinishedCallback != null) {
						streamingFinishedCallback.invoke(null);
					}
				}
				catch (Throwable e) {
					if (streamingFinishedCallback != null) {
						try {
							streamingFinishedCallback.invoke(e);
						}
						catch (Throwable e1) {
							// intended blank
						}
					}
					ILogger log = getLog();
					if (log.isErrorEnabled()) {
						// Reconstruct written stream for debugging purpose
						final StringBuilder sb = new StringBuilder();
						try {
							writeContent(new OutputStream() {
								@Override
								public void write(int b) throws IOException {
									sb.append(b);
								}

								@Override
								public void flush() throws IOException {
									// Intended blank
								}
							}, result);
						}
						catch (RuntimeException ex) {
							// Intended blank
						}
						logException(e, sb);
					}
					if (e instanceof IOException) {
						throw (IOException) e; // no need to mask IOException because of the write() signature
					}
					throw RuntimeExceptionUtil.mask(e);
				}
				finally {
					if (cleanupOnFinally) {
						getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
					}
				}
			}
		};
	}

	protected String evaluateAcceptedContentEncoding(HttpServletResponse response) {
		List<String> acceptEncoding = readMultiValueFromHeader(ACCEPT_ENCODING_WORKAROUND);
		if (acceptEncoding.isEmpty()) {
			acceptEncoding = readMultiValueFromHeader(HttpHeaders.ACCEPT_ENCODING);
		}
		for (int a = acceptEncoding.size(); a-- > 0;) {
			acceptEncoding.set(a, acceptEncoding.get(a).toLowerCase());
		}
		if (acceptEncoding.contains(deflateEncoding)) {
			response.setHeader(HttpHeaders.CONTENT_ENCODING, deflateEncoding);
			return deflateEncoding;
		}
		else if (acceptEncoding.contains(gzipEncoding)) {
			response.setHeader(HttpHeaders.CONTENT_ENCODING, gzipEncoding);
			return gzipEncoding;
		}
		return "text/xml";
	}

	protected InputStream decompressContentIfNecessary(InputStream is) {
		String contentEncoding = readSingleValueFromHeader("Content-Encoding-Workaround");
		if (contentEncoding == null || contentEncoding.length() == 0) {
			contentEncoding = readSingleValueFromHeader(HttpHeaders.CONTENT_ENCODING);
		}
		if (contentEncoding == null) {
			contentEncoding = "";
		}
		contentEncoding = contentEncoding.toLowerCase();
		if (deflateEncoding.equals(contentEncoding)) {
			return new InflaterInputStream(is);
		}
		else if (gzipEncoding.equals(contentEncoding)) {
			try {
				return new GZIPInputStream(is);
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return is;
	}

	protected void logException(Throwable e, StringBuilder sb) {
		if (e instanceof MaskingRuntimeException) {
			MaskingRuntimeException mre = (MaskingRuntimeException) e;
			if (mre.getMessage() == null) {
				logException(e.getCause(), sb);
				return;
			}
		}
		if (ignoreExceptions.contains(e.getClass().getName())) {
			return;
		}
		ILogger log = getLog();
		if (log.isErrorEnabled()) {
			if (sb != null) {
				log.error(sb.toString(), e);
			}
			else {
				log.error(e);
			}
		}
	}
}
