package de.osthus.ambeth.cache;

import java.util.List;

import de.osthus.ambeth.audit.IAuditEntryVerifier;
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
	protected IAuditEntryVerifier auditEntryVerifier;

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
		ArrayList<ILoadContainer> targetEntities = new ArrayList<ILoadContainer>(orisToLoad.size());
		loadContainerProvider.assignInstances(orisToLoad, targetEntities);

		if (auditEntryVerifier != null)
		{
			ArrayList<IObjRef> objRefsToReturn = new ArrayList<IObjRef>(targetEntities.size());
			for (int a = targetEntities.size(); a-- > 0;)
			{
				objRefsToReturn.add(targetEntities.get(a).getReference());
			}
			auditEntryVerifier.verifyEntitiesOnLoad(objRefsToReturn);
		}
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

			int printBorder = 3, skipped = count >= maxDebugItems ? Math.max(0, count - printBorder * 2) : 0;
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

			int printBorder = 3, skipped = count >= maxDebugItems ? Math.max(0, count - printBorder * 2) : 0;
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
