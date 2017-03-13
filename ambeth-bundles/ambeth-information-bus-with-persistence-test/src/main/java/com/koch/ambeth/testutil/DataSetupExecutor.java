package com.koch.ambeth.testutil;

import java.util.Collection;

import com.koch.ambeth.audit.server.IAuditInfoController;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.IRevertDelegate;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.util.setup.IDataSetup;
import com.koch.ambeth.merge.util.setup.IDataSetupWithAuthorization;
import com.koch.ambeth.security.persistence.IPermissionGroupUpdater;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

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
	protected IPasswordUtil passwordUtil;

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
					IRevertDelegate suppressPasswordValidationRevert = passwordUtil.suppressPasswordValidation();
					try
					{
						final Collection<Object> dataSet = dataSetup.executeDatasetBuilders();

						final IBackgroundWorkerDelegate transactionDelegate = new IBackgroundWorkerDelegate()
						{
							@Override
							public void invoke() throws Throwable
							{
								permissionGroupUpdater.executeWithoutPermissionGroupUpdate(new IResultingBackgroundWorkerDelegate<Object>()
								{
									@Override
									public Object invoke() throws Throwable
									{
										if (dataSet.size() > 0)
										{
											mergeProcess.process(dataSet, null, null, null);
										}
										return null;
									}
								});
								permissionGroupUpdater.fillEmptyPermissionGroups();
							}
						};
						IDataSetupWithAuthorization dataSetupWithAuthorization = dataSetup.resolveDataSetupWithAuthorization();
						if (dataSetupWithAuthorization != null)
						{
							dataSetupWithAuthorization.executeWithAuthorization(new IResultingBackgroundWorkerDelegate<Object>()
							{
								@Override
								public Object invoke() throws Throwable
								{
									transaction.runInTransaction(transactionDelegate);
									return null;
								}
							});
						}
						else
						{
							transaction.runInTransaction(transactionDelegate);
						}
						return null;
					}
					finally
					{
						suppressPasswordValidationRevert.revert();
					}
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
