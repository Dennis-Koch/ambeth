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
 * Annotate a property (getter or setter) to state that the property defined in "value" is
 * implicitly changed when the annotated property fires a PCE. It is used mostly in cases where the
 * target property can not be annotated (for whatever reason) with
 * {@link FireThisOnPropertyChange}<br>
 * Usage example:<br>
 *
 * <pre>
 * {
 * 	&#064;PropertyChangeAspect
 * 	public abstract class MyBean {
 * 		public abstract String getMyCalculatedProperty()
 * 	  {
 * 	    return "pre" + getMyDefinedProperty() + "post";
 * 	  }
 *
 * 		public abstract String getMyDefinedProperty();
 *
 * 		// whenever the property &quot;MyDefinedProperty&quot; fires a DCE the property &quot;MyCalculatedProperty&quot;
 * 		// is also fired and listeners are notified that the
 * 		// calculatedValue might have changed
 * 		&#064;FireTargetOnPropertyChange(&quot;MyCalculatedProperty&quot;)
 * 		public abstract void setMyDefinedProperty(String myDefinedProperty);
 * 	}
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface FireTargetOnPropertyChange {
	String[] value();
}
