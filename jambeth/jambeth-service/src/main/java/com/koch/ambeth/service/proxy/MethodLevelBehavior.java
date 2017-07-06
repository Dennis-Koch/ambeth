package com.koch.ambeth.service.proxy;

/*-
 * #%L
 * jambeth-service
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
import java.lang.reflect.Method;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.annotation.AnnotationCache;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class MethodLevelBehavior<T> implements IMethodLevelBehavior<T> {
	private static final IMethodLevelBehavior<Object> noBehavior = new NoBehavior();

	public static class BehaviorKey {
		private final Class<?> beanType;

		private final Class<?> behaviourType;

		public BehaviorKey(Class<?> beanType, Class<?> behaviourType) {
			this.beanType = beanType;
			this.behaviourType = behaviourType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof BehaviorKey)) {
				return false;
			}
			BehaviorKey other = (BehaviorKey) obj;
			return beanType.equals(other.beanType) && behaviourType.equals(other.behaviourType);
		}

		@Override
		public int hashCode() {
			return beanType.hashCode() ^ behaviourType.hashCode();
		}
	}

	@SuppressWarnings("rawtypes")
	private static final SmartCopyMap<BehaviorKey, IMethodLevelBehavior> beanTypeToBehavior = new SmartCopyMap<>(
			0.5f);

	public static <A extends Annotation, T> IMethodLevelBehavior<T> create(Class<?> beanType,
			AnnotationCache<A> annotationCache, Class<T> behaviourType,
			IBehaviorTypeExtractor<A, T> behaviourTypeExtractor, IBeanContextFactory beanContextFactory,
			IServiceContext beanContext) {
		BehaviorKey key = new BehaviorKey(beanType, behaviourType);
		@SuppressWarnings("unchecked")
		IMethodLevelBehavior<T> behavior = beanTypeToBehavior.get(key);
		if (behavior != null) {
			if (behavior == noBehavior) {
				return null;
			}
			return behavior;
		}
		A annotation = annotationCache.getAnnotation(beanType);
		if (annotation == null) {
			beanTypeToBehavior.put(key, noBehavior);
			return null;
		}
		T defaultBehaviour = behaviourTypeExtractor.extractBehaviorType(annotation, beanType);
		MethodLevelHashMap<T> methodLevelBehaviour = null;

		Method[] methods = ReflectUtil.getMethods(beanType);
		for (int a = methods.length; a-- > 0;) {
			Method method = methods[a];
			A annotationOnMethod = annotationCache.getAnnotation(method);
			if (annotationOnMethod == null) {
				annotationOnMethod = annotation;
			}
			if (methodLevelBehaviour == null) {
				methodLevelBehaviour = new MethodLevelHashMap<>();
			}
			T behaviourTypeOnMethod = behaviourTypeExtractor.extractBehaviorType(annotationOnMethod,
					method);
			if (behaviourTypeOnMethod != null && !behaviourTypeOnMethod.equals(defaultBehaviour)) {
				methodLevelBehaviour.put(method.getName(), method.getParameterTypes(),
						behaviourTypeOnMethod);
			}
		}
		if (methodLevelBehaviour == null || methodLevelBehaviour.size() == 0) {
			behavior = new SimpleMethodLevelBehavior<>(defaultBehaviour);
		}
		else {
			behavior = new MethodLevelBehavior<>(defaultBehaviour, methodLevelBehaviour);
		}
		beanTypeToBehavior.put(key, behavior);
		return behavior;
	}

	protected final T defaultBehaviour;

	protected final MethodLevelHashMap<T> methodLevelBehaviour;

	public MethodLevelBehavior(T defaultBehaviour, MethodLevelHashMap<T> methodLevelBehaviour) {
		super();
		this.defaultBehaviour = defaultBehaviour;
		this.methodLevelBehaviour = methodLevelBehaviour;
	}

	@Override
	public T getDefaultBehaviour() {
		return defaultBehaviour;
	}

	@Override
	public T getBehaviourOfMethod(Method method) {
		T behaviourOfMethod = methodLevelBehaviour.get(method.getName(), method.getParameterTypes());

		if (behaviourOfMethod == null) {
			behaviourOfMethod = defaultBehaviour;
		}
		return behaviourOfMethod;
	}

}
