package de.osthus.ambeth.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public class AnnotationInfo<A extends Annotation> implements IAnnotationInfo<A>
{
	protected final A annotation;

	protected final AnnotatedElement annotatedElement;

	public AnnotationInfo(A annotation, AnnotatedElement annotatedElement)
	{
		this.annotation = annotation;
		this.annotatedElement = annotatedElement;
	}

	@Override
	public A getAnnotation()
	{
		return annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement()
	{
		return annotatedElement;
	}
}
