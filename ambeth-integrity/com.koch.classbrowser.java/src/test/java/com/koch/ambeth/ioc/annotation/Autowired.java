package com.koch.ambeth.ioc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired
{
	Class<?> value() default Object.class;

	String name() default "";

	boolean optional() default false;
}
