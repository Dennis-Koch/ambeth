package com.koch.ambeth.cache.stream;

/*-
 * #%L
 * jambeth-cache-test
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;

public class InputSourceTemplateFakeConverter implements IDedicatedConverter {
	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		Object values = ((InputSourceTemplateFake) value).getValues();
		return conversionHelper.convertValueToType(expectedType, values, additionalInformation);
	}
}
