package com.koch.ambeth.util.annotation;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a property (getter or setter) to state that this property is implicitly changed when a
 * "foreign" property fires a PCE.<br>
 * Usage example:<br>
 *
 * <pre>
 * {
 * 	&#064;PropertyChangeAspect
 * 	public abstract class MyBean {
 * 		// whenever the property &quot;MyDefinedProperty&quot; fires a DCE the property &quot;MyCalculatedProperty&quot;
 * 		// is also fired and listeners are notified that the
 * 		// calculatedValue might have changed
 * 		&#064;FireThisOnPropertyChange(&quot;MyDefinedProperty&quot;)
 * 	  public abstract String getMyCalculatedProperty()
 * 	  {
 * 	    return "pre" + getMyDefinedProperty() + "post";
 * 	  }
 *
 * 		public abstract String getMyDefinedProperty();
 *
 * 		public abstract void setMyDefinedProperty(String myDefinedProperty);
 * 	}
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface FireThisOnPropertyChange {
	String[] value();
}
