package com.koch.ambeth.ioc.converter;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;

public class StringToClassArrayConverter implements IDedicatedConverter {
	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		if (Class[].class.equals(expectedType)) {
			String[] split = StringToPatternConverter.splitPattern.split((String) value);
			Class<?>[] result = new Class<?>[split.length];
			for (int a = split.length; a-- > 0;) {
				result[a] = conversionHelper.convertValueToType(Class.class, split[a]);
			}
			return result;
		}
		StringBuilder sb = new StringBuilder();
		Class<?>[] array = (Class<?>[]) value;
		for (Class<?> item : array) {
			if (sb.length() > 0) {
				sb.append(StringToPatternConverter.splitPattern.pattern());
			}
			sb.append(item.getName());
		}
		return sb.toString();
	}
}
