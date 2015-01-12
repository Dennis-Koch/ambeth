package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface IBehaviorTypeExtractor<A extends Annotation, T>
{
	T extractBehaviorType(A annotation, AnnotatedElement annotatedElement);
}