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

import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.audit.model.AuditedArg;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IBehaviorTypeExtractor;
import com.koch.ambeth.service.proxy.MethodLevelBehavior;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

public class AuditMethodCallPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanProcessor {
    protected final AnnotationCache<Audited> annotationCache = new AnnotationCache<Audited>(Audited.class) {
        @Override
        protected boolean annotationEquals(Audited left, Audited right) {
            return true;
        }
    };

    protected final IBehaviorTypeExtractor<Audited, AuditInfo> auditMethodExtractor = new IBehaviorTypeExtractor<Audited, AuditInfo>() {
        @Override
        public AuditInfo extractBehaviorType(Audited annotation, AnnotatedElement annotatedElement) {
            if (annotation == null) {
                return null;
            }
            AuditInfo auditInfo = new AuditInfo(annotation);

            if (annotatedElement instanceof Method) {
                Method method = (Method) annotatedElement;
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                AuditedArg[] auditedArgs = new AuditedArg[parameterAnnotations.length];
                for (int i = 0; i < parameterAnnotations.length; i++) {
                    AuditedArg aaa = null;
                    for (Annotation parameterAnnotation : parameterAnnotations[i]) {
                        if (parameterAnnotation instanceof AuditedArg) {
                            aaa = (AuditedArg) parameterAnnotation;
                            break;
                        }
                    }
                    auditedArgs[i] = aaa;
                }
                auditInfo.setAuditedArgs(auditedArgs);
            }
            return auditInfo;
        }
    };

    @Override
    protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext, IBeanConfiguration beanConfiguration, Class<?> type,
            Set<Class<?>> requestedTypes) {
        var behaviour = MethodLevelBehavior.create(type, annotationCache, AuditInfo.class, auditMethodExtractor, beanContextFactory, beanContext);
        if (behaviour == null) {
            return null;
        }
        var interceptor = new AuditMethodCallInterceptor();
        if (beanContext.isRunning()) {
            var interceptorBC = beanContext.registerWithLifecycle(interceptor);
            interceptorBC.propertyValue(AuditMethodCallInterceptor.P_METHOD_LEVEL_BEHAVIOUR, behaviour);
            return interceptorBC.finish();
        }
        var interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
        interceptorBC.propertyValue(AuditMethodCallInterceptor.P_METHOD_LEVEL_BEHAVIOUR, behaviour);
        return interceptor;
    }

    @Override
    public ProcessorOrder getOrder() {
        return ProcessorOrder.LOW;
    }
}
