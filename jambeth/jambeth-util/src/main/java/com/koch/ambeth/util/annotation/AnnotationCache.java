package com.koch.ambeth.util.annotation;

/*-
 * #%L
 * jambeth-util
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.HashMap;

public abstract class AnnotationCache<T extends Annotation> extends HashMap<AnnotatedElement, AnnotationEntry<T>>
{
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected static final AnnotationEntry emptyAnnotationEntry = new AnnotationEntry(null, null);

	protected final Class<T> annotationType;

	protected final Lock writeLock = new ReentrantLock();

	public AnnotationCache(Class<T> annotationType)
	{
		this.annotationType = annotationType;
	}

	protected Lock getWriteLock()
	{
		return writeLock;
	}

	public T getAnnotation(AnnotatedElement type)
	{
		AnnotationEntry<T> entry = getAnnotationEntry(type);
		if (entry == null)
		{
			return null;
		}
		return entry.getAnnotation();
	}

	@SuppressWarnings("unchecked")
	public AnnotationEntry<T> getAnnotationEntry(AnnotatedElement annotatedElement)
	{
		AnnotationEntry<T> entry = get(annotatedElement);
		if (entry != null)
		{
			if (entry == emptyAnnotationEntry)
			{
				return null;
			}
			return entry;
		}
		Class<T> annotationType = this.annotationType;
		T annotation = annotatedElement.getAnnotation(annotationType);
		AnnotatedElement declaringAnnotatedElement = null;
		if (annotation == null)
		{
			if (annotatedElement instanceof Class)
			{
				Class<?> type = (Class<?>) annotatedElement;
				Class<?>[] interfaces = type.getInterfaces();
				for (int a = interfaces.length; a-- > 0;)
				{
					T interfaceAnnotation = interfaces[a].getAnnotation(annotationType);
					if (interfaceAnnotation == null)
					{
						continue;
					}
					if (annotation == null)
					{
						annotation = interfaceAnnotation;
						declaringAnnotatedElement = interfaces[a];
						continue;
					}
					if (!annotationEquals(annotation, interfaceAnnotation))
					{
						throw new IllegalStateException("Ambiguous annotation on type " + type.toString() + " with " + annotationType.toString()
								+ " based on multiple implementing interfaces with DIFFERENT " + annotationType.toString() + " values");
					}
				}
			}
		}
		else
		{
			declaringAnnotatedElement = annotatedElement;
		}
		entry = annotation != null ? new AnnotationEntry<T>(annotation, declaringAnnotatedElement) : emptyAnnotationEntry;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			if (!putIfNotExists(annotatedElement, entry))
			{
				entry = get(annotatedElement);
			}
			if (entry == emptyAnnotationEntry)
			{
				return null;
			}
			return entry;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected abstract boolean annotationEquals(T left, T right);
}
