package de.osthus.ambeth.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface PersistenceContext
{

	public static enum PersistenceContextType
	{
		NOT_REQUIRED, REQUIRED, REQUIRED_READ_ONLY, FORBIDDEN;
	}

	PersistenceContextType value() default PersistenceContextType.REQUIRED;
}
