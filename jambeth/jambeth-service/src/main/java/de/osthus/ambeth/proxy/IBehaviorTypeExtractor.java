package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;

public interface IBehaviorTypeExtractor<A extends Annotation, T>
{
	T extractBehaviorType(A annotation);
}