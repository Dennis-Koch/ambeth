package com.koch.ambeth.util;

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

/**
 * Interface for the Ambeth conversion feature. It is used throughout the framework to convert
 * values to different types in one unified way. Event the most basic Ambeth context has a bean
 * autowired to this interface. The conversion feature is extendable via the {@wiki wikipedia_en
 * Extensibility_pattern Extensibility pattern} by implementing the {@link IDedicatedConverter}
 * interface and linking the bean to {@link IDedicatedConverterExtendable}.
 */
public abstract class IConversionHelper {
	/**
	 * Primary method to convert values.
	 *
	 * @param expectedType Conversion target type.
	 * @param value Value to be converted.
	 * @return Representation of the given value as the target type.
	 */
	public abstract <T> T convertValueToType(Class<T> expectedType, Object value);

	/**
	 * Secondary method to convert values to specific types. Only used if the conversion needs
	 * additional informations, e.g. lost generic types, date format, string encoding.
	 *
	 * @param expectedType Conversion target type.
	 * @param value Value to be converted.
	 * @param additionalInformation Additional information needed for this conversion.
	 * @return Representation of the given value as the target type.
	 */
	public abstract <T> T convertValueToType(Class<T> expectedType, Object value,
			Object additionalInformation);
}
