package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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

import java.util.Map;

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.model.IUpdateItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;

public class MergeHandle
{
	protected final IdentityHashSet<Object> alreadyProcessedSet = new IdentityHashSet<Object>();
	protected final IdentityHashMap<Object, IObjRef> objToOriDict = new IdentityHashMap<Object, IObjRef>();
	protected final HashMap<IObjRef, Object> oriToObjDict = new HashMap<IObjRef, Object>();
	protected final IdentityLinkedMap<Object, IList<IUpdateItem>> objToModDict = new IdentityLinkedMap<Object, IList<IUpdateItem>>();

	protected final ArrayList<IObjRef> oldOrList = new ArrayList<IObjRef>();
	protected final ArrayList<IObjRef> newOrList = new ArrayList<IObjRef>();

	protected ICache cache;

	protected ICache privilegedCache;

	protected boolean isCacheToDispose;

	protected boolean isPrivilegedCacheToDispose;

	@Property(name = MergeConfigurationConstants.FieldBasedMergeActive, defaultValue = "true")
	protected boolean fieldBasedMergeActive;

	protected boolean handleExistingIdAsNewId;

	protected boolean isDeepMerge = true;

	protected final IdentityHashSet<Object> objToDeleteSet = new IdentityHashSet<Object>();

	protected final ArrayList<Object> pendingValueHolders = new ArrayList<Object>();

	protected final ArrayList<Runnable> pendingRunnables = new ArrayList<Runnable>();

	public boolean isFieldBasedMergeActive()
	{
		return fieldBasedMergeActive;
	}

	public Map<Object, IObjRef> getObjToOriDict()
	{
		return objToOriDict;
	}

	public Map<IObjRef, Object> getOriToObjDict()
	{
		return oriToObjDict;
	}

	public ISet<Object> getObjToDeleteSet()
	{
		return objToDeleteSet;
	}

	public ICache getCache()
	{
		return cache;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public ICache getPrivilegedCache()
	{
		return privilegedCache;
	}

	public void setPrivilegedCache(ICache privilegedCache)
	{
		this.privilegedCache = privilegedCache;
	}

	public IList<IObjRef> getOldOrList()
	{
		return oldOrList;
	}

	public IList<IObjRef> getNewOrList()
	{
		return newOrList;
	}

	public IList<Runnable> getPendingRunnables()
	{
		return pendingRunnables;
	}

	public IList<Object> getPendingValueHolders()
	{
		return pendingValueHolders;
	}

	public boolean isDeepMerge()
	{
		return isDeepMerge;
	}

	public void setDeepMerge(boolean isDeepMerge)
	{
		this.isDeepMerge = isDeepMerge;
	}

	public boolean isHandleExistingIdAsNewId()
	{
		return handleExistingIdAsNewId;
	}

	public void setHandleExistingIdAsNewId(boolean handleExistingIdAsNewId)
	{
		this.handleExistingIdAsNewId = handleExistingIdAsNewId;
	}

	public boolean isCacheToDispose()
	{
		return isCacheToDispose;
	}

	public void setCacheToDispose(boolean isCacheToDispose)
	{
		this.isCacheToDispose = isCacheToDispose;
	}

	public boolean isPrivilegedCacheToDispose()
	{
		return isPrivilegedCacheToDispose;
	}

	public void setPrivilegedCacheToDispose(boolean isPrivilegedCacheToDispose)
	{
		this.isPrivilegedCacheToDispose = isPrivilegedCacheToDispose;
	}
}
