package de.osthus.ambeth.audit;

import java.lang.reflect.Method;
import java.util.List;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.database.ITransactionListener;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;

public class MethodCallLogger implements IThreadLocalCleanupBean, IMethodCallLogger, ITransactionListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAuditEntryFactory auditEntryFactory;

	@Autowired
	protected IMergeProcess mergeProcess;

	protected final ThreadLocal<List<IAuditEntry>> queuedMethodCallsTL = new ThreadLocal<List<IAuditEntry>>();

	@Override
	public void cleanupThreadLocal()
	{
		queuedMethodCallsTL.remove();
	}

	@Override
	public void logMethodCall(Method method)
	{
		IAuditEntry auditEntry = auditEntryFactory.createAuditEntry();
		auditEntry.setMethodName(method.getName());
		List<IAuditEntry> queuedMethodCalls = queuedMethodCallsTL.get();
		if (queuedMethodCalls == null)
		{
			queuedMethodCalls = new ArrayList<IAuditEntry>();
			queuedMethodCallsTL.set(queuedMethodCalls);
		}
		queuedMethodCalls.add(auditEntry);
	}

	@Override
	public void handlePreCommit()
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
