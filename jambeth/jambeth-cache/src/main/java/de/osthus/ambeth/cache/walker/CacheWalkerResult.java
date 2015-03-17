package de.osthus.ambeth.cache.walker;

import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.util.AbstractPrintable;
import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class CacheWalkerResult implements IPrintable, ICacheWalkerResult
{
	protected static final char pipe = "\u2514".toCharArray()[0];

	protected final ICache cache;

	protected final boolean privileged;

	protected final boolean transactional;

	protected final boolean threadLocal;

	public IObjRef[] objRefs;

	public Object[] cacheValues;

	public boolean[] pendingChanges;

	protected ICacheWalkerResult parentEntry;

	public final Object childEntries;

	public CacheWalkerResult(ICache cache, boolean transactional, boolean threadLocal, IObjRef[] objRefs, Object[] cacheValues, Object childEntries)
	{
		this.cache = cache;
		this.transactional = transactional;
		this.threadLocal = threadLocal;
		this.objRefs = objRefs;
		this.cacheValues = cacheValues;
		this.childEntries = childEntries;
		privileged = cache.isPrivileged();
	}

	public void updatePendingChanges()
	{
		boolean[] pendingChanges = null;
		for (int a = cacheValues.length; a-- > 0;)
		{
			Object cacheValue = cacheValues[a];
			if (cacheValue == null || cacheValue instanceof AbstractCacheValue)
			{
				continue;
			}
			if (pendingChanges == null)
			{
				pendingChanges = new boolean[cacheValues.length];
			}
			pendingChanges[a] = ((IDataObject) cacheValue).hasPendingChanges();
		}
		this.pendingChanges = pendingChanges;
	}

	public ICacheWalkerResult getParentEntry()
	{
		return parentEntry;
	}

	public void setParentEntry(ICacheWalkerResult parentEntry)
	{
		this.parentEntry = parentEntry;
	}

	public ICache getCache()
	{
		return cache;
	}

	public boolean isPrivileged()
	{
		return privileged;
	}

	public boolean isTransactional()
	{
		return transactional;
	}

	public boolean isThreadLocal()
	{
		return threadLocal;
	}

	public Object[] getCacheValues()
	{
		return cacheValues;
	}

	public Object getChildEntries()
	{
		return childEntries;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		toString(sb, 0);
	}

	@Override
	public void toString(StringBuilder sb, final int tabCount)
	{
		toStringIntern(sb, new IPrintable()
		{

			@Override
			public void toString(StringBuilder sb)
			{
				sb.append(System.lineSeparator());
				StringBuilderUtil.appendTabs(sb, tabCount);
			}
		}, null);
	}

	protected void toStringIntern(StringBuilder sb, IPrintable preSpace, Boolean entityDescriptionAtRoot)
	{
		Object[] cacheValues = this.cacheValues;

		if (entityDescriptionAtRoot == null)
		{
			IObjRef[] objRefs = this.objRefs;
			for (int a = 0, size = objRefs.length; a < size; a++)
			{
				IObjRef objRef = objRefs[a];
				sb.append(pipe).append(' ').append(a + 1);
				sb.append(". Type=").append(objRef.getRealType().getSimpleName());
				sb.append(" Id(");
				if (objRef.getIdNameIndex() == ObjRef.PRIMARY_KEY_INDEX)
				{
					sb.append("PK");
				}
				else
				{
					sb.append("AK-").append(objRef.getIdNameIndex());
				}
				sb.append(")=").append(objRef.getId());
				entityDescriptionAtRoot = Boolean.TRUE;
				preSpace.toString(sb);
			}
		}
		sb.append(pipe).append(" Cache");
		boolean firstSuffix = true;
		if (!privileged)
		{
			firstSuffix = appendSuffix("SEC", firstSuffix, sb);
		}
		if (transactional)
		{
			firstSuffix = appendSuffix("TX", firstSuffix, sb);
		}
		if (threadLocal)
		{
			firstSuffix = appendSuffix("L", firstSuffix, sb);
		}
		if (parentEntry == null)
		{
			firstSuffix = appendSuffix("G", firstSuffix, sb);
		}
		sb.append("--#0x").append(toHexString(cache));

		preSpace = new AbstractPrintable(preSpace)
		{
			@Override
			public void toString(StringBuilder sb)
			{
				p.toString(sb);
				sb.append('\t');
			}
		};
		IPrintable preSpaceForCacheValue;
		if (childEntries == null)
		{
			preSpaceForCacheValue = new AbstractPrintable(preSpace)
			{
				@Override
				public void toString(StringBuilder sb)
				{
					p.toString(sb);
					sb.append("   ");
				}
			};
		}
		else
		{
			preSpaceForCacheValue = new AbstractPrintable(preSpace)
			{
				@Override
				public void toString(StringBuilder sb)
				{
					p.toString(sb);
					sb.append("|  ");
				}
			};
		}
		for (int a = 0, size = cacheValues.length; a < size; a++)
		{
			preSpaceForCacheValue.toString(sb);
			sb.append(a + 1).append('.');
			Object cacheValue = cacheValues[a];
			if (cacheValue == null)
			{
				sb.append(" n/a");
				continue;
			}
			IEntityMetaData metaData = ((IEntityMetaDataHolder) cacheValue).get__EntityMetaData();
			Object id, version = null;
			boolean hasVersion = metaData.getVersionMember() != null;
			boolean hasPendingChanges = false;
			if (cacheValue instanceof AbstractCacheValue)
			{
				AbstractCacheValue cacheValueCasted = (AbstractCacheValue) cacheValue;
				id = cacheValueCasted.getId();
				if (hasVersion)
				{
					version = cacheValueCasted.getVersion();
				}
			}
			else
			{
				id = metaData.getIdMember().getValue(cacheValue);
				if (hasVersion)
				{
					version = metaData.getVersionMember().getValue(cacheValue);
				}
				hasPendingChanges = pendingChanges != null && pendingChanges[a];
			}
			if (!Boolean.TRUE.equals(entityDescriptionAtRoot))
			{
				sb.append(" Type=").append(metaData.getEntityType().getSimpleName());
				sb.append(" Id=").append(id);
			}
			if (hasVersion)
			{
				sb.append(" Version=").append(version);
			}
			if (hasPendingChanges)
			{
				sb.append(" (m)");
			}
		}
		if (childEntries instanceof ICacheWalkerResult[])
		{
			CacheWalkerResult[] childEntries = (CacheWalkerResult[]) this.childEntries;
			for (int a = 0, size = childEntries.length; a < size; a++)
			{
				final boolean hasSuccessor = a < size - 1;
				CacheWalkerResult entry = childEntries[a];

				IPrintable preSpaceForChildEntry = new AbstractPrintable(preSpace)
				{

					@Override
					public void toString(StringBuilder sb)
					{
						p.toString(sb);
						if (hasSuccessor)
						{
							sb.append("|");
						}
					}
				};
				preSpace.toString(sb);
				entry.toStringIntern(sb, preSpaceForChildEntry, entityDescriptionAtRoot);
			}
		}
		else if (childEntries != null)
		{
			CacheWalkerResult entry = (CacheWalkerResult) childEntries;
			preSpace.toString(sb);
			entry.toStringIntern(sb, preSpace, entityDescriptionAtRoot);
		}
	}

	protected boolean appendSuffix(String suffix, boolean isFirstSuffix, StringBuilder sb)
	{
		sb.append('-');
		sb.append(suffix);
		return false;
	}

	protected String toHexString(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		return Integer.toHexString(System.identityHashCode(obj));
	}
}
