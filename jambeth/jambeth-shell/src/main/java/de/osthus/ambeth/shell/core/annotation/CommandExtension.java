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
@Target(ElementType.TYPE)
public @interface CommandExtension
{
	/**
	 * the name of the command
	 * 
	 * @return
	 */
	String command();

}
