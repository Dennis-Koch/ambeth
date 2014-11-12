package de.osthus.ambeth.rest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
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
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.remote.IRemoteInterceptor;
import de.osthus.ambeth.service.IOfflineListener;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.transfer.AmbethServiceException;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

public class RESTClientInterceptor extends AbstractSimpleInterceptor implements IRemoteInterceptor, IOfflineListener, IDisposableBean
{

	private Lock clientLock = new ReentrantLock();

	@Autowired
	protected IAuthenticationHolder authenticationHolder;

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXMLHandler;

	protected boolean connectionChangePending = false;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	// @Autowired
	// protected IThreadPool threadPool;

	@Property(name = ServiceConfigurationConstants.ServiceBaseUrl)
	protected String ServiceBaseUrl;

	@Property
	protected String serviceName;

	protected final Lock writeLock = new ReentrantLock();

	protected final Condition destroyedCondition = writeLock.newCondition();

	protected final IdentityHashSet<Thread> waitingThreads = new IdentityHashSet<Thread>();

	protected volatile boolean destroyed;

	@Override
	public void destroy() throws Throwable
	{
		writeLock.lock();
		try
		{
			destroyed = true;
			for (Thread waitingThread : waitingThreads)
			{
				waitingThread.interrupt();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, final Object[] args, MethodProxy proxy) throws Throwable
	{
		if (guiThreadHelper != null && guiThreadHelper.isInGuiThread())
		{
			throw new Exception("It is not allowed to call this interceptor from GUI thread");
		}
		if (destroyed)
		{
			throw new IllegalStateException();
		}

		// MethodInfo method = invocation.Method;
		String restUrl = ServiceBaseUrl + "/" + serviceName;// + "/" + method.getName();

		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		setAuthorization(client);

		WebTarget rootWebTarget = client.target(restUrl);
		WebTarget methodWebTarget = rootWebTarget.path(method.getName());

		final Builder restRequest = methodWebTarget.request(MediaType.TEXT_PLAIN);

		Response restResponse;
		if (args == null || args.length == 0)
		{
			// do GET
			restResponse = restRequest.get();
		}
		else
		{
			// do POST
			final PipedInputStream pis = new PipedInputStream(1024);
			OutputStream os = new PipedOutputStream(pis);

			final ParamHolder<Object> responseHolder = new ParamHolder<Object>();
			final CountDownLatch latch = new CountDownLatch(1);

			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					writeLock.lock();
					try
					{
						waitingThreads.add(Thread.currentThread());
					}
					finally
					{
						writeLock.unlock();
					}
					try
					{
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						int oneByte;
						while ((oneByte = pis.read()) != -1)
						{
							bos.write(oneByte);
						}
						// Response response = restRequest.post(Entity.entity(pis, MediaType.TEXT_PLAIN));
						Response response = restRequest.post(Entity.entity(bos.toByteArray(), MediaType.TEXT_PLAIN));
						responseHolder.setValue(response);
					}
					catch (Throwable e)
					{
						responseHolder.setValue(e);
					}
					finally
					{
						latch.countDown();
					}
				}
			});
			thread.setName("REST Request " + restRequest.toString());
			thread.setDaemon(true);
			thread.start();

			writeLock.lock();
			try
			{
				waitingThreads.add(Thread.currentThread());
			}
			finally
			{
				writeLock.unlock();
			}
			try
			{
				try
				{
					cyclicXMLHandler.writeToStream(os, args);
				}
				finally
				{
					os.close();
				}

				long waitAmount = 60 * 1000;
				long waitTill = System.currentTimeMillis() + waitAmount;
				while (!destroyed || waitAmount <= 0)
				{
					try
					{
						if (latch.await(waitAmount, TimeUnit.MILLISECONDS))
						{
							break;
						}
					}
					catch (InterruptedException e)
					{
						// intended blank
					}
					waitAmount = waitTill - System.currentTimeMillis();
				}
				if (destroyed)
				{
					throw new IllegalStateException();
				}
				if (responseHolder.getValue() instanceof Throwable)
				{
					throw RuntimeExceptionUtil.mask((Throwable) responseHolder.getValue());
				}
				restResponse = (Response) responseHolder.getValue();
				if (restResponse == null)
				{
					return new TimeoutException();
				}
			}
			finally
			{
				writeLock.lock();
				try
				{
					waitingThreads.remove(Thread.currentThread());
				}
				finally
				{
					writeLock.unlock();
				}
			}
			// restResponse = restRequest.post(Entity.entity(payloadStream.toString(), MediaType.TEXT_PLAIN));
			// TODO streamin api?
		}

		InputStream responseStream = (InputStream) restResponse.getEntity();
		if (responseStream.available() > 0)
		{
			Object responseEntity = cyclicXMLHandler.readFromStream(responseStream);
			if (responseEntity instanceof AmbethServiceException)
			{
				throw ParseServiceException((AmbethServiceException) responseEntity);
			}
			else
			{
				return convertToExpectedType(method.getReturnType(), responseEntity);
			}
		}
		else
		{
			return null;
		}
	}

	protected Throwable ParseServiceException(AmbethServiceException serviceException)
	{
		AmbethServiceException serviceCause = serviceException.getCause();
		Throwable cause = null;
		if (serviceCause != null)
		{
			cause = ParseServiceException(serviceCause);
		}
		try
		{
			Class<?> exceptionType = Thread.currentThread().getContextClassLoader().loadClass(serviceException.getExceptionType());
			if (cause == null)
			{
				Constructor<?> constructor = exceptionType.getConstructor(String.class);
				return (Throwable) constructor.newInstance(serviceException.getMessage() + "\n" + serviceException.getStackTrace());
			}
			Constructor<?> constructor = exceptionType.getConstructor(String.class, Throwable.class);
			return (Throwable) constructor.newInstance(serviceException.getMessage() + "\n" + serviceException.getStackTrace(), cause);
		}
		catch (Throwable e)
		{
			// intended blank
		}
		return new RuntimeException(serviceException.getMessage() + "\n" + serviceException.getStackTrace(), cause);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object convertToExpectedType(Class<?> expectedType, Object result)
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

	protected void setAuthorization(Client client)
	{
		String[] authentication = authenticationHolder.getAuthentication();
		String userName = authentication[0];
		String password = authentication[1];
		if (userName == null && password == null)
		{
			return;
		}
		// authInfo = DatatypeConverter.printBase64Binary(authInfo.getBytes(StandardCharsets.UTF_8));
		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic(userName, password);
		client.register(authFeature);
	}

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