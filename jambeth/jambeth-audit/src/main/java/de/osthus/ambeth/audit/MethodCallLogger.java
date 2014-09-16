package de.osthus.ambeth.audit;

import java.lang.reflect.Method;
import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.event.DatabasePreCommitEvent;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.security.IAuthorization;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.IUserResolver;
import de.osthus.ambeth.security.model.IUser;

public class MethodCallLogger implements IThreadLocalCleanupBean, IMethodCallLogger
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditEntryFactory auditEntryFactory;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired(optional = true)
	protected IUserResolver userResolver;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired
	protected ITransactionState transactionState;

	protected final ThreadLocal<List<IAuditEntry>> queuedMethodCallsTL = new ThreadLocal<List<IAuditEntry>>();

	@Override
	public void cleanupThreadLocal()
	{
		queuedMethodCallsTL.remove();
	}

	@Override
	public IMethodCallHandle logMethodCallStart(Method method)
	{
		if (!transactionState.isTransactionActive())
		{
			return null;
		}
		IAuditEntry auditEntry = auditEntryFactory.createAuditEntry();
		auditEntry.setServiceType(method.getDeclaringClass().getName());
		auditEntry.setMethodName(method.getName());

		if (userResolver != null)
		{
			IAuthorization authorization = securityContextHolder.getCreateContext().getAuthorization();
			if (authorization != null)
			{
				String currentSID = authorization.getSID();
				IUser currentUser = userResolver.resolveUserBySID(currentSID);
				auditEntry.setUser(currentUser);
			}
		}

		List<IAuditEntry> queuedMethodCalls = queuedMethodCallsTL.get();
		if (queuedMethodCalls == null)
		{
			queuedMethodCalls = new ArrayList<IAuditEntry>();
			queuedMethodCallsTL.set(queuedMethodCalls);
		}
		queuedMethodCalls.add(auditEntry);
		return new MethodCallHandle(auditEntry, System.currentTimeMillis());
	}

	@Override
	public void logMethodCallFinish(IMethodCallHandle methodCallHandle)
	{
		if (methodCallHandle == null)
		{
			return;
		}
		MethodCallHandle handle = (MethodCallHandle) methodCallHandle;
		handle.auditEntry.setSpentTime(System.currentTimeMillis() - handle.start);
	}

	public void handlePreCommit(DatabasePreCommitEvent evnt)
	{
		List<IAuditEntry> queuedMethodCalls = queuedMethodCallsTL.get();
		if (queuedMethodCalls == null)
		{
			return;
		}
		queuedMethodCallsTL.remove();
		mergeProcess.process(queuedMethodCalls, null, null, null);
	}
}
