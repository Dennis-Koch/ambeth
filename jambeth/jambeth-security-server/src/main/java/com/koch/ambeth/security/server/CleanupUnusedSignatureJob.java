package com.koch.ambeth.security.server;

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.job.IJob;
import com.koch.ambeth.job.IJobContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.util.collections.IList;

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
