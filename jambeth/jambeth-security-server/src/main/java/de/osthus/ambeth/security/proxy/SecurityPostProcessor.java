package de.osthus.ambeth.security.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IOrderedBeanPostProcessor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.PostProcessorOrder;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.proxy.AbstractCascadePostProcessor;
import de.osthus.ambeth.proxy.IBehaviorTypeExtractor;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.proxy.MethodLevelBehavior;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextPassword;
import de.osthus.ambeth.security.SecurityContextScope;
import de.osthus.ambeth.security.SecurityContextUserName;
import de.osthus.ambeth.security.SecurityFilterInterceptor;
import de.osthus.ambeth.security.SecurityFilterInterceptor.SecurityMethodMode;
import de.osthus.ambeth.security.StringSecurityScope;
import de.osthus.ambeth.util.EqualsUtil;

public class SecurityPostProcessor extends AbstractCascadePostProcessor implements IOrderedBeanPostProcessor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected AnnotationCache<SecurityContext> annotationCache = new AnnotationCache<SecurityContext>(SecurityContext.class)
	{
		@Override
		protected boolean annotationEquals(SecurityContext left, SecurityContext right)
		{
			return EqualsUtil.equals(left.value(), right.value());
		}
	};

	protected final IBehaviorTypeExtractor<SecurityContext, SecurityMethodMode> btExtractor = new IBehaviorTypeExtractor<SecurityContext, SecurityMethodMode>()
	{
		@Override
		public SecurityMethodMode extractBehaviorType(SecurityContext annotation, AnnotatedElement annotatedElement)
		{
			if (!(annotatedElement instanceof Method))
			{
				return new SecurityMethodMode(annotation.value());
			}
			Method method = (Method) annotatedElement;
			Annotation[][] parameterAnnotations = method.getParameterAnnotations();
			int userNameIndex = -1;
			int passwordIndex = -1;
			PasswordType passwordType = null;
			int securityScopeIndex = -1;
			for (int a = parameterAnnotations.length; a-- > 0;)
			{
				for (Annotation annotationOfParam : parameterAnnotations[a])
				{
					if (annotationOfParam instanceof SecurityContextUserName)
					{
						if (userNameIndex != -1)
						{
							throw new IllegalStateException("Annotation '" + SecurityContextUserName.class.getName() + "' ambiguous on method signature '"
									+ method.toGenericString() + "'");
						}
						userNameIndex = a;
					}
					else if (annotationOfParam instanceof SecurityContextPassword)
					{
						if (passwordIndex != -1)
						{
							throw new IllegalStateException("Annotation '" + SecurityContextPassword.class.getName() + "' ambiguous on method signature '"
									+ method.toGenericString() + "'");
						}
						passwordIndex = a;
						passwordType = ((SecurityContextPassword) annotationOfParam).value();
					}
					else if (annotationOfParam instanceof SecurityContextScope)
					{
						if (securityScopeIndex != -1)
						{
							throw new IllegalStateException("Annotation '" + SecurityContextScope.class.getName() + "' ambiguous on method signature '"
									+ method.toGenericString() + "'");
						}
						securityScopeIndex = a;
					}
				}
			}
			ISecurityScope securityScope = securityScopeIndex == -1 ? StringSecurityScope.DEFAULT_SCOPE : null;
			return new SecurityMethodMode(annotation.value(), userNameIndex, passwordIndex, passwordType, securityScopeIndex, securityScope);
		}
	};

	@Override
	protected ICascadedInterceptor handleServiceIntern(IBeanContextFactory beanContextFactory, IServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> type, Set<Class<?>> requestedTypes)
	{
		IMethodLevelBehavior<SecurityMethodMode> behaviour = MethodLevelBehavior.create(type, annotationCache, SecurityMethodMode.class, btExtractor,
				beanContextFactory, beanContext);
		if (behaviour == null)
		{
			return null;
		}
		SecurityFilterInterceptor interceptor = new SecurityFilterInterceptor();
		if (beanContext.isRunning())
		{
			IBeanRuntime<SecurityFilterInterceptor> interceptorBC = beanContext.registerWithLifecycle(interceptor);
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
		return PostProcessorOrder.HIGHER;
	}
}
