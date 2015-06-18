package de.osthus.ambeth.cache;

import java.util.List;

import de.osthus.ambeth.audit.IVerifyOnLoad;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.ILoadContainerProvider;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.util.StringBuilderUtil;

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
