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

import com.koch.ambeth.util.IDedicatedConverter;

public class StringToLongArrayConverter implements IDedicatedConverter {
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		if (long[].class.equals(expectedType)) {
			String[] split = StringToPatternConverter.splitPattern.split((String) value);
			long[] result = new long[split.length];
			for (int a = split.length; a-- > 0;) {
				result[a] = Long.parseLong(split[a]);
			}
			return result;
		}
		StringBuilder sb = new StringBuilder();
		long[] array = (long[]) value;
		for (long item : array) {
			if (sb.length() > 0) {
				sb.append(StringToPatternConverter.splitPattern.pattern());
			}
			sb.append(item);
		}
		return sb.toString();
	}
}
