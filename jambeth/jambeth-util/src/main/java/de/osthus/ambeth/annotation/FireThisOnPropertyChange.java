package de.osthus.ambeth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a property (getter or setter) to state that this property is implicitly changed when a "foreign" property fires a PCE.<br/>
 * Usage example:<br/>
 * 
 * <pre>
 * {
 * 	&#064;PropertyChangeAspect
 * 	public abstract class MyBean
 * 	{
 * 	  // whenever the property &quot;MyDefinedProperty&quot; fires a DCE the property &quot;MyCalculatedProperty&quot; is also fired and listeners are notified that the
 * 	  // calculatedValue might have changed
 * 	  &#064;FireThisOnPropertyChange(&quot;MyDefinedProperty&quot;)
 * 	  public abstract String getMyCalculatedProperty()
 * 	  {
 * 	    return "pre" + getMyDefinedProperty() + "post";
 * 	  }
 * 
 * 	  public abstract String getMyDefinedProperty();
 * 
 * 	  public abstract void setMyDefinedProperty(String myDefinedProperty);
 * 	}
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface FireThisOnPropertyChange
{
	String[] value();
}
