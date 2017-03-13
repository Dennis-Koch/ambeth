package com.koch.ambeth.util.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface IAnnotationInfo<A extends Annotation>
{
	A getAnnotation();

	AnnotatedElement getAnnotatedElement();
}