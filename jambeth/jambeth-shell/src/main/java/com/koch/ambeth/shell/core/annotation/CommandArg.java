package com.koch.ambeth.shell.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author daniel.mueller
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CommandArg
{
	/**
	 * the name of the argument, will be used to match the argument name in cli.
	 * 
	 * @return
	 */
	String name() default "";

	/**
	 * used for usage generation when the name of the arg is empty
	 * 
	 * @return
	 */
	String alt() default "";

	/**
	 * not used yet
	 * 
	 * @return
	 */
	String shortName() default "";

	/**
	 * used to generate help text
	 * 
	 * @return
	 */
	String description() default "";

	/**
	 * not used yet
	 * 
	 * @return
	 */
	String descriptionFile() default "";

	/**
	 * is argument optional or mandatory
	 * 
	 * @return
	 */
	boolean optional() default false;

	/**
	 * the default value
	 * 
	 * @return
	 */
	String defaultValue() default "";
}
