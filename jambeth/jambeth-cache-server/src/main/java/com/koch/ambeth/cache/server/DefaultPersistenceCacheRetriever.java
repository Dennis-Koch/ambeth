package com.koch.ambeth.cache.server;

import java.util.List;

import com.koch.ambeth.cache.audit.IVerifyOnLoad;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.ILoadContainerProvider;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

@PersistenceContext
public class DefaultPersistenceCacheRetriever implements ICacheRetriever
{
	@LogInstance
	private ILogger log;

	protected int maxDebugItems = 50;

	@Autowired(optional = true)
	protected IVerifyOnLoad verifyOnLoad;

	@Autowired
	protected ILoadContainerProvider loadContainerProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		if (log.isDebugEnabled())
		{
			debugToLoad(orisToLoad);
		}
		ArrayList<ILoadContainer> loadedEntities = new ArrayList<ILoadContainer>(orisToLoad.size());
		loadContainerProvider.assignInstances(orisToLoad, loadedEntities);

		if (verifyOnLoad != null)
		{
			verifyOnLoad.queueVerifyEntitiesOnLoad(loadedEntities);
		}
		return loadedEntities;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> orelsToLoad)
	{
		if (log.isDebugEnabled())
		{
			debugOrelsToLoad(orelsToLoad);
		}
		ArrayList<IObjRelationResult> targetRelations = new ArrayList<IObjRelationResult>();
		loadContainerProvider.assignRelations(orelsToLoad, targetRelations);
		return targetRelations;
	}

	protected void debugToLoad(List<IObjRef> orisToLoad)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			int count = orisToLoad.size();
			sb.append("List<IObjRef> : ").append(count).append(" item");
			if (count != 1)
			{
				sb.append('s');
			}
			sb.append(" [");

			int printBorder = 3;
			if (count <= maxDebugItems || count <= printBorder * 2)
			{
				for (int a = count; a-- > 0;)
				{
					sb.append("\r\n\t");
					IObjRef oriToLoad = orisToLoad.get(a);
					StringBuilderUtil.appendPrintable(sb, oriToLoad);
				}
			}
			else
			{
				for (int a = count, pos = count - printBorder; a-- > pos;)
				{
					sb.append("\r\n\t");
					IObjRef oriToLoad = orisToLoad.get(a);
					StringBuilderUtil.appendPrintable(sb, oriToLoad);
				}
				sb.append("\r\n\t...skipped ").append(count - printBorder * 2).append(" items...");
				for (int a = printBorder; a-- > 0;)
				{
					sb.append("\r\n\t");
					IObjRef oriToLoad = orisToLoad.get(a);
					StringBuilderUtil.appendPrintable(sb, oriToLoad);
				}
			}
			sb.append("]");

			log.debug(sb.toString());
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	protected void debugOrelsToLoad(List<IObjRelation> orelsToLoad)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			int count = orelsToLoad.size();
			sb.append("List<IObjRelation> : ").append(count).append(" item");
			if (count != 1)
			{
				sb.append('s');
			}
			sb.append(" [");

			int printBorder = 3;
			if (count <= maxDebugItems || count <= printBorder * 2)
			{
				for (int a = count; a-- > 0;)
				{
					sb.append("\r\n\t");
					IObjRelation orelToLoad = orelsToLoad.get(a);
					StringBuilderUtil.appendPrintable(sb, orelToLoad);
				}
			}
			else
			{
				for (int a = count, pos = count - printBorder; a-- > pos;)
				{
					sb.append("\r\n\t");
					IObjRelation orelToLoad = orelsToLoad.get(a);
					StringBuilderUtil.appendPrintable(sb, orelToLoad);
				}
				sb.append("\r\n\t...skipped ").append(count - printBorder * 2).append(" items...");
				for (int a = printBorder; a-- > 0;)
				{
					sb.append("\r\n\t");
					IObjRelation orelToLoad = orelsToLoad.get(a);
					StringBuilderUtil.appendPrintable(sb, orelToLoad);
				}
			}
			sb.append("]");

			log.debug(sb.toString());
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}
}
