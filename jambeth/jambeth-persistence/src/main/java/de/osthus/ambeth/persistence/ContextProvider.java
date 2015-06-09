package de.osthus.ambeth.persistence;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.IAuthorizationChangeListener;
import de.osthus.ambeth.security.IAuthenticatedUserHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IInterningFeature;

public class ContextProvider implements IContextProvider, IAuthorizationChangeListener
{
	protected Long currentTime;

	protected String currentUser;

	protected Reference<Thread> boundThread;

	@Autowired
	protected IAuthenticatedUserHolder authenticatedUserHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired(optional = true)
	protected IInterningFeature interningFeature;

	@Override
	public void acquired()
	{
		boundThread = new WeakReference<Thread>(Thread.currentThread());
		setCurrentUser(authenticatedUserHolder.getAuthenticatedSID());
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
	public void clear()
	{
		clearAfterMerge();
		currentTime = null;
		currentUser = null;
		boundThread = null;
	}

	@Override
	public void clearAfterMerge()
	{
		alreadyLinkedCache.clear();
	}

	@Override
	public void authorizationChanged(IAuthorization authorization)
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
		String user = authorization != null ? authorization.getSID() : null;
		setCurrentUser(user);
	}

}
