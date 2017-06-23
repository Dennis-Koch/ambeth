package com.koch.ambeth.audit.server;

/*-
 * #%L
 * jambeth-audit-server
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

import java.util.Collections;
import java.util.Comparator;

import com.koch.ambeth.audit.IAuditEntryVerifier;
import com.koch.ambeth.audit.model.IAuditEntry;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.job.IJob;
import com.koch.ambeth.job.IJobContext;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.DatabaseCallback;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class AuditVerifierJob implements IJob, IStartingBean {
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
	public void afterStarted() throws Throwable {
		IQueryBuilder<IAuditEntry> qb = queryBuilderFactory.create(IAuditEntry.class);
		q_allAuditEntries = qb.build();
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
	public void execute(final IJobContext context) throws Throwable {
		IDisposableCache cache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false,
				Boolean.TRUE, AuditVerifierJob.class.getName());
		try {
			cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerDelegate<Object>() {
				@Override
				public Object invoke() throws Throwable {
					securityActivation.executeWithoutSecurity(new IBackgroundWorkerDelegate() {
						@Override
						public void invoke() throws Throwable {
							transaction.processAndCommit(new DatabaseCallback() {
								@Override
								public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
										throws Throwable {
									verifyAllAuditEntries(context);
								}
							}, false, true);
						}
					});
					return null;
				}
			});
		}
		finally {
			cache.dispose();
		}
	}

	protected void verifyAllAuditEntries(IJobContext context) throws Throwable {
		ArrayList<IObjRef> batchEntries = new ArrayList<>(batchCount);

		IPreparedObjRefFactory preparedObjRefFactory = objRefFactory
				.prepareObjRefFactory(q_allAuditEntries.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
		IVersionCursor cursor = q_allAuditEntries.retrieveAsVersions(false);
		try {
			int count = 0;
			boolean verificationSuccess = true;
			while (cursor.moveNext()) {
				IVersionItem versionItem = cursor.getCurrent();

				count++;

				// objRef WITHOUT version intentional: We do not want to get cache hits in the committed
				// RootCache but load all data
				// directly to the transactional RootCache instead
				IObjRef objRef = preparedObjRefFactory.createObjRef(versionItem.getId(), null);
				batchEntries.add(objRef);
				if (batchEntries.size() < batchCount) {
					continue;
				}
				verificationSuccess &= verify(batchEntries);
				batchEntries.clear();
			}
			verificationSuccess &= verify(batchEntries);
			if (verificationSuccess && log.isInfoEnabled()) {
				log.info("Verification of " + count + " audit entries finished. AUDIT TRAIL IS VALID");
			}
		}
		finally {
			cursor.dispose();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean verify(IList<IObjRef> objRefs) {
		if (objRefs.isEmpty()) {
			return true;
		}
		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
		IList<IAuditEntry> auditEntries = (IList) cache.getObjects(objRefs, CacheDirective.none());
		boolean[] verificationResult = auditEntryVerifier.verifyAuditEntries(auditEntries);
		ArrayList<IAuditEntry> invalidAuditEntries = new ArrayList<>();
		for (int a = 0, size = verificationResult.length; a < size; a++) {
			if (!verificationResult[a]) {
				invalidAuditEntries.add(auditEntries.get(a));
			}
		}
		if (!invalidAuditEntries.isEmpty()) {
			Collections.sort(auditEntries, new Comparator<IAuditEntry>() {
				@Override
				public int compare(IAuditEntry o1, IAuditEntry o2) {
					if (o1.getTimestamp() > o2.getTimestamp()) {
						return 1;
					}
					if (o1.getTimestamp() < o2.getTimestamp()) {
						return -1;
					}
					return 0;
				}
			});

			StringBuilder sb = new StringBuilder();
			sb.append(invalidAuditEntries.size()).append(" audit entries invalid:\n");
			for (int a = 0, size = invalidAuditEntries.size(); a < size; a++) {
				IAuditEntry auditEntry = invalidAuditEntries.get(a);
				sb.append("\t").append(auditEntry);
			}
			log.error(sb);
		}
		return invalidAuditEntries.isEmpty();
	}
}
