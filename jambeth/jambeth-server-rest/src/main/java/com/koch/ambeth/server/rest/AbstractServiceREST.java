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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.IAuthentication;
import com.koch.ambeth.security.ISecurityContext;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.service.transfer.AmbethServiceException;
import com.koch.ambeth.util.Base64;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
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

	private static final Set<String> ignoreExceptions = new HashSet<String>();

	static {
		ignoreExceptions.add("org.apache.catalina.connector.ClientAbortException");
	}

	@Context
	protected ServletContext servletContext;

	@Context
	protected HttpHeaders headers;

	protected final Charset utfCharset = Charset.forName("UTF-8");

	protected final Pattern basicPattern = Pattern.compile("Basic *(.+) *", Pattern.CASE_INSENSITIVE);

	protected final Pattern pattern = Pattern.compile("(.+) *\\: *(.+)");

	private ILogger log;

	protected ILogger getLog() {
		if (log == null) {
			IProperties properties = getServiceContext().getService(IProperties.class);
			log = LoggerFactory.getLogger(getClass(), properties);
		}
		return log;
	}

	@GET
	@Path("ping")
	@Produces({MediaType.TEXT_PLAIN})
	public String ping() {
		return "Ping";
	}

	@GET
	@Path("ping2")
	@Produces({MediaType.TEXT_XML})
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
		ArrayList<T> targetList = new ArrayList<T>(list.size());
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
		return (IServiceContext) servletContext
				.getAttribute(AmbethServletListener.ATTRIBUTE_I_SERVICE_CONTEXT);
	}

	protected void preServiceCall() {
		IAuthentication authentication = readAuthentication();

		if (authentication != null) {
			setAuthentication(authentication);
		}
	}

	protected IAuthentication readAuthentication() {
		String value = readSingleValueFromHeader("Authorization");

		String userName = null;
		char[] userPass = null;
		if (value != null) {
			Matcher basicMatcher = basicPattern.matcher(value);
			if (!basicMatcher.matches()) {
				throw new IllegalStateException(value);
			}
			String group = basicMatcher.group(1);
			byte[] decodedAuthorization = Base64.decodeBase64(group.getBytes(utfCharset));

			String decodedValue = new String(decodedAuthorization, utfCharset);

			Matcher matcher = pattern.matcher(decodedValue);
			if (!matcher.matches()) {
				throw new IllegalStateException(decodedValue);
			}
			userName = matcher.group(1);
			userPass = matcher.group(2).toCharArray();
		}
		return new DefaultAuthentication(userName, userPass, PasswordType.PLAIN);
	}

	protected void setAuthentication(IAuthentication authentication) {
		IServiceContext beanContext = getServiceContext();

		ISecurityContextHolder securityContextHolder =
				beanContext.getService(ISecurityContextHolder.class, false);
		if (securityContextHolder != null) {
			ISecurityContext securityContext = securityContextHolder.getCreateContext();
			securityContext.setAuthentication(authentication);
		}
		else {
			ILogger log = getLog();
			if (log.isInfoEnabled()) {
				ILoggerHistory loggerHistory = getService(ILoggerHistory.class);
				loggerHistory.infoOnce(log, this,
						"No security context holder available. Skip creating security Context!");
			}
		}
	}

	protected void postServiceCall() {
		postServiceCall(servletContext);
	}

	protected void postServiceCall(ServletContext servletContext) {
		IServiceContext beanContext = getServiceContext();

		ISecurityContextHolder securityContextHolder =
				beanContext.getService(ISecurityContextHolder.class, false);
		if (securityContextHolder != null) {
			securityContextHolder.clearContext();
		}
		else {
			getLog().info("No security context holder available. Skip clearing security context!");
		}
		beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
	}

	protected <T> T getService(Class<T> serviceType) {
		return getServiceContext().getService(serviceType);
	}

	protected <T> T getService(String beanName, Class<T> serviceType) {
		return getServiceContext().getService(beanName, serviceType);
	}

	protected String readSingleValueFromHeader(String name) {
		List<String> values = headers.getRequestHeader(name);
		return values != null && values.size() > 0 ? values.get(0) : null;
	}

	protected List<String> readMultiValueFromHeader(String name) {
		List<String> values = headers.getRequestHeader(name);
		return values != null && values.size() > 0 ? values : EmptyList.<String>getInstance();
	}

	protected Object[] getArguments(InputStream is, HttpServletRequest request) {
		is = decompressContentIfNecessary(is);

		String contentType = request.getContentType();

		Object args = null;
		if (contentType == null || contentType.equals("application/xml")
				|| contentType.equals("application/xml+ambeth")) {
			ICyclicXMLHandler cyclicXmlHandler =
					getService(XmlModule.CYCLIC_XML_HANDLER, ICyclicXMLHandler.class);
			args = cyclicXmlHandler.readFromStream(is);
		}
		else if (contentType.equals("application/json")) {
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
		return new Object[] {args};
	}

	protected StreamingOutput createExceptionResult(Throwable e, final HttpServletResponse response) {
		logException(e, null);
		AmbethServiceException result = new AmbethServiceException();

		if (e instanceof MaskingRuntimeException && e.getMessage() == null) {
			e = e.getCause();
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, false);
		e.printStackTrace(pw);
		pw.flush();
		result.setMessage(e.getMessage());
		result.setStackTrace(sw.toString());
		return createResult(result, response);
	}

	protected void writeContent(OutputStream os, Object result) {
		ICyclicXMLHandler cyclicXmlHandler =
				getService(XmlModule.CYCLIC_XML_HANDLER, ICyclicXMLHandler.class);
		cyclicXmlHandler.writeToStream(os, result);
	}

	protected StreamingOutput createResult(final Object result, final HttpServletResponse response) {
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
				}
				catch (RuntimeException e) {
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
					throw e;
				}
				finally {
					getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
				}
			}
		};
	}

	protected String evaluateAcceptedContentEncoding(HttpServletResponse response) {
		List<String> acceptEncoding = readMultiValueFromHeader(ACCEPT_ENCODING_WORKAROUND);
		if (acceptEncoding.size() == 0) {
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

	protected void executeRequest(IBackgroundWorkerDelegate runnable) {
		preServiceCall();
		try {
			runnable.invoke();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			postServiceCall();
		}
	}
}
