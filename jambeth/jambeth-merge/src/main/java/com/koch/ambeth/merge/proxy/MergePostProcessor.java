package com.koch.ambeth.merge.proxy;

/*-
 * #%L
 * jambeth-merge
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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Set;

import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.interceptor.MergeInterceptor;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.Merge;
import com.koch.ambeth.util.annotation.NoProxy;
import com.koch.ambeth.util.annotation.Remove;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class MergePostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<MergeContext> mergeContextCache = new AnnotationCache<MergeContext>(MergeContext.class)
	{
		@Override
		protected boolean annotationEquals(MergeContext left, MergeContext right)
		{
			return true;
		}
	};

	@Override
	public ProcessorOrder getOrder()
	{
		return ProcessorOrder.HIGH;
	}

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		MergeContext mergeContext = mergeContextCache.getAnnotation(type);
		if (mergeContext == null)
		{
			return null;
		}
		IMethodLevelBehavior<Annotation> behavior = createInterceptorModeBehavior(type);

		MergeInterceptor mergeInterceptor = new MergeInterceptor();
		if (beanContext.isRunning())
		{
			return beanContext.registerWithLifecycle(mergeInterceptor)//
					.propertyValue("Behavior", behavior)//
					.ignoreProperties("ServiceName")//
					.finish();
		}
		beanContextFactory.registerWithLifecycle(mergeInterceptor)//
				.propertyValue("Behavior", behavior)//
				.ignoreProperties("ServiceName");
		return mergeInterceptor;
	}

	@Override
	protected Annotation lookForAnnotation(AnnotatedElement member)
	{
		Annotation annotation = super.lookForAnnotation(member);
		if (annotation != null)
		{
			return annotation;
		}
		NoProxy noProxy = member.getAnnotation(NoProxy.class);
		if (noProxy != null)
		{
			return noProxy;
		}
		com.koch.ambeth.util.annotation.Process process = member.getAnnotation(com.koch.ambeth.util.annotation.Process.class);
		if (process != null)
		{
			return process;
		}
		Find find = member.getAnnotation(Find.class);
		if (find != null)
		{
			return find;
		}
		Merge merge = member.getAnnotation(Merge.class);
		if (merge != null)
		{
			return merge;
		}
		return member.getAnnotation(Remove.class);
	}
}
