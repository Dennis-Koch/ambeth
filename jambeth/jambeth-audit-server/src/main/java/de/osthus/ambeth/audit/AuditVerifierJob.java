package de.osthus.ambeth.audit;

import java.util.Collections;
import java.util.Comparator;

import de.osthus.ambeth.IAuditEntryVerifier;
import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.IPreparedObjRefFactory;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IMultithreadingHelper;

public class AuditVerifierJob implements IJob, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditEntryVerifier auditEntryVerifier;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICache cache;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ITransaction transaction;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Property(defaultValue = "100")
	protected int batchCount;

	private IQuery<IAuditEntry> q_allAuditEntries;

	@Override
	public void afterStarted() throws Throwable
	{
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		q_allAuditEntries = qb.build();
	}

	@Override
	public boolean canBePaused()
	{
		return false;
	}

	@Override
	public boolean canBeStopped()
	{
		return false;
	}

	@Override
	public boolean supportsStatusTracking()
	{
		return false;
	}

	@Override
	public boolean supportsCompletenessTracking()
	{
		return false;
	}

	@Override
	public void execute(final IJobContext context) throws Throwable
	{
		IDisposableCache cache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.TRUE, AuditVerifierJob.class.getName());
		try
		{
			cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					securityActivation.executeWithoutSecurity(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							transaction.processAndCommit(new DatabaseCallback()
							{
								@Override
								public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
								{
									verifyAllAuditEntries(context);
								}
							}, false, true);
						}
					});
					return null;
				}
			});
		}
		finally
		{
			cache.dispose();
		}
	}

	protected void verifyAllAuditEntries(IJobContext context) throws Throwable
	{
		ArrayList<IObjRef> batchEntries = new ArrayList<IObjRef>(batchCount);

		IPreparedObjRefFactory preparedObjRefFactory = objRefFactory.prepareObjRefFactory(q_allAuditEntries.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
		IVersionCursor cursor = q_allAuditEntries.retrieveAsVersions(false);
		try
		{
			int count = 0;
			boolean verificationSuccess = true;
			while (cursor.moveNext())
			{
				IVersionItem versionItem = cursor.getCurrent();

				count++;

				// objRef WITHOUT version intentional: We do not want to get cache hits in the committed RootCache but load all data
				// directly to the transactional RootCache instead
				IObjRef objRef = preparedObjRefFactory.createObjRef(versionItem.getId(), null);
				batchEntries.add(objRef);
				if (batchEntries.size() < batchCount)
				{
					continue;
				}
				verificationSuccess &= verify(batchEntries);
				batchEntries.clear();
			}
			verificationSuccess &= verify(batchEntries);
			if (verificationSuccess && log.isInfoEnabled())
			{
				log.info("Verification of " + count + " audit entries finished. AUDIT TRAIL IS VALID");
			}
		}
		finally
		{
			cursor.dispose();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean verify(IList<IObjRef> objRefs)
	{
		if (objRefs.size() == 0)
		{
			return true;
		}
		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
		IList<IAuditEntry> auditEntries = (IList) cache.getObjects(objRefs, CacheDirective.none());
		boolean[] verificationResult = auditEntryVerifier.verifyAuditEntries(auditEntries);
		ArrayList<IAuditEntry> invalidAuditEntries = new ArrayList<IAuditEntry>();
		for (int a = 0, size = verificationResult.length; a < size; a++)
		{
			if (!verificationResult[a])
			{
				invalidAuditEntries.add(auditEntries.get(a));
			}
		}
		if (invalidAuditEntries.size() > 0)
		{
			Collections.sort(auditEntries, new Comparator<IAuditEntry>()
			{
				@Override
				public int compare(IAuditEntry o1, IAuditEntry o2)
				{
					if (o1.getTimestamp() > o2.getTimestamp())
					{
						return 1;
					}
					if (o1.getTimestamp() < o2.getTimestamp())
					{
						return -1;
					}
					return 0;
				}
			});

			StringBuilder sb = new StringBuilder();
			sb.append(invalidAuditEntries.size()).append(" audit entries invalid:\n");
			for (int a = 0, size = invalidAuditEntries.size(); a < size; a++)
			{
				IAuditEntry auditEntry = invalidAuditEntries.get(a);
				sb.append("\t").append(auditEntry);
			}
			log.error(sb);
		}
		return invalidAuditEntries.size() == 0;
	}
}
