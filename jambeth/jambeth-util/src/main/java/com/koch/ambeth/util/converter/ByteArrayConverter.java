package com.koch.ambeth.util.converter;

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

import com.koch.ambeth.util.IDedicatedConverter;

public class ByteArrayConverter extends AbstractEncodingArrayConverter
		implements IDedicatedConverter {
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value,
			Object additionalInformation) {
		EncodingType sourceEncoding = getSourceEncoding(additionalInformation);
		EncodingType targetEncoding = getTargetEncoding(additionalInformation);

		if (byte[].class.equals(sourceType) && CharSequence.class.isAssignableFrom(expectedType)) {
			byte[] bytes = switchEncoding((byte[]) value, sourceEncoding, targetEncoding);
			String text = new String(bytes, CHARSET_UTF8);
			return text;
		}
		else if (String.class.equals(sourceType) && byte[].class.equals(expectedType)) {
			byte[] bytes = ((String) value).getBytes(CHARSET_UTF8);
			bytes = switchEncoding(bytes, sourceEncoding, targetEncoding);
			return bytes;
		}
		else if (CharSequence.class.isAssignableFrom(sourceType) && byte[].class.equals(expectedType)) {
			byte[] bytes = ((CharSequence) value).toString().getBytes(CHARSET_UTF8);
			bytes = switchEncoding(bytes, sourceEncoding, targetEncoding);
			return bytes;
		}
		throw new IllegalStateException(
				"Conversion " + sourceType.getName() + "->" + expectedType.getName() + " not supported");
	}
}
