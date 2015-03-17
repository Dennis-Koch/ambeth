package de.osthus.ambeth.merge;

import java.util.Map;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IUpdateItem;

public class MergeHandle
{
	protected final IdentityHashSet<Object> alreadyProcessedSet = new IdentityHashSet<Object>();
	protected final IdentityHashMap<Object, IObjRef> objToOriDict = new IdentityHashMap<Object, IObjRef>();
	protected final HashMap<IObjRef, Object> oriToObjDict = new HashMap<IObjRef, Object>();
	protected final IdentityLinkedMap<Object, IList<IUpdateItem>> objToModDict = new IdentityLinkedMap<Object, IList<IUpdateItem>>();

	protected final ArrayList<IObjRef> oldOrList = new ArrayList<IObjRef>();
	protected final ArrayList<IObjRef> newOrList = new ArrayList<IObjRef>();
	protected ICache cache;

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

}
