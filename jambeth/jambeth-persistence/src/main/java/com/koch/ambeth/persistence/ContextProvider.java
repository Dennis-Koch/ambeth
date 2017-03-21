package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.api.IContextProvider;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.security.IAuthenticatedUserHolder;
import com.koch.ambeth.security.IAuthorization;
import com.koch.ambeth.security.IAuthorizationChangeListener;
import com.koch.ambeth.util.IInterningFeature;

public class ContextProvider implements IContextProvider, IAuthorizationChangeListener
{
	@Autowired
	protected IAuthenticatedUserHolder authenticatedUserHolder;

	@Autowired
	protected IAlreadyLinkedCache alreadyLinkedCache;

	@Autowired(optional = true)
	protected IInterningFeature interningFeature;

	protected Long currentTime;

	protected String currentUser;

	protected Reference<Thread> boundThread;

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
