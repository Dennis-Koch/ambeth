package de.osthus.ambeth.service.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.core.MethodInfo;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.service.IOfflineListener;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.util.ParamChecker;

public class SyncCallInterceptor extends AbstractSimpleInterceptor implements IInitializingBean
{
	public Object asyncService;

	public Class<?> asyncServiceInterface;

	public IOfflineListenerExtendable offlineListenerExtendable;

	public void AfterPropertiesSet()
	{
		ParamChecker.assertNotNull(offlineListenerExtendable, "OfflineListenerExtendable");
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(offlineListenerExtendable, "OfflineListenerExtendable");
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{

		return null;
	}

	public class SyncCallItem implements IOfflineListener
	{
		private MethodInfo asyncEndMethod;

		private Object serviceObject;

		protected Object result;

		protected Exception exceptionResult;

		protected boolean isResultSet;

		protected Object resultSyncObject = new Object();

		public void callbackMethod(/* IAsyncRes */)
		{

		}

		@Override
		public void beginOnline()
		{
		}

		@Override
		public void handleOnline()
		{
		}

		@Override
		public void endOnline()
		{
		}

		@Override
		public void beginOffline()
		{
		}

		@Override
		public void handleOffline()
		{
		}

		@Override
		public void endOffline()
		{
		}

		public MethodInfo getAsyncEndMethod()
		{
			return asyncEndMethod;
		}

		public void setAsyncEndMethod(MethodInfo asyncEndMethod)
		{
			this.asyncEndMethod = asyncEndMethod;
		}

		public Object getServiceObject()
		{
			return serviceObject;
		}

		public void setServiceObject(Object serviceObject)
		{
			this.serviceObject = serviceObject;
		}

		public Object getResult()
		{
			return result;
		}

		public Exception getExceptionResult()
		{
			return exceptionResult;
		}

		public boolean isResultSet()
		{
			return isResultSet;
		}
	}

	public Object getAsyncService()
	{
		return asyncService;
	}

	public void setAsyncService(Object asyncService)
	{
		this.asyncService = asyncService;
	}

	public Class<?> getAsyncServiceInterface()
	{
		return asyncServiceInterface;
	}

	public void setAsyncServiceInterface(Class<?> asyncServiceInterface)
	{
		this.asyncServiceInterface = asyncServiceInterface;
	}

	public IOfflineListenerExtendable getOfflineListenerExtendable()
	{
		return offlineListenerExtendable;
	}

	public void setOfflineListenerExtendable(IOfflineListenerExtendable offlineListenerExtendable)
	{
		this.offlineListenerExtendable = offlineListenerExtendable;
	}
}
