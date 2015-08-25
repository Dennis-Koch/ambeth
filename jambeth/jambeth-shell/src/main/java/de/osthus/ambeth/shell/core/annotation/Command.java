package de.osthus.ambeth.shell.core.annotation;

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
@Target(ElementType.METHOD)
public @interface Command
{
	/**
	 * the name of the command
	 * 
	 * @return
	 */
	String name();

	/**
	 * description used to generate help info
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
}
