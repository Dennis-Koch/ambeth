package de.osthus.ambeth.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class AnnotationEntry<T extends Annotation>
{
	protected final T annotation;

	protected final AnnotatedElement declaringAnnotatedElement;

	public AnnotationEntry(T annotation, AnnotatedElement declaringAnnotatedElement)
	{
		this.annotation = annotation;
		this.declaringAnnotatedElement = declaringAnnotatedElement;
	}

	public T getAnnotation()
	{
		return annotation;
	}

	public AnnotatedElement getDeclaringAnnotatedElement()
	{
		return declaringAnnotatedElement;
	}

	public Class<?> getDeclaringType()
	{
		return (Class<?>) declaringAnnotatedElement;
	}
}
