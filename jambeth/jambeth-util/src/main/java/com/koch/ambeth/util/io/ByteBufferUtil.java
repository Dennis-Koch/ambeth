package com.koch.ambeth.util.io;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public final class ByteBufferUtil {
	public static void clean(ByteBuffer bb) {
		if (bb == null) {
			return;
		}
		try {
			Field cleanerField = bb.getClass().getDeclaredField("cleaner");
			cleanerField.setAccessible(true);
			Object cleaner = cleanerField.get(bb);
			if (cleaner == null) {
				return;
			}
			Method cleanMethod = cleaner.getClass().getMethod("clean");
			cleanMethod.invoke(cleaner);
		}
		catch (NoSuchFieldException e) {
			return;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private ByteBufferUtil() {
		// Intended blank
	}
}
