package de.osthus.ambeth.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface SecurityContext
{
	public static enum SecurityContextType
	{
		AUTHORIZED, AUTHENTICATED, NOT_REQUIRED;
	}

	SecurityContextType value() default SecurityContextType.AUTHORIZED;
}
