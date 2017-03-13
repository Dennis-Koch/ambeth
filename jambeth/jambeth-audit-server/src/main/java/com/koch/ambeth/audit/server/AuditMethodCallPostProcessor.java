package com.koch.ambeth.audit.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

import com.koch.ambeth.ioc.IBeanRuntime;
import com.koch.ambeth.ioc.IOrderedBeanProcessor;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.ProcessorOrder;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.audit.model.AuditedArg;
import com.koch.ambeth.service.proxy.AbstractCascadePostProcessor;
import com.koch.ambeth.service.proxy.IBehaviorTypeExtractor;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.service.proxy.MethodLevelBehavior;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;

public class AuditMethodCallPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final AnnotationCache<Audited> annotationCache = new AnnotationCache<Audited>(Audited.class)
	{
		@Override
		protected boolean annotationEquals(Audited left, Audited right)
		{
			return true;
		}
	};

	protected final IBehaviorTypeExtractor<Audited, AuditInfo> auditMethodExtractor = new IBehaviorTypeExtractor<Audited, AuditInfo>()
	{
		@Override
		public AuditInfo extractBehaviorType(Audited annotation, AnnotatedElement annotatedElement)
		{
			AuditInfo auditInfo = new AuditInfo(annotation);

			if (annotatedElement instanceof Method)
			{
				Method method = (Method) annotatedElement;
				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				AuditedArg[] auditedArgs = new AuditedArg[parameterAnnotations.length];
				for (int i = 0; i < parameterAnnotations.length; i++)
				{
					AuditedArg aaa = null;
					for (Annotation parameterAnnotation : parameterAnnotations[i])
					{
						if (parameterAnnotation instanceof AuditedArg)
						{
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
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehavior<AuditInfo> behaviour = MethodLevelBehavior.create(type, annotationCache, AuditInfo.class, auditMethodExtractor,
				beanContextFactory, beanContext);
		if (behaviour == null)
		{
			return null;
		}
		AuditMethodCallInterceptor interceptor = new AuditMethodCallInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<AuditMethodCallInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
			interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
			return interceptorBC.finish();
		}
		IBeanConfiguration interceptorBC = beanContextFactory.registerWithLifecycle(interceptor);
		interceptorBC.propertyValue("MethodLevelBehaviour", behaviour);
		return interceptor;
	}

	@Override
	public ProcessorOrder getOrder()
	{
		return ProcessorOrder.LOW;
	}
}
