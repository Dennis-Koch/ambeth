package de.osthus.ambeth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.osthus.ambeth.model.INotifyPropertyChanged;

/**
 * If PropertyChangeAspect is added to a class or interface the bytecode enhancement - if connected to the IOC container - will add support for PCEs
 * (PropertyChangeEvents) implementing {@link INotifyPropertyChanged}.
 * 
 * @see NotifyPropertyChangedBehavior
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropertyChangeAspect
{
	// Intended blank
}
