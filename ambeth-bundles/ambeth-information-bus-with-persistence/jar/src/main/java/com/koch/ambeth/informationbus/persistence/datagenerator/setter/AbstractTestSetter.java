package com.koch.ambeth.informationbus.persistence.datagenerator.setter;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
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

import java.util.Map;
import java.util.Objects;

import com.koch.ambeth.informationbus.persistence.datagenerator.ITestSetter;
import com.koch.ambeth.util.ParamChecker;

public abstract class AbstractTestSetter implements ITestSetter {

	private final Class<?> type;

	public AbstractTestSetter(Class<?> type) {
		this.type = type;
	}

	@Override
	public boolean isApplicable(Class<?> parameter) {
		return type.isAssignableFrom(parameter);
	}

	@Override
	public void compareResult(String propertyName, Map<Object, Object> arguments, Object content) {
		Object parameter = createParameter(propertyName, arguments);
		ParamChecker.assertTrue(Objects.equals(parameter, content), propertyName);
	}
}
