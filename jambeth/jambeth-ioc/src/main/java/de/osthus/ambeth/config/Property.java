package de.osthus.ambeth.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Property
{
	public static final String DEFAULT_VALUE = "##unspecified##";

	String name() default DEFAULT_VALUE;

	boolean mandatory() default true;

	String defaultValue() default DEFAULT_VALUE;
}
