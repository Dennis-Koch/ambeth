package de.osthus.ambeth.ioc.threadlocal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Forkable
{
	ForkableType value() default ForkableType.REFERENCE;

	@SuppressWarnings("rawtypes")
	Class<? extends IForkProcessor> processor() default IForkProcessor.class;
}
