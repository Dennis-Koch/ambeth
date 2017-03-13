package com.koch.ambeth.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached
{
	Class<?> type() default void.class;

	String alternateIdName() default "";

	boolean returnMisses() default false;
}
