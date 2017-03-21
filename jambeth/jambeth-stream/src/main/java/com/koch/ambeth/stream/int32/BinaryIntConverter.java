package com.koch.ambeth.stream.int32;

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

public class BinaryIntConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (IIntInputStream.class.isAssignableFrom(sourceType))
			{
				return new IntToBinaryInputStream((IIntInputStream) value);
			}
			else if (IIntInputSource.class.isAssignableFrom(sourceType))
			{
				return new IntToBinaryInputStream(((IIntInputSource) value).deriveIntInputStream());
			}
			else if (int[].class.equals(sourceType))
			{
				return new IntToBinaryInputStream(new IntInMemoryInputStream((int[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IIntInputStream.class))
		{
			if (IIntInputSource.class.isAssignableFrom(sourceType))
			{
				return ((IIntInputSource) value).deriveIntInputStream();
			}
			else if (int[].class.equals(sourceType))
			{
				return new IntInMemoryInputStream((int[]) value);
			}
		}
		return null;
	}
}
