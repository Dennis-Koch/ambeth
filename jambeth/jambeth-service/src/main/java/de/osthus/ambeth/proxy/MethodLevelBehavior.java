package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.util.MethodKey;
import de.osthus.ambeth.util.ReflectUtil;

public class MethodLevelBehavior<T> implements IMethodLevelBehavior<T>
{
	public interface IBehaviourTypeExtractor<A extends Annotation, T>
	{
		T extractBehaviourType(A annotation);
	}

	private static final IMethodLevelBehavior<Object> noBehavior = new NoBehavior();

	public static class BehaviorKey
	{
		private final Class<?> beanType;

		private final Class<?> behaviourType;

		public BehaviorKey(Class<?> beanType, Class<?> behaviourType)
		{
			this.beanType = beanType;
			this.behaviourType = behaviourType;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}
			if (!(obj instanceof BehaviorKey))
			{
				return false;
			}
			BehaviorKey other = (BehaviorKey) obj;
			return beanType.equals(other.beanType) && behaviourType.equals(other.behaviourType);
		}

		@Override
		public int hashCode()
		{
			return beanType.hashCode() ^ behaviourType.hashCode();
		}
	}

	@SuppressWarnings("rawtypes")
	private static final SmartCopyMap<BehaviorKey, IMethodLevelBehavior> beanTypeToBehavior = new SmartCopyMap<BehaviorKey, IMethodLevelBehavior>(0.5f);

	public static <A extends Annotation, T> IMethodLevelBehavior<T> create(Class<?> beanType, AnnotationCache<A> annotationCache, Class<T> behaviourType,
			IBehaviourTypeExtractor<A, T> behaviourTypeExtractor, IBeanContextFactory beanContextFactory, IServiceContext beanContext)
	{
		BehaviorKey key = new BehaviorKey(beanType, behaviourType);
		@SuppressWarnings("unchecked")
		IMethodLevelBehavior<T> behavior = beanTypeToBehavior.get(key);
		if (behavior != null)
		{
			if (behavior == noBehavior)
			{
				return null;
			}
			return behavior;
		}
		A annotation = annotationCache.getAnnotation(beanType);
		if (annotation == null)
		{
			beanTypeToBehavior.put(key, noBehavior);
			return null;
		}
		T defaultBehaviour = behaviourTypeExtractor.extractBehaviourType(annotation);
		Map<MethodKey, T> methodLevelBehaviour = null;

		Method[] methods = ReflectUtil.getMethods(beanType);
		for (int a = methods.length; a-- > 0;)
		{
			Method method = methods[a];
			A annotationOnMethod = annotationCache.getAnnotation(method);
			if (annotationOnMethod != null)
			{
				if (methodLevelBehaviour == null)
				{
					methodLevelBehaviour = new HashMap<MethodKey, T>();
				}
				MethodKey methodKey = new MethodKey(method.getName(), method.getParameterTypes());
				T behaviourTypeOnMethod = behaviourTypeExtractor.extractBehaviourType(annotationOnMethod);
				if (behaviourTypeOnMethod != null)
				{
					methodLevelBehaviour.put(methodKey, behaviourTypeOnMethod);
				}
			}
		}
		if (methodLevelBehaviour == null)
		{
			methodLevelBehaviour = Collections.<MethodKey, T> emptyMap();
		}
		behavior = new MethodLevelBehavior<T>(defaultBehaviour, methodLevelBehaviour);
		beanTypeToBehavior.put(key, behavior);
		return behavior;
	}

	protected final T defaultBehaviour;

	protected final Map<MethodKey, T> methodLevelBehaviour;

	public MethodLevelBehavior(T defaultBehaviour, Map<MethodKey, T> methodLevelBehaviour)
	{
		super();
		this.defaultBehaviour = defaultBehaviour;
		this.methodLevelBehaviour = methodLevelBehaviour;
	}

	@Override
	public T getDefaultBehaviour()
	{
		return defaultBehaviour;
	}

	public Map<MethodKey, T> getMethodLevelBehaviour()
	{
		return methodLevelBehaviour;
	}

	@Override
	public T getBehaviourOfMethod(Method method)
	{
		MethodKey methodKey = new MethodKey(method.getName(), method.getParameterTypes());
		T behaviourOfMethod = methodLevelBehaviour.get(methodKey);

		if (behaviourOfMethod == null)
		{
			behaviourOfMethod = defaultBehaviour;
		}
		return behaviourOfMethod;
	}
}
