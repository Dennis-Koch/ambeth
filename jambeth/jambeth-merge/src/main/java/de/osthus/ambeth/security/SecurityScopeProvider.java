package de.osthus.ambeth.security;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

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
