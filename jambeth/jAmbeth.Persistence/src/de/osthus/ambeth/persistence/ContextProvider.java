package de.osthus.ambeth.persistence;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.security.ISecurityScopeChangeListener;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.IUserHandle;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IAlreadyLoadedCache;
import de.osthus.ambeth.util.IInterningFeature;

public class ContextProvider implements IContextProvider, ISecurityScopeChangeListener
{
	protected Long currentTime;

	protected String currentUser;
	
	protected Reference<Thread> boundThread;
	
	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected IAlreadyLoadedCache alreadyLoadedCache;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired(optional = true)
	protected IInterningFeature interningFeature;

	@Override
	public void acquired()
	{
		boundThread = new WeakReference<Thread>(Thread.currentThread());
	}
	
	@Override
	public Long getCurrentTime()
	{
		return currentTime;
	}

	@Override
	public void setCurrentTime(Long currentTime)
	{
		this.currentTime = currentTime;
	}

	@Override
	public String getCurrentUser()
	{
		if (currentUser == null)
		{
			return "anonymous";
		}
		return currentUser;
	}

	@Override
	public void setCurrentUser(String currentUser)
	{
		IInterningFeature interningFeature = this.interningFeature;
		if (interningFeature != null)
		{
			currentUser = interningFeature.intern(currentUser);
		}
		this.currentUser = currentUser;
	}

	@Override
	public IAlreadyLoadedCache getAlreadyLoadedCache()
	{
		return alreadyLoadedCache;
	}

	@Override
	public void clear()
	{
		alreadyLoadedCache.clear();
		alreadyLinkedCache.clear();
		currentTime = null;
		currentUser = null;
		boundThread = null;
	}
	
	@Override
	public void securityScopeChanged(IUserHandle userHandle, ISecurityScope[] securityScopes)
	{
		if (boundThread == null)
		{
			// currently inactive
			return;
		}
		if (boundThread.get() != Thread.currentThread())
		{
			// other thread
			return;
		}
		String user = userHandle != null ? userHandle.getSID() : null;
		setCurrentUser(user);
	}
}
