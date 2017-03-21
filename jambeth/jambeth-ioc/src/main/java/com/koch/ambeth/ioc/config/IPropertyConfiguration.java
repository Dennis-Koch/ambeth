package com.koch.ambeth.ioc.config;

/*-
 * #%L
 * jambeth-ioc
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

/**
 * Interface for property configurations during bean registration.
 */
public interface IPropertyConfiguration
{
	/**
	 * Getter for the Stacktrace where this instance has been declared
	 * 
	 * @return Stacktrace of declaration
	 */
	StackTraceElement[] getDeclarationStackTrace();

	/**
	 * Retrieves the bean configuration which constains this property configuration
	 * 
	 * @return
	 */
	IBeanConfiguration getBeanConfiguration();

	/**
	 * Getter for the name of the target property.
	 * 
	 * @return Property name.
	 */
	String getPropertyName();

	/**
	 * Getter for the name of the IoC context where the bean to inject on the property has to be looked up.
	 * 
	 * @return Bean name of the IoC context.
	 */
	String getFromContext();

	/**
	 * Getter for the name of the target been. Even unnamed beans are referred to by the IoC container by a generated name.
	 * 
	 * @return Bean name.
	 */
	String getBeanName();

	/**
	 * Flag if this property configuration is optional. If so this configuration will be ignored if the bean cannot be found.
	 * 
	 * @return True if this configuration is optional, false otherwise.
	 */
	boolean isOptional();

	/**
	 * Getter for the value to be set in the referred beans referred property.
	 * 
	 * @return Value to be set.
	 */
	Object getValue();
}
