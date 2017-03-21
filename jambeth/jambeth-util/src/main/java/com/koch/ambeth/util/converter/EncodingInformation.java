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

public final class EncodingInformation {
	public static final int SOURCE_PLAIN = 0, SOURCE_HEX = 1, SOURCE_BASE64 = 2, TARGET_PLAIN = 0,
			TARGET_HEX = 4, TARGET_BASE64 = 8;

	public static EncodingType getSourceEncoding(int encoding) {
		if ((encoding & SOURCE_HEX) != 0) {
			return EncodingType.HEX;
		}
		else if ((encoding & SOURCE_BASE64) != 0) {
			return EncodingType.BASE64;
		}
		return EncodingType.PLAIN;
	}

	public static EncodingType getTargetEncoding(int encoding) {
		if ((encoding & TARGET_HEX) != 0) {
			return EncodingType.HEX;
		}
		else if ((encoding & TARGET_BASE64) != 0) {
			return EncodingType.BASE64;
		}
		return EncodingType.PLAIN;
	}

	private EncodingInformation() {
		// Intended blank
	}
}
