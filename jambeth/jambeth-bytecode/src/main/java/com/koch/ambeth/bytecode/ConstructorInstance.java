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

import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ConstructorInstance extends MethodInstance {
	public static final String CONSTRUCTOR_NAME = "<init>";

	public static final ConstructorInstance defaultConstructor;

	static {
		try {
			defaultConstructor = new ConstructorInstance(Object.class.getDeclaredConstructor());
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static final String getSignature(java.lang.reflect.Constructor<?> method) {
		try {
			var getSignature = method.getClass().getDeclaredMethod("getSignature");
			getSignature.setAccessible(true);
			return (String) getSignature.invoke(method);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public ConstructorInstance(java.lang.reflect.Constructor<?> constructor) {
		super(Type.getType(constructor.getDeclaringClass()),
				TypeUtil.getModifiersToAccess(constructor.getModifiers()), Type.VOID_TYPE, CONSTRUCTOR_NAME,
				getSignature(constructor), TypeUtil.getClassesToTypes(constructor.getParameterTypes()));
	}

	public ConstructorInstance(Type owner, int access, String signature, Type... parameters) {
		super(owner, access, Type.VOID_TYPE, CONSTRUCTOR_NAME, signature, parameters);
	}

	public ConstructorInstance(Class<?> owner, int access, String signature, Class<?>... parameters) {
		super(Type.getType(owner), access, Type.VOID_TYPE, CONSTRUCTOR_NAME, signature,
				TypeUtil.getClassesToTypes(parameters));
	}

	public ConstructorInstance(int access, String signature, Type... parameters) {
		super(BytecodeBehaviorState.getState().getNewType(), access, Type.VOID_TYPE, CONSTRUCTOR_NAME,
				signature, parameters);
	}

	public ConstructorInstance(int access, String signature, Class<?>... parameters) {
		super(BytecodeBehaviorState.getState().getNewType(), access, Type.VOID_TYPE, CONSTRUCTOR_NAME,
				signature, TypeUtil.getClassesToTypes(parameters));
	}

	@Override
	public MethodInstance deriveName(String methodName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConstructorInstance deriveAccess(int access) {
		return new ConstructorInstance(access, getSignature(), getParameters());
	}

	@Override
	public MethodInstance deriveOwner() {
		throw new UnsupportedOperationException();
	}
}
