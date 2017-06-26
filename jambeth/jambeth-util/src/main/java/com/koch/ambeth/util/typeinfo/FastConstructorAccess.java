package com.koch.ambeth.util.typeinfo;

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

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_VARARGS;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_1;

import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.util.TypeUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public abstract class FastConstructorAccess<T> {
	private static Class<?>[] EMPTY_PARAM_TYPES = new Class<?>[0];

	public static <T> FastConstructorAccess<T> get(Class<T> type) {
		return get(type, EMPTY_PARAM_TYPES);
	}

	public static <T> FastConstructorAccess<T> get(Constructor<T> constructor) {
		return get(constructor.getDeclaringClass(), constructor.getParameterTypes());
	}

	@SuppressWarnings("unchecked")
	public static <T> FastConstructorAccess<T> get(Class<T> type, Class<?>... paramTypes) {
		if (paramTypes.length == 0) {
			paramTypes = EMPTY_PARAM_TYPES;
		}

		String accessClassName = type.getName() + "FastConstructorAccess";
		if (accessClassName.startsWith("java.")) {
			accessClassName = "com.koch.ambeth.repackaged." + accessClassName;
		}
		AccessClassLoader loader = AccessClassLoader.get(type);
		Class<? extends FastConstructorAccess<T>> accessClass;
		synchronized (loader) {
			try {
				accessClass = (Class<? extends FastConstructorAccess<T>>) loader.loadClass(accessClassName);
			}
			catch (ClassNotFoundException ignored) {
				accessClass = (Class<? extends FastConstructorAccess<T>>) createAccessClass(accessClassName,
						type, paramTypes);
			}
		}
		try {
			return accessClass.getConstructor(Class[].class).newInstance((Object) paramTypes);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected static Class<?> createAccessClass(String accessClassName, Class<?> type,
			Class<?>... paramTypes) {
		String accessClassNameInternal = accessClassName.replace('.', '/');
		String classNameInternal = Type.getInternalName(type);

		String constructorAccessName = Type.getInternalName(FastConstructorAccess.class);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, accessClassNameInternal, null, constructorAccessName,
				null);
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>",
					"(" + Type.getDescriptor(Class[].class) + ")V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESPECIAL, constructorAccessName, "<init>",
					"(" + Type.getDescriptor(Class[].class) + ")V");
			mv.visitInsn(RETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		{
			MethodVisitor mv =
					cw.visitMethod(ACC_PUBLIC, "newInstance", "()Ljava/lang/Object;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, classNameInternal);
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, classNameInternal, "<init>", "()V");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "newInstance",
					"([Ljava/lang/Object;)Ljava/lang/Object;", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, classNameInternal);
			mv.visitInsn(DUP);

			for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitIntInsn(BIPUSH, paramIndex);
				mv.visitInsn(AALOAD);
				Type paramType = Type.getType(paramTypes[paramIndex]);
				switch (paramType.getSort()) {
					case Type.BOOLEAN:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
						break;
					case Type.BYTE:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
						break;
					case Type.CHAR:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
						break;
					case Type.SHORT:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
						break;
					case Type.INT:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
						break;
					case Type.FLOAT:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
						break;
					case Type.LONG:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
						break;
					case Type.DOUBLE:
						mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
						break;
					case Type.ARRAY:
						mv.visitTypeInsn(CHECKCAST, paramType.getDescriptor());
						break;
					case Type.OBJECT:
						mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
						break;
				}
			}
			String desc =
					Type.getMethodDescriptor(Type.VOID_TYPE, TypeUtil.getClassesToTypes(paramTypes));
			mv.visitMethodInsn(INVOKESPECIAL, classNameInternal, "<init>", desc);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
		cw.visitEnd();
		byte[] data = cw.toByteArray();

		AccessClassLoader classLoader = AccessClassLoader.get(type);

		// ClassLoader classLoader = MethodAccessCache.get(type).getClass().getClassLoader();
		// Method m_defineClass = ReflectUtil.getDeclaredMethod(false, classLoader.getClass(),
		// "defineClass", String.class, byte[].class);
		try {
			return classLoader.defineClass(accessClassName, data);
			// return (Class<?>) m_defineClass.invoke(classLoader, accessClassName, data);
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private final Class<?>[] parameterTypes;

	public FastConstructorAccess(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public abstract T newInstance();

	public abstract T newInstance(Object... args);
}
