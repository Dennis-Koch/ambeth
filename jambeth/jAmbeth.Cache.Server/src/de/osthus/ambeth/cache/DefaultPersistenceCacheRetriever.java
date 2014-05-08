package de.osthus.ambeth.cache;

import java.util.List;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.ILoadContainerProvider;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

@PersistenceContext
public class DefaultPersistenceCacheRetriever implements ICacheRetriever, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected ILoadContainerProvider loadContainerProvider;

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(loadContainerProvider, "LoadContainerProvider");
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
	}

	public void setLoadContainerProvider(ILoadContainerProvider loadContainerProvider)
	{
		this.loadContainerProvider = loadContainerProvider;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		if (log.isDebugEnabled())
		{
			debugToLoad(orisToLoad);
		}
		ArrayList<ILoadContainer> targetEntities = new ArrayList<ILoadContainer>();
		loadContainerProvider.assignInstances(orisToLoad, targetEntities);
		return targetEntities;
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

			int printBorder = 3, skipped = count >= 50 ? Math.max(0, count - printBorder * 2) : 0;
			for (int a = count; a-- > 0;)
			{
				if (skipped > 1)
				{
					if (count - a > printBorder && a >= printBorder)
					{
						continue;
					}
					if (a == printBorder - 1)
					{
						sb.append("\r\n\t...skipped ").append(skipped).append(" items...");
					}
				}
				IObjRef oriToLoad = orisToLoad.get(a);
				if (count > 1)
				{
					sb.append("\r\n\t");
				}
				StringBuilderUtil.appendPrintable(sb, oriToLoad);
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

			int printBorder = 3, skipped = count >= 50 ? Math.max(0, count - printBorder * 2) : 0;
			for (int a = count; a-- > 0;)
			{
				if (skipped > 1)
				{
					if (count - a > printBorder && a >= printBorder)
					{
						continue;
					}
					if (a == printBorder - 1)
					{
						sb.append("\r\n\t...skipped ").append(skipped).append(" items...");
					}
				}
				IObjRelation orelToLoad = orelsToLoad.get(a);
				if (count > 1)
				{
					sb.append("\r\n\t");
				}
				StringBuilderUtil.appendPrintable(sb, orelToLoad);
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
