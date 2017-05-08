package com.koch.ambeth.util.exception;

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

import java.lang.reflect.InvocationTargetException;

import com.koch.ambeth.util.collections.IdentityHashSet;

public final class RuntimeExceptionUtil {
	public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	public static Throwable mask(Throwable e, Class<?>... exceptionTypes) {
		while (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof MaskingRuntimeException && e.getMessage() == null) {
			Throwable cause = e.getCause();
			for (int a = exceptionTypes.length; a-- > 0;) {
				if (exceptionTypes[a].isAssignableFrom(cause.getClass())) {
					return cause;
				}
			}
		}
		if (e instanceof RuntimeException || e instanceof Error) {
			return e;
		}
		for (int a = exceptionTypes.length; a-- > 0;) {
			if (exceptionTypes[a].isAssignableFrom(e.getClass())) {
				return e;
			}
		}
		MaskingRuntimeException re = new MaskingRuntimeException(e);
		re.setStackTrace(EMPTY_STACK_TRACE);
		return re;
	}

	public static RuntimeException mask(Throwable e, String message) {
		while (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof MaskingRuntimeException && e.getMessage() == null) {
			return mask(e.getCause(), message);
		}
		MaskingRuntimeException re = new MaskingRuntimeException(message, e);
		re.setStackTrace(EMPTY_STACK_TRACE);
		return re;
	}

	public static RuntimeException mask(Throwable e) {
		while (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		MaskingRuntimeException re = new MaskingRuntimeException(e);
		re.setStackTrace(EMPTY_STACK_TRACE);
		return re;
	}

	public static void fillInClientStackTraceIfPossible(Throwable ex) {
		if (ex == null) {
			return;
		}
		StackTraceElement[] clientStack = Thread.currentThread().getStackTrace();
		IdentityHashSet<Throwable> visitedExceptions = new IdentityHashSet<>();
		Throwable exToUpdate = ex;
		while (exToUpdate != null && !visitedExceptions.contains(exToUpdate)) {
			StackTraceElement[] serverStack = exToUpdate.getStackTrace();
			StackTraceElement[] combinedStack =
					new StackTraceElement[serverStack.length + clientStack.length];
			System.arraycopy(serverStack, 0, combinedStack, 0, serverStack.length);
			System.arraycopy(clientStack, 0, combinedStack, serverStack.length, clientStack.length);
			exToUpdate.setStackTrace(combinedStack);
			visitedExceptions.add(exToUpdate);
			exToUpdate = exToUpdate.getCause();
		}
	}

	public static RuntimeException createEnumNotSupportedException(Enum<?> enumInstance) {
		return new EnumNotSupportedException(enumInstance);
	}

	private RuntimeExceptionUtil() {
		// Intended blank
	}
}
