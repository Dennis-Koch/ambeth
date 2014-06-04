package de.osthus.ambeth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If PropertyChangeAspect is added to a class or interface the bytecode enhancement will add support for PropertyChange implementing {@link INotifyPropertyChanged}.
 * 
 * @see NotifyPropertyChangedBehavior
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropertyChangeAspect
{
	// Intended blank
}