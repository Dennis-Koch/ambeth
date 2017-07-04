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

import java.lang.reflect.Method;

import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.security.audit.model.AuditedArg;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

import net.sf.cglib.proxy.MethodProxy;

public class AuditMethodCallInterceptor extends CascadedInterceptor {
	public static final String P_METHOD_LEVEL_BEHAVIOUR = "MethodLevelBehaviour";

	@Autowired
	protected IMethodCallLogger methodCallLogger;

	@Autowired
	protected IMethodLevelBehavior<AuditInfo> methodLevelBehaviour;

	@Autowired
	protected ILightweightTransaction transaction;

	@Autowired
	protected ITransactionState transactionState;

	@Property(name = AuditConfigurationConstants.AuditedServiceDefaultModeActive, defaultValue = "true")
	protected boolean auditedServiceDefaultModeActive;

	@Property(name = AuditConfigurationConstants.AuditedServiceArgDefaultModeActive, defaultValue = "false")
	protected boolean auditedServiceArgDefaultModeActive;

	@Override
	protected Object interceptIntern(final Object obj, final Method method, final Object[] args,
			final MethodProxy proxy) throws Throwable {
		final AuditInfo auditInfo = methodLevelBehaviour.getBehaviourOfMethod(method);
		if ((auditInfo == null && !auditedServiceDefaultModeActive)
				|| (auditInfo != null && !auditInfo.getAudited().value())) {
			return invokeTarget(obj, method, args, proxy);
		}

		// filter the args by audit configuration
		AuditedArg[] auditedArgs = auditInfo.getAuditedArgs();
		final Object[] filteredArgs = new Object[args.length];
		for (int i = 0; i < filteredArgs.length; i++) {
			AuditedArg auditedArg = auditedArgs != null ? auditedArgs[i] : null;
			if ((auditedArg == null && auditedServiceArgDefaultModeActive)
					|| (auditedArg != null && auditedArg.value())) {
				filteredArgs[i] = args[i];
			}
			else {
				filteredArgs[i] = "n/a";
			}
		}
		if (transactionState.isTransactionActive()) {
			IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method,
					filteredArgs);
			try {
				return invokeTarget(obj, method, args, proxy);
			}
			finally {
				methodCallLogger.logMethodCallFinish(methodCallHandle);
			}
		}
		return transaction.runInTransaction(new IResultingBackgroundWorkerDelegate<Object>() {
			@Override
			public Object invoke() throws Exception {
				IMethodCallHandle methodCallHandle = methodCallLogger.logMethodCallStart(method,
						filteredArgs);
				try {
					return invokeTarget(obj, method, args, proxy);
				}
				catch (Error e) {
					throw e;
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
				finally {
					methodCallLogger.logMethodCallFinish(methodCallHandle);
				}
			}
		});
	}
}
