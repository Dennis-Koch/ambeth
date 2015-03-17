package de.osthus.ambeth.testutil;

import java.util.Collection;

import de.osthus.ambeth.audit.IAuditInfoController;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.security.IPermissionGroupUpdater;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
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

	@Autowired(optional = true)
	protected IAuditInfoController auditInfoController;

	@Autowired
	protected IPermissionGroupUpdater permissionGroupUpdater;

	@Autowired
	protected IDataSetup dataSetup;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ILightweightTransaction transaction;

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
		if (auditInfoController != null)
		{
			auditInfoController.pushAuditReason("Data Rebuild!");
		}
		try
		{
			securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
			{
				@Override
				public Object invoke() throws Throwable
				{
					transaction.runInTransaction(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							permissionGroupUpdater.executeWithoutPermissionGroupUpdate(new IResultingBackgroundWorkerDelegate<Object>()
							{
								@Override
								public Object invoke() throws Throwable
								{
									final Collection<Object> dataSet = dataSetup.executeDatasetBuilders();
									if (dataSet.size() > 0)
									{
										mergeProcess.process(dataSet, null, null, null);
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
			if (auditInfoController != null)
			{
				auditInfoController.popAuditReason();
			}
		}
	}
}
