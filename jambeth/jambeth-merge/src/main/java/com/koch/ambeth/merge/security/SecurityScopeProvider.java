package com.koch.ambeth.merge.security;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.service.model.ISecurityScope;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class SecurityScopeProvider implements IThreadLocalCleanupBean, ISecurityScopeProvider, ISecurityScopeChangeListenerExtendable
{
	public static final ISecurityScope[] defaultSecurityScopes = new ISecurityScope[0];

	@Forkable
	protected final ThreadLocal<SecurityScopeHandle> securityScopeTL = new SensitiveThreadLocal<SecurityScopeHandle>();

	protected final DefaultExtendableContainer<ISecurityScopeChangeListener> securityScopeChangeListeners = new DefaultExtendableContainer<ISecurityScopeChangeListener>(
			ISecurityScopeChangeListener.class, "securityScopeChangeListener");

	@Override
	public void cleanupThreadLocal()
	{
		securityScopeTL.remove();
	}

	@Override
	public ISecurityScope[] getSecurityScopes()
	{
		SecurityScopeHandle securityScopeHandle = securityScopeTL.get();
		if (securityScopeHandle == null)
		{
			return defaultSecurityScopes;
		}
		if (securityScopeHandle.securityScopes == null)
		{
			return defaultSecurityScopes;
		}
		return securityScopeHandle.securityScopes;
	}

	@Override
	public void setSecurityScopes(ISecurityScope[] securityScopes)
	{
		SecurityScopeHandle securityScopeHandle = securityScopeTL.get();
		if (securityScopeHandle == null)
		{
			securityScopeHandle = new SecurityScopeHandle();
			securityScopeTL.set(securityScopeHandle);
		}
		securityScopeHandle.securityScopes = securityScopes;
		notifySecurityScopeChangeListeners(securityScopeHandle);
	}

	@Override
	public <R, V> R executeWithSecurityScopes(IResultingBackgroundWorkerParamDelegate<R, V> runnable, V state, ISecurityScope... securityScopes)
			throws Throwable
	{
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try
		{
			setSecurityScopes(securityScopes);
			return runnable.invoke(state);
		}
		finally
		{
			setSecurityScopes(oldSecurityScopes);
		}
	}

	@Override
	public <R> R executeWithSecurityScopes(IResultingBackgroundWorkerDelegate<R> runnable, ISecurityScope... securityScopes) throws Throwable
	{
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try
		{
			setSecurityScopes(securityScopes);
			return runnable.invoke();
		}
		finally
		{
			setSecurityScopes(oldSecurityScopes);
		}
	}

	@Override
	public <V> void executeWithSecurityScopes(IBackgroundWorkerParamDelegate<V> runnable, V state, ISecurityScope... securityScopes) throws Throwable
	{
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try
		{
			setSecurityScopes(securityScopes);
			runnable.invoke(state);
		}
		finally
		{
			setSecurityScopes(oldSecurityScopes);
		}
	}

	@Override
	public void executeWithSecurityScopes(IBackgroundWorkerDelegate runnable, ISecurityScope... securityScopes) throws Throwable
	{
		ISecurityScope[] oldSecurityScopes = getSecurityScopes();
		try
		{
			setSecurityScopes(securityScopes);
			runnable.invoke();
		}
		finally
		{
			setSecurityScopes(oldSecurityScopes);
		}
	}

	protected void notifySecurityScopeChangeListeners(SecurityScopeHandle securityScopeHandle)
	{
		for (ISecurityScopeChangeListener securityScopeChangeListener : securityScopeChangeListeners.getExtensions())
		{
			securityScopeChangeListener.securityScopeChanged(securityScopeHandle.securityScopes);
		}
	}

	@Override
	public void registerSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener)
	{
		securityScopeChangeListeners.register(securityScopeChangeListener);
	}

	@Override
	public void unregisterSecurityScopeChangeListener(ISecurityScopeChangeListener securityScopeChangeListener)
	{
		securityScopeChangeListeners.unregister(securityScopeChangeListener);
	}
}
