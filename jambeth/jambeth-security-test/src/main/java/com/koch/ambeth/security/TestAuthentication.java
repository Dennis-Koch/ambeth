package com.koch.ambeth.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.koch.ambeth.security.StringSecurityScope;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface TestAuthentication
{
	String name();

	String password();

	String scope() default StringSecurityScope.DEFAULT_SCOPE_NAME;
}
