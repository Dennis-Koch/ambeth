package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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

public interface IColumnEntry {
	String getFieldName();

	int getColumnIndex();

	Class<?> getJavaType();

	String getTypeName();

	boolean isNullable();

	int getRadix();

	/**
	 * Returns the information whether a mapping to a property of an entity is expected. If a mapping
	 * is not possible a warning will be logged if the mapping is expected (flag is true)
	 *
	 * @return true if a warning should be logged when a property can not be resolved at evaluation
	 *         time
	 */
	boolean expectsMapping();
}
