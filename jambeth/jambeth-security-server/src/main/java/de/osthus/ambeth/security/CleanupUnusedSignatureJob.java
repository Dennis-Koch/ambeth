package de.osthus.ambeth.security;

import de.osthus.ambeth.audit.model.IAuditEntry;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IMergeProcess;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.security.model.ISignature;

@SecurityContext(SecurityContextType.AUTHENTICATED)
public class CleanupUnusedSignatureJob implements IJob, IStartingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	private IQuery<ISignature> q_signaturesWithoutUser;

	@Override
	public void afterStarted() throws Throwable
	{
		IQueryBuilder<ISignature> qb = queryBuilderFactory.create(ISignature.class);
		q_signaturesWithoutUser = qb.build(qb.and(qb.isNull(qb.property(ISignature.User)),
				qb.isNotIn(qb.property("Id"), qb.property("<" + IAuditEntry.class.getName() + "#" + IAuditEntry.SignatureOfUser))));
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
	public void execute(IJobContext context) throws Throwable
	{
		IList<ISignature> retrieve = q_signaturesWithoutUser.retrieve();
		mergeProcess.process(retrieve, null, null, null);
	}
}
