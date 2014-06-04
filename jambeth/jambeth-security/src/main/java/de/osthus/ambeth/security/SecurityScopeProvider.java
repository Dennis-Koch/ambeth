package de.osthus.ambeth.security;

import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class SecurityScopeProvider implements IThreadLocalCleanupBean, ISecurityScopeProvider, ISecurityScopeChangeListenerExtendable
{
	public static final ISecurityScope[] defaultSecurityScopes = new ISecurityScope[0];

	protected final ThreadLocal<SecurityScopeHandle> securityScopeTL = new SensitiveThreadLocal<SecurityScopeHandle>();

	protected final DefaultExtendableContainer<ISecurityScopeChangeListener> securityScopeChangeListeners = new DefaultExtendableContainer<ISecurityScopeChangeListener>(ISecurityScopeChangeListener.class, "securityScopeChangeListener");
	
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
	public IUserHandle getUserHandle()
	{
		SecurityScopeHandle securityScopeHandle = securityScopeTL.get();
		if (securityScopeHandle == null)
		{
			return null;
		}
		return securityScopeHandle.userHandle;
	}

	@Override
	public void setUserHandle(IUserHandle userHandle)
	{
		SecurityScopeHandle securityScopeHandle = securityScopeTL.get();
		if (securityScopeHandle == null)
		{
			securityScopeHandle = new SecurityScopeHandle();
			securityScopeTL.set(securityScopeHandle);
		}
		securityScopeHandle.userHandle = userHandle;
		notifySecurityScopeChangeListeners(securityScopeHandle);
	}
	
	protected void notifySecurityScopeChangeListeners(SecurityScopeHandle securityScopeHandle)
	{
		for (ISecurityScopeChangeListener securityScopeChangeListener : securityScopeChangeListeners.getExtensions())
		{
			securityScopeChangeListener.securityScopeChanged(securityScopeHandle.userHandle, securityScopeHandle.securityScopes);
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