package com.koch.ambeth.stream.float32;

/*-
 * #%L
 * jambeth-stream
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

import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.IDedicatedConverter;

public class BinaryFloatConverter implements IDedicatedConverter {
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		if (expectedType.isAssignableFrom(IBinaryInputStream.class)) {
			if (IFloatInputStream.class.isAssignableFrom(sourceType)) {
				return new FloatToBinaryInputStream((IFloatInputStream) value);
			}
			else if (IFloatInputSource.class.isAssignableFrom(sourceType)) {
				return new FloatToBinaryInputStream(((IFloatInputSource) value).deriveFloatInputStream());
			}
			else if (float[].class.equals(sourceType)) {
				return new FloatToBinaryInputStream(new FloatInMemoryInputStream((float[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IFloatInputStream.class)) {
			if (IFloatInputSource.class.isAssignableFrom(sourceType)) {
				return ((IFloatInputSource) value).deriveFloatInputStream();
			}
			else if (float[].class.equals(sourceType)) {
				return new FloatInMemoryInputStream((float[]) value);
			}
		}
		return null;
	}
}
