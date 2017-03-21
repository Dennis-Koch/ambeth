package com.koch.ambeth.bytecode;

/*-
 * #%L
 * jambeth-bytecode
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

import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class TypeUtil {
	public static final Type[] getClassesToTypes(Class<?>[] classes) {
		Type[] types = new Type[classes.length];
		for (int a = classes.length; a-- > 0;) {
			types[a] = Type.getType(classes[a]);
		}
		return types;
	}

	public static final int getModifiersToAccess(int modifiers) {
		int access = 0;
		if ((modifiers & Modifier.PUBLIC) != 0) {
			access |= Opcodes.ACC_PUBLIC;
		}
		if ((modifiers & Modifier.PRIVATE) != 0) {
			access |= Opcodes.ACC_PRIVATE;
		}
		if ((modifiers & Modifier.PROTECTED) != 0) {
			access |= Opcodes.ACC_PROTECTED;
		}
		if ((modifiers & Modifier.FINAL) != 0) {
			access |= Opcodes.ACC_FINAL;
		}
		if ((modifiers & Modifier.STATIC) != 0) {
			access |= Opcodes.ACC_STATIC;
		}
		if ((modifiers & Modifier.TRANSIENT) != 0) {
			access |= Opcodes.ACC_TRANSIENT;
		}
		return access;
	}

	public static boolean isPrimitive(Type type) {
		return Type.BOOLEAN_TYPE.equals(type) || Type.BYTE_TYPE.equals(type)
				|| Type.CHAR_TYPE.equals(type) || Type.DOUBLE_TYPE.equals(type)
				|| Type.SHORT_TYPE.equals(type) || Type.FLOAT_TYPE.equals(type)
				|| Type.INT_TYPE.equals(type) || Type.LONG_TYPE.equals(type);
	}

	public static boolean isPrimitiveOrVoid(Type type) {
		return isPrimitive(type) || Type.VOID_TYPE.equals(type);
	}

	private TypeUtil() {
		// Intended blank
	}
}
