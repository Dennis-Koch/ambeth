package de.osthus.ambeth.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MapEntityNestProperty
{
	/**
	 * the ExEntity property map to Entity nested Property, this is the nested path
	 */
	String[] value();
}
