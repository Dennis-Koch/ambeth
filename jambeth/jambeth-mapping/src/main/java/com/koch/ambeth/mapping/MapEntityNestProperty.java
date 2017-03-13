package com.koch.ambeth.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Field annotation is allowed to be able to use frameworks like lombok
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface MapEntityNestProperty
{
	/**
	 * the ExEntity property map to Entity nested Property, this is the nested path
	 */
	String[] value();
}
