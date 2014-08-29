package de.osthus.ambeth.webservice;

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
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication;
import de.osthus.ambeth.security.IAuthentication.PasswordType;
import de.osthus.ambeth.security.ISecurityContext;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.transfer.AmbethServiceException;
import de.osthus.ambeth.util.Base64;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

public abstract class AbstractServiceREST
{
	public static final String deflateEncoding = "deflate";

	public static final String gzipEncoding = "gzip";

	public static final String DEFLATE_MIME_TYPE = MediaType.APPLICATION_OCTET_STREAM;

	private static final Set<String> ignoreExceptions = new HashSet<String>();

	static
	{
		ignoreExceptions.add("org.apache.catalina.connector.ClientAbortException");
	}

	@Context
	protected ServletContext servletContext;

	@Context
	protected HttpHeaders headers;

	@Context
	protected HttpServletResponse response;

	protected final Charset utfCharset = Charset.forName("UTF-8");

	protected final Pattern basicPattern = Pattern.compile("Basic *(.+) *", Pattern.CASE_INSENSITIVE);

	protected final Pattern pattern = Pattern.compile("(.+) *\\: *(.+)");

	private ILogger log;

	protected ILogger getLog()
	{
		if (log == null)
		{
			IProperties properties = getServiceContext().getService(IProperties.class);
			log = LoggerFactory.getLogger(getClass(), properties);
		}
		return log;
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	public String ping()
	{
		return "Ping";
	}

	@GET
	@Path("json")
	@Produces({ MediaType.TEXT_XML })
	public ObjRef ping2()
	{
		ObjRef objRef = new ObjRef();
		objRef.setId(5);
		objRef.setIdNameIndex((byte) -1);
		objRef.setVersion(7);
		objRef.setRealType(CreateContainer.class);
		return objRef;
	}

	@SuppressWarnings("unchecked")
	protected <T> IList<T> createList(Class<T> targetType, List<?> list)
	{
		ArrayList<T> targetList = new ArrayList<T>(list.size());
		for (int a = 0, size = list.size(); a < size; a++)
		{
			targetList.add((T) list.get(a));
		}
		return targetList;
	}

	/**
	 * 
	 * @return The singleton IServiceContext which is stored in the context of the servlet
	 */
	protected IServiceContext getServiceContext()
	{
		return (IServiceContext) servletContext.getAttribute(AmbethServletListener.ATTRIBUTE_I_SERVICE_CONTEXT);
	}

	protected void preServiceCall()
	{
		IAuthentication authentication = readAuthentication();
		if (authentication != null)
		{
			setAuthentication(authentication);
		}
	}

	protected IAuthentication readAuthentication()
	{
		String value = readSingleValueFromHeader("Authorization");

		String userName = null;
		char[] userPass = null;
		if (value != null)
		{
			Matcher basicMatcher = basicPattern.matcher(value);
			if (!basicMatcher.matches())
			{
				throw new IllegalStateException(value);
			}
			String group = basicMatcher.group(1);
			byte[] decodedAuthorization = Base64.decodeBase64(group.getBytes(utfCharset));

			String decodedValue = new String(decodedAuthorization, utfCharset);

			Matcher matcher = pattern.matcher(decodedValue);
			if (!matcher.matches())
			{
				throw new IllegalStateException(decodedValue);
			}
			userName = matcher.group(1);
			userPass = matcher.group(2).toCharArray();
		}
		return new DefaultAuthentication(userName, userPass, PasswordType.PLAIN);
	}

	protected void setAuthentication(IAuthentication authentication)
	{
		ISecurityContext securityContext = getServiceContext().getService(ISecurityContextHolder.class).getCreateContext();
		securityContext.setAuthentication(authentication);
	}

	protected void postServiceCall()
	{
		postServiceCall(servletContext);
	}

	protected void postServiceCall(ServletContext servletContext)
	{
		IServiceContext beanContext = getServiceContext();
		beanContext.getService(ISecurityContextHolder.class).clearContext();
		beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
	}

	protected <T> T getService(Class<T> serviceType)
	{
		return getServiceContext().getService(serviceType);
	}

	protected <T> T getService(String beanName, Class<T> serviceType)
	{
		return getServiceContext().getService(beanName, serviceType);
	}

	protected String readSingleValueFromHeader(String name)
	{
		List<String> values = headers.getRequestHeader(name);
		return values != null && values.size() > 0 ? values.get(0) : null;
	}

	protected List<String> readMultiValueFromHeader(String name)
	{
		List<String> values = headers.getRequestHeader(name);
		return values != null && values.size() > 0 ? values : EmptyList.<String> getInstance();
	}

	protected Object[] getArguments(InputStream is)
	{
		is = decompressContentIfNecessary(is);
		ICyclicXMLHandler cyclicXmlHandler = getService(XmlModule.CYCLIC_XML_HANDLER, ICyclicXMLHandler.class);
		return (Object[]) cyclicXmlHandler.readFromStream(is);
	}

	protected StreamingOutput createExceptionResult(Throwable e)
	{
		logException(e, null);
		AmbethServiceException result = new AmbethServiceException();

		if (e instanceof MaskingRuntimeException && e.getMessage() == null)
		{
			e = e.getCause();
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, false);
		e.printStackTrace(pw);
		pw.flush();
		result.setMessage(e.getMessage());
		result.setStackTrace(sw.toString());
		return createResult(result);
	}

	protected StreamingOutput createResult(final Object result)
	{
		final String contentEncoding = evaluateAcceptedContentEncoding();
		final ICyclicXMLHandler cyclicXmlHandler = getService(XmlModule.CYCLIC_XML_HANDLER, ICyclicXMLHandler.class);
		return new StreamingOutput()
		{
			@Override
			public void write(OutputStream output) throws IOException, WebApplicationException
			{
				response.setHeader(HttpHeaders.CONTENT_ENCODING, contentEncoding);
				if (gzipEncoding.equals(contentEncoding))
				{
					output = new GZIPOutputStream(output);
				}
				else if (deflateEncoding.equals(contentEncoding))
				{
					output = new DeflaterOutputStream(output);
				}
				try
				{
					cyclicXmlHandler.writeToStream(output, result);
					if (output instanceof DeflaterOutputStream)
					{
						((DeflaterOutputStream) output).finish();
					}
				}
				catch (RuntimeException e)
				{
					ILogger log = getLog();
					if (log.isErrorEnabled())
					{
						// Reconstruct written stream for debugging purpose
						final StringBuilder sb = new StringBuilder();
						try
						{
							cyclicXmlHandler.writeToStream(new OutputStream()
							{
								@Override
								public void write(int b) throws IOException
								{
									sb.append(b);
								}

								@Override
								public void flush() throws IOException
								{
									// Intended blank
								}
							}, result);
						}
						catch (RuntimeException ex)
						{
							// Intended blank
						}
						logException(e, sb);
					}
					throw e;
				}
				finally
				{
					getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
				}
			}
		};
	}

	protected String evaluateAcceptedContentEncoding()
	{
		List<String> acceptEncoding = readMultiValueFromHeader("Accept-Encoding-Workaround");
		if (acceptEncoding.size() == 0)
		{
			acceptEncoding = readMultiValueFromHeader(HttpHeaders.ACCEPT_ENCODING);
		}
		for (int a = acceptEncoding.size(); a-- > 0;)
		{
			acceptEncoding.set(a, acceptEncoding.get(a).toLowerCase());
		}
		if (acceptEncoding.contains(deflateEncoding))
		{
			response.setHeader(HttpHeaders.CONTENT_ENCODING, deflateEncoding);
			return deflateEncoding;
		}
		else if (acceptEncoding.contains(gzipEncoding))
		{
			response.setHeader(HttpHeaders.CONTENT_ENCODING, gzipEncoding);
			return gzipEncoding;
		}
		return "text/xml";
	}

	protected InputStream decompressContentIfNecessary(InputStream is)
	{
		String contentEncoding = readSingleValueFromHeader("Content-Encoding-Workaround");
		if (contentEncoding == null || contentEncoding.length() == 0)
		{
			contentEncoding = readSingleValueFromHeader(HttpHeaders.CONTENT_ENCODING);
		}
		if (contentEncoding == null)
		{
			contentEncoding = "";
		}
		contentEncoding = contentEncoding.toLowerCase();
		if (deflateEncoding.equals(contentEncoding))
		{
			return new InflaterInputStream(is);
		}
		else if (gzipEncoding.equals(contentEncoding))
		{
			try
			{
				return new GZIPInputStream(is);
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return is;
	}

	protected void logException(Throwable e, StringBuilder sb)
	{
		if (e instanceof MaskingRuntimeException)
		{
			MaskingRuntimeException mre = (MaskingRuntimeException) e;
			if (mre.getMessage() == null)
			{
				logException(e.getCause(), sb);
				return;
			}
		}
		if (ignoreExceptions.contains(e.getClass().getName()))
		{
			return;
		}
		ILogger log = getLog();
		if (log.isErrorEnabled())
		{
			if (sb != null)
			{
				log.error(sb.toString(), e);
			}
			else
			{
				log.error(e);
			}
		}
	}

	protected void executeRequest(IBackgroundWorkerDelegate runnable)
	{
		preServiceCall();
		try
		{
			runnable.invoke();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			postServiceCall();
		}
	}
}
