package com.koch.ambeth.persistence.sql;

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

import java.util.List;

public final class ParamsUtil {
	public static void addParam(List<Object> parameters, Object value) {
		parameters.add(value);
	}

	public static void addParams(List<Object> parameters, List<Object> values) {
		for (int i = 0, size = values.size(); i < size; i++) {
			parameters.add(values.get(i));
		}
	}

	private ParamsUtil() {
		// Intended blank
	}
}
