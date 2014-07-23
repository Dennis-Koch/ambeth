package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.collections.SmartCopyMap;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.util.ReflectUtil;

public class MethodLevelBehavior<T> implements IMethodLevelBehavior<T>
{
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
			IBehaviorTypeExtractor<A, T> behaviourTypeExtractor, IBeanContextFactory beanContextFactory, IServiceContext beanContext)
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
		T defaultBehaviour = behaviourTypeExtractor.extractBehaviorType(annotation);
		MethodLevelHashMap<T> methodLevelBehaviour = null;

		Method[] methods = ReflectUtil.getMethods(beanType);
		for (int a = methods.length; a-- > 0;)
		{
			Method method = methods[a];
			A annotationOnMethod = annotationCache.getAnnotation(method);
			if (annotationOnMethod != null)
			{
				if (methodLevelBehaviour == null)
				{
					methodLevelBehaviour = new MethodLevelHashMap<T>();
				}
				T behaviourTypeOnMethod = behaviourTypeExtractor.extractBehaviorType(annotationOnMethod);
				if (behaviourTypeOnMethod != null)
				{
					methodLevelBehaviour.put(method.getName(), method.getParameterTypes(), behaviourTypeOnMethod);
				}
			}
		}
		if (methodLevelBehaviour == null)
		{
			methodLevelBehaviour = new MethodLevelHashMap<T>(0);
		}
		behavior = new MethodLevelBehavior<T>(defaultBehaviour, methodLevelBehaviour);
		beanTypeToBehavior.put(key, behavior);
		return behavior;
	}

	protected final T defaultBehaviour;

	protected final MethodLevelHashMap<T> methodLevelBehaviour;

	public MethodLevelBehavior(T defaultBehaviour, MethodLevelHashMap<T> methodLevelBehaviour)
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

	public Tuple2KeyHashMap<String, Class<?>[], T> getMethodLevelBehaviour()
	{
		return methodLevelBehaviour;
	}

	@Override
	public T getBehaviourOfMethod(Method method)
	{
		T behaviourOfMethod = methodLevelBehaviour.get(method.getName(), method.getParameterTypes());

		if (behaviourOfMethod == null)
		{
			behaviourOfMethod = defaultBehaviour;
		}
		return behaviourOfMethod;
	}
}
