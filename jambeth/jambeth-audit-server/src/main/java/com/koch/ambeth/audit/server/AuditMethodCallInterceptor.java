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

import com.koch.ambeth.audit.server.config.AuditConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.proxy.CascadedInterceptor;
import com.koch.ambeth.util.proxy.MethodProxy;
import com.koch.ambeth.util.transaction.ILightweightTransaction;

import java.lang.reflect.Method;

public class AuditMethodCallInterceptor extends CascadedInterceptor implements IInitializingBean {
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

    protected boolean initialized;

    @Override
    public void afterPropertiesSet() throws Throwable {
        initialized = true;
    }

    @Override
    protected Object interceptIntern(final Object obj, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
        if (!initialized) {
            return invokeTarget(obj, method, args, proxy);
        }
        final AuditInfo auditInfo = methodLevelBehaviour.getBehaviourOfMethod(method);
        if ((auditInfo == null && !auditedServiceDefaultModeActive) || (auditInfo != null && !auditInfo.getAudited().value())) {
            return invokeTarget(obj, method, args, proxy);
        }

        // filter the args by audit configuration
        var auditedArgs = auditInfo.getAuditedArgs();
        var filteredArgs = new Object[args.length];
        for (int i = 0; i < filteredArgs.length; i++) {
            var auditedArg = auditedArgs != null ? auditedArgs[i] : null;
            if ((auditedArg == null && auditedServiceArgDefaultModeActive) || (auditedArg != null && auditedArg.value())) {
                filteredArgs[i] = args[i];
            } else {
                filteredArgs[i] = "n/a";
            }
        }
        if (transactionState.isTransactionActive()) {
            var methodCallHandle = methodCallLogger.logMethodCallStart(method, filteredArgs);
            try {
                return invokeTarget(obj, method, args, proxy);
            } finally {
                methodCallLogger.logMethodCallFinish(methodCallHandle);
            }
        }
        return transaction.runInTransaction(() -> {
            var methodCallHandle = methodCallLogger.logMethodCallStart(method, filteredArgs);
            try {
                return invokeTarget(obj, method, args, proxy);
            } finally {
                methodCallLogger.logMethodCallFinish(methodCallHandle);
            }
        });
    }
}
