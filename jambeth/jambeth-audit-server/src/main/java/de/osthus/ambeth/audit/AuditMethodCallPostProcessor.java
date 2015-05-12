package de.osthus.ambeth.audit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.audit.model.Audited;
import de.osthus.ambeth.audit.model.AuditedArg;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.IBehaviorTypeExtractor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.proxy.MethodLevelBehavior;

public class AuditMethodCallPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
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
	public PostProcessorOrder getOrder()
	{
		return PostProcessorOrder.LOW;
	}
}
