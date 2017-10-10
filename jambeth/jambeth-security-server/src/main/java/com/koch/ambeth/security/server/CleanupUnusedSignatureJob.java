package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.job.IJob;
import com.koch.ambeth.job.IJobContext;
import com.koch.ambeth.job.JobScheduleConfiguration;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.security.SecurityContext;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.util.collections.IList;

@SecurityContext(SecurityContextType.AUTHENTICATED)
public class CleanupUnusedSignatureJob implements IJob, IStartingBean {
	public static void registerCleanupSignatureJob(IBeanContextFactory beanContextFactory,
			String userName, char[] userPass, String cronPattern) {
		IBeanConfiguration cleanupUnusedSignatureJob = beanContextFactory
				.registerBean(CleanupUnusedSignatureJob.class);
		beanContextFactory.registerBean(JobScheduleConfiguration.class) //
				.propertyRef(JobScheduleConfiguration.JOB, cleanupUnusedSignatureJob) //
				.propertyValue(JobScheduleConfiguration.JOB_NAME,
						CleanupUnusedSignatureJob.class.getSimpleName()) //
				.propertyValue(JobScheduleConfiguration.CRON_PATTERN, cronPattern) //
				.propertyValue(JobScheduleConfiguration.USER_NAME, userName) //
				.propertyValue(JobScheduleConfiguration.USER_PASS, userPass);
	}

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	private IQuery<ISignature> q_signaturesWithoutUser;

	@Override
	public void afterStarted() throws Throwable {
		IQueryBuilder<ISignature> qb = queryBuilderFactory.create(ISignature.class);
		q_signaturesWithoutUser = qb
				.build(qb.and(qb.let(qb.property(ISignature.User)).isNull(),
						qb.let(qb.property("Id")).isNotIn(
								qb.property(
										"<" + IAuditEntry.class.getName() + "#" + IAuditEntry.SignatureOfUser))));
	}

	@Override
	public boolean canBePaused() {
		return false;
	}

	@Override
	public boolean canBeStopped() {
		return false;
	}

	@Override
	public boolean supportsStatusTracking() {
		return false;
	}

	@Override
	public boolean supportsCompletenessTracking() {
		return false;
	}

	@Override
	public void execute(IJobContext context) throws Exception {
		IList<ISignature> retrieve = q_signaturesWithoutUser.retrieve();
		mergeProcess.process(retrieve);
	}
}
