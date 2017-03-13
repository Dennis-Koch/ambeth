package com.koch.ambeth.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface SQLStructure
{
	String[] value() default {};

	Class<? extends ISchemaRunnable> type() default ISchemaRunnable.class;
}
