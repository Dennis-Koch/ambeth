package de.osthus.ambeth.testutil;

import java.util.Collection;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.security.IPermissionGroupUpdater;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.setup.IDataSetup;

public class DataSetupExecutor implements IStartingBean
{
	private static final ThreadLocal<Boolean> autoRebuildDataTL = new ThreadLocal<Boolean>();

	public static Boolean setAutoRebuildData(Boolean autoRebuildData)
	{
		Boolean oldValue = autoRebuildDataTL.get();
		if (autoRebuildData == null)
		{
			autoRebuildDataTL.remove();
		}
		else
		{
			autoRebuildDataTL.set(autoRebuildData);
		}
		return oldValue;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IPermissionGroupUpdater permissionGroupUpdater;

	@Autowired
	protected IDataSetup dataSetup;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Autowired
	protected ITransaction transaction;

	@Override
	public void afterStarted() throws Throwable
	{
		if (Boolean.TRUE.equals(autoRebuildDataTL.get()))
		{
			rebuildData();
		}
	}

	public void rebuildData()
	{
		try
		{
			try
			{
				securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
				{
					@Override
					public Object invoke() throws Throwable
					{
						transaction.processAndCommit(new DatabaseCallback()
						{
							@Override
							public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
							{
								permissionGroupUpdater.executeWithoutPermissionGroupUpdate(new IResultingBackgroundWorkerDelegate<Object>()
								{
									@Override
									public Object invoke() throws Throwable
									{
										final Collection<Object> dataSet = dataSetup.executeDatasetBuilders();
										if (dataSet.size() > 0)
										{
											mergeProcess.process(dataSet, null, null, null, false);
										}
										return null;
									}
								});
								permissionGroupUpdater.fillEmptyPermissionGroups();
							}
						});
						return null;
					}
				});
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				eventDispatcher.dispatchEvent(ClearAllCachesEvent.getInstance());
			}
		}
		finally
		{
			threadLocalCleanupController.cleanupThreadLocal();
		}
	}
}
