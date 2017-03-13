package com.koch.ambeth.testutil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.koch.ambeth.ioc.IInitializingModule;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface TestFrameworkModule
{
	Class<? extends IInitializingModule>[] value();
}
