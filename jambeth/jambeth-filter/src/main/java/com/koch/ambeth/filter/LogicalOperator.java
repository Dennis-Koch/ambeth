package com.koch.ambeth.filter;

/*-
 * #%L
 * jambeth-filter
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

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;

@XmlType
@XmlEnum
public enum LogicalOperator {
	/**
	 * OR logic for the combination of filters
	 *
	 */
	@XmlEnumValue("Or")
	OR("Or"),

	/**
	 * AND logic for the combination of filters
	 *
	 */
	@XmlEnumValue("And")
	AND("And");

	private final String value;

	LogicalOperator(String v) {
		value = v;
	}

	public String value() {
		return value;
	}
}
