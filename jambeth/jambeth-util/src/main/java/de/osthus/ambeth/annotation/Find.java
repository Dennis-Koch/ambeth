package de.osthus.ambeth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Find
{
	Class<?> entityType() default Object.class;

	String queryName() default "";

	QueryResultType resultType() default QueryResultType.REFERENCES;

	String referenceIdName() default "";
}
