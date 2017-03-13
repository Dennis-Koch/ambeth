package com.koch.ambeth.ioc.link;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used this annotation on specific optional parameters on listener interface methods. The
 * annotation gets considered in cases where a extension bean links one of its methods (not itself)
 * as a delegate argument to an extendable interface register/unregister operation.
 *
 * One specific example of its use is in the {@link com.koch.ambeth.event.IEventListener}.
 *
 * If used on a parameter the delegate method may omit this parameter in its declaration and the
 * Ambeth Link API still finds the "binding" to create the correct delegate handle. Of course the
 * delegate method on the extension bean may still define this parameter if it wants to consume it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface LinkOptional {
	// intended blank
}
