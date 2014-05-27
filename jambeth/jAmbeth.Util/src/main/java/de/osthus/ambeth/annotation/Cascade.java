package de.osthus.ambeth.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Cascade
{
	CascadeLoadMode load() default CascadeLoadMode.DEFAULT;
}
