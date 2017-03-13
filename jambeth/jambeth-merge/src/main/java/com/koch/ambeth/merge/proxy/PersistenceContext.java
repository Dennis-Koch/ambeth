package com.koch.ambeth.merge.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface PersistenceContext
{
	PersistenceContextType value() default PersistenceContextType.REQUIRED;
}
