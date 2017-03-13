package com.koch.ambeth.util.annotation;

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
