package de.osthus.ambeth.rest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.cglib.proxy.MethodProxy;

import org.glassfish.jersey.client.ClientConfig;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.remote.IRemoteInterceptor;
import de.osthus.ambeth.service.IOfflineListener;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.transfer.AmbethServiceException;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

public class RESTClientInterceptor extends AbstractSimpleInterceptor implements IRemoteInterceptor, IInitializingBean, IOfflineListener
{

	private Lock clientLock = new ReentrantLock();
	// private Condition clientLockCondition = clientLock.newCondition();

	@Property(name = ServiceConfigurationConstants.ServiceBaseUrl)
	protected String ServiceBaseUrl;

	@Autowired
	protected IAuthenticationHolder authenticationHolder;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXMLHandler;

	protected boolean connectionChangePending = false;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	protected String serviceName;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceName, "ServiceName");
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (guiThreadHelper != null && guiThreadHelper.isInGuiThread())
		{
			throw new Exception("It is not allowed to call this interceptor from GUI thread");
		}

		// MethodInfo method = invocation.Method;
		String restUrl = ServiceBaseUrl + "/" + serviceName;// + "/" + method.getName();

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		WebTarget rootWebTarget = client.target(restUrl);
		WebTarget methodWebTarget = rootWebTarget.path(method.getName());

		Builder restRequest = methodWebTarget.request(MediaType.TEXT_PLAIN);

		Response restResponse;
		if (args == null || args.length == 0)
		{
			// do GET
			restResponse = restRequest.get();
		}
		else
		{
			// do POST
			OutputStream payloadStream = new ByteArrayOutputStream();
			cyclicXMLHandler.writeToStream(payloadStream, args);
			restResponse = restRequest.post(Entity.entity(payloadStream.toString(), MediaType.TEXT_PLAIN));
		}

		InputStream responseStream = (InputStream) restResponse.getEntity(); // this is a stream?
		Object responseEntity = cyclicXMLHandler.readFromStream(responseStream);

		return responseEntity;

		// Object result = null;
		// boolean hasResult = false;
		// Exception ex = null;
		//
		// webRequestLock.lock();
		// try
		// {
		// webRequest.Accept = "text/plain";
		// if (HttpAcceptEncodingZipped)
		// {
		// TryToSetHeader(HttpRequestHeader.AcceptEncoding, webRequest, "gzip");
		// webRequest.Headers["Accept-Encoding-Workaround"] = "gzip";
		// }
		// SetAuthorization(webRequest);
		//
		// if (invocation.Arguments.Length == 0)
		// {
		// webRequest.Method = "GET";
		// webRequest.BeginGetResponse(delegate(IAsyncResult asyncResult2)
		// {
		// try
		// {
		// HttpWebResponse response = (HttpWebResponse)webRequest.EndGetResponse(asyncResult2);
		// using (Stream responseStream = response.GetResponseStream())
		// using (Stream memoryStream = new MemoryStream())
		// {
		// int b;
		// while ((b = responseStream.ReadByte()) != -1)
		// {
		// memoryStream.WriteByte((byte)b);
		// }
		// memoryStream.Position = 0;
		// try
		// {
		// Stream deflateStream = GetResponseStream(response, memoryStream);
		// result = CyclicXmlHandler.ReadFromStream(deflateStream);
		// }
		// catch (XmlTypeNotFoundException)
		// {
		// throw;
		// }
		// catch (Exception)
		// {
		// memoryStream.Position = 0;
		// result = CyclicXmlHandler.ReadFromStream(memoryStream);
		// }
		// }
		// hasResult = true;
		// }
		// catch (WebException e)
		// {
		// ex = ParseWebException(e);
		// using (HttpWebResponse response = (HttpWebResponse)e.Response)
		// {
		// HandleException(e, response);
		// }
		// }
		// catch (Exception e)
		// {
		// Log.Error(e);
		// ex = e;
		// }
		// finally
		// {
		// lock (webRequest)
		// {
		// Monitor.PulseAll(webRequest);
		// }
		// }
		// }, null);
		// }
		// else
		// {
		// webRequest.Method = "POST";
		// webRequest.ContentType = "text/plain";
		// if (HttpContentEncodingZipped)
		// {
		// TryToSetHeader(HttpRequestHeader.ContentEncoding, webRequest, "gzip");
		// webRequest.Headers["Content-Encoding-Workaround"] = "gzip";
		// }
		// webRequest.BeginGetRequestStream(delegate(IAsyncResult asyncResult)
		// {
		// try
		// {
		// // using (Stream stream = webRequest.EndGetRequestStream(asyncResult))
		// //#if SILVERLIGHT
		// // using (Stream deflateStream = HttpContentEncodingZipped ? new GZipStream(stream, CompressionMode.Compress, CompressionLevel.BestCompression,
		// false) : stream)
		// //#else
		// // using (Stream deflateStream = HttpContentEncodingZipped ? new GZipStream(stream, CompressionMode.Compress, false) : stream)
		// //#endif
		// {
		// CyclicXmlHandler.WriteToStream(deflateStream, invocation.Arguments);
		// }
		// webRequest.BeginGetResponse(delegate(IAsyncResult asyncResult2)
		// {
		// try
		// {
		// HttpWebResponse response = (HttpWebResponse)webRequest.EndGetResponse(asyncResult2);
		// using (Stream responseStream = response.GetResponseStream())
		// using (Stream memoryStream = new MemoryStream())
		// {
		// int b;
		// while ((b = responseStream.ReadByte()) != -1)
		// {
		// memoryStream.WriteByte((byte)b);
		// }
		// memoryStream.Position = 0;
		// try
		// {
		// Stream deflateStream = GetResponseStream(response, memoryStream);
		// result = CyclicXmlHandler.ReadFromStream(deflateStream);
		// }
		// catch (XmlTypeNotFoundException)
		// {
		// throw;
		// }
		// catch (Exception)
		// {
		// memoryStream.Position = 0;
		// result = CyclicXmlHandler.ReadFromStream(memoryStream);
		// }
		// }
		// hasResult = true;
		// }
		// catch (WebException e)
		// {
		// ex = ParseWebException(e);
		// using (HttpWebResponse response = (HttpWebResponse)e.Response)
		// {
		// HandleException(e, response);
		// }
		// }
		// catch (Exception e)
		// {
		// ex = e;
		// }
		// finally
		// {
		// webRequestLock.lock();
		// try
		// {
		// // Monitor.PulseAll(webRequest);
		// webRequest.notifyAll();
		// }
		// finally {
		// webRequestLock.unlock();
		// }
		// }
		// }, null);
		// }
		// catch (Exception e)
		// {
		// ex = e;
		// webRequestLock.lock();
		// try
		// {
		// // Monitor.PulseAll(webRequest);
		// webRequest.notifyAll();
		// }
		// finally {
		// webRequestLock.unlock();
		// }
		// }
		// }, null);
		// }
		// while (!hasResult && ex == null)
		// {
		//
		// Monitor.Wait(webRequest);
		// }
		// }
		// finally {
		// webRequestLock.unlock();
		// }
		// if (result is AmbethServiceException)
		// {
		// ex = ParseServiceException((AmbethServiceException)result);
		// throw new Exception("Error occured while calling " + webRequest.Method + " " + webRequest.RequestUri, ex);
		// }
		// if (ex != null)
		// {
		// if (ex is WebException)
		// {
		// throw new Exception(ex.Message + "\r\rError occured while calling " + webRequest.Method + " " + webRequest.RequestUri + ". " +
		// CyclicXmlHandler.Write(invocation.Arguments), ex);
		// }
		// throw new Exception("Error occured while calling " + webRequest.Method + " " + webRequest.RequestUri + ". " +
		// CyclicXmlHandler.Write(invocation.Arguments), ex);
		// }
		// if (!hasResult)
		// {
		// throw new Exception("This must never happen");
		// }
		// invocation.ReturnValue = ConvertToExpectedType(method.ReturnType, result);
	}

	protected Exception ParseServiceException(AmbethServiceException serviceException)
	{
		AmbethServiceException serviceCause = serviceException.getCause();
		Exception cause = null;
		if (serviceCause != null)
		{
			cause = ParseServiceException(serviceCause);
		}
		return new Exception(serviceException.getMessage() + "\n" + serviceException.getStackTrace(), cause);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object ConvertToExpectedType(Class<?> expectedType, Object result)
	{
		// if (typeof(void).Equals(expectedType) || result == null)
		if (result == null) // TODO find .net equivalent?
		{
			return null;
		}
		else if (expectedType.isAssignableFrom(result.getClass()))
		{
			return result;
		}
		if (Collection.class.isAssignableFrom(expectedType)) // TODO: && typeof(String).Equals(expectedType) --> I assume Strings in .net are Enumerable?
		{
			Collection targetCollection = ListUtil.createCollectionOfType(expectedType);

			// MethodInfo addMethod = targetCollection.getClass().getMethod("Add");
			// Class<?> addType = addMethod.getParameters()[0].ParameterType;
			// Object[] parameters = new Object[1];

			if (result instanceof Collection)
			{
				for (Object item : (Collection) result)
				{
					// TODO convert to expected type
					// item = ConversionHelper.ConvertValueToType(addType, item);
					targetCollection.add(item); // we could use addAll, but we might have to use the ConversionHelper for each individual Object
				}
			}
			return targetCollection;
		}
		else if (expectedType.isArray())
		{
			List<Object> list = new ArrayList<Object>();
			if (result instanceof Collection)
			{
				for (Object item : (Collection) result)
				{
					list.add(item);
				}
			}

			// TODO not sure this is possible in java
			// Object[] array = Array.newInstance(expectedType.getElementType(), list.size());
			Object[] array = list.toArray();

			// for (int a = 0, size = list.Count; a < size; a++)
			// {
			// // TODO no conversion done here at all in .Net Code???!!
			// // array.SetValue(list[a], a);
			// }
			return array;
		}
		throw new RuntimeException("Can not convert result " + result + " to expected type " + expectedType.getName());
	}

	// protected void setAuthorization(HttpWebRequest request)
	// {
	// String[] authentication = authenticationHolder.getAuthentication();
	// String userName = authentication[0];
	// String password = authentication[1];
	// if (userName == null && password == null)
	// {
	// return;
	// }
	// String authInfo = userName + ":" + password; // userName + ":" + Encryption.Encrypt(.password;//TODO Encryption.encrypt(password);
	// authInfo = Convert.ToBase64String(Encoding.UTF8.GetBytes(authInfo));
	// request.Headers[HttpRequestHeader.Authorization] = "Basic " + authInfo;
	// }

	@Override
	public void beginOnline()
	{
		beginOffline();
	}

	@Override
	public void handleOnline()
	{
		handleOffline();
	}

	@Override
	public void endOnline()
	{
		endOffline();
	}

	@Override
	public void beginOffline()
	{
		clientLock.lock();
		try
		{
			connectionChangePending = true;
		}
		finally
		{
			clientLock.unlock();
		}
	}

	@Override
	public void handleOffline()
	{
		// Intended blank
	}

	@Override
	public void endOffline()
	{
		clientLock.lock();
		try
		{
			connectionChangePending = false;
			clientLock.notifyAll();
		}
		finally
		{
			clientLock.unlock();
		}
	}

	@Override
	public String getServiceName()
	{
		return serviceName;
	}

	@Override
	public void setServiceName(String serviceName)
	{
		this.serviceName = serviceName;
	}
}