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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.ioc.accessor.AccessorTypeProvider;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class MethodGenerator extends GeneratorAdapter {
	public static final MethodInstance m_getClass = new MethodInstance(
			ReflectUtil.getDeclaredMethod(false, Object.class, Class.class, "getClass"));

	public static final MethodInstance m_isAssignableFrom = new MethodInstance(ReflectUtil
			.getDeclaredMethod(false, Class.class, boolean.class, "isAssignableFrom", Class.class));

	protected final MethodInstance method;
	protected final ClassGenerator cg;

	protected final Printer methodPrinter = new Textifier();

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, Type owner, int access,
			Type returnType, String name, String signature, Type... parameters) {
		this(cg, mv, new MethodInstance(owner, access, returnType, name, signature, parameters));
	}

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, Type owner, int access, Method method,
			String signature) {
		this(cg, mv, new MethodInstance(owner, access, method, signature));
	}

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, Type owner,
			java.lang.reflect.Method method) {
		this(cg, mv, new MethodInstance(method));
	}

	public MethodGenerator(ClassGenerator cg, MethodVisitor mv, MethodInstance method) {
		super(Opcodes.ASM4, new TraceMethodVisitor(mv, new Textifier()), method.getAccess(),
				method.getName(), method.getDescriptor());
		this.cg = cg;
		this.method = method;
	}

	public MethodInstance getMethod() {
		return method;
	}

	public ClassGenerator getClassGenerator() {
		return cg;
	}

	public int newLocal(Class<?> localVariableType) {
		ParamChecker.assertParamNotNull(localVariableType, "localVariableType");
		return newLocal(Type.getType(localVariableType));
	}

	public void newInstance(ConstructorInstance constructor, Script argumentsScript) {
		ParamChecker.assertParamNotNull(constructor, "constructor");
		newInstance(constructor.getOwner());
		dup();
		if (argumentsScript != null) {
			argumentsScript.execute(this);
		}
		invokeConstructor(constructor);
	}

	public void invokeConstructor(ConstructorInstance constructor) {
		ParamChecker.assertParamNotNull(constructor, "constructor");
		invokeSuper(constructor);
	}

	public void invokeSuper(MethodInstance method) {
		ParamChecker.assertParamNotNull(method, "method");
		Type currType = BytecodeBehaviorState.getState().getNewType();
		if (currType.equals(method.getOwner())) {
			// Given method is not a super method. We look in the existing class hierarchy for a method
			// with the same signature
			if (ConstructorInstance.CONSTRUCTOR_NAME.equals(method.getName())) {
				Constructor<?> c_method = ReflectUtil.getDeclaredConstructor(true,
						BytecodeBehaviorState.getState().getCurrentType(), method.getParameters());

				if (c_method == null) {
					throw new IllegalArgumentException("Constructor has no super implementation: " + method);
				}
				method = new ConstructorInstance(c_method);
			}
			else {
				java.lang.reflect.Method r_method =
						ReflectUtil.getDeclaredMethod(true, BytecodeBehaviorState.getState().getCurrentType(),
								method.getReturnType(), method.getName(), method.getParameters());
				if (r_method == null) {
					throw new IllegalArgumentException("Method has no super implementation: " + method);
				}
				method = new MethodInstance(r_method);
			}
		}
		invokeOnExactOwner(method);
	}

	public void invokeSuperOfCurrentMethod() {
		invokeSuper(getMethod());
	}

	public void invokeOnExactOwner(java.lang.reflect.Method method) {
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getModifiers() & Modifier.STATIC) != 0) {
			throw new IllegalArgumentException("Given method is not virtual: " + method);
		}
		invokeSuper(new MethodInstance(method));
	}

	public void invokeOnExactOwner(MethodInstance method) {
		ParamChecker.assertParamNotNull(method, "method");
		invokeConstructor(method.getOwner(), method.method);
	}

	public void invokeVirtual(MethodInstance method) {
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getAccess() & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalArgumentException("Given method is not virtual: " + method);
		}
		Type owner = method.getOwner();
		if (owner == null) {
			owner = BytecodeBehaviorState.getState().getNewType();
		}
		invokeVirtual(owner, method.getMethod());
	}

	public void invokeInterface(MethodInstance method) {
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getAccess() & Opcodes.ACC_STATIC) != 0) {
			throw new IllegalArgumentException("Given method is not virtual: " + method);
		}
		Type owner = method.getOwner();
		if (owner == null) {
			owner = BytecodeBehaviorState.getState().getNewType();
		}
		invokeInterface(method.getOwner(), method.getMethod());
	}

	public void invokeSuperOf(java.lang.reflect.Method method) {
		ParamChecker.assertParamNotNull(method, "method");
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		java.lang.reflect.Method superMethod =
				ReflectUtil.getDeclaredMethod(false, state.getCurrentType(), method.getReturnType(),
						method.getName(), method.getParameterTypes());
		invokeSuper(new MethodInstance(superMethod));
	}

	public void invokeStatic(java.lang.reflect.Method method) {
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getModifiers() & Modifier.STATIC) == 0) {
			throw new IllegalArgumentException("Given method is not static: " + method);
		}
		invokeStatic(Type.getType(method.getDeclaringClass()), Method.getMethod(method));
	}

	public void invokeStatic(MethodInstance method) {
		ParamChecker.assertParamNotNull(method, "method");
		if ((method.getAccess() & Opcodes.ACC_STATIC) == 0) {
			throw new IllegalArgumentException("Given method is not static: " + method);
		}
		invokeStatic(method.getOwner(), method.getMethod());
	}

	@Override
	public void box(Type typeToBox) {
		if (Type.BOOLEAN_TYPE.equals(typeToBox)) {
			invokeStatic(
					new MethodInstance(null, Boolean.class, Boolean.class, "valueOf", boolean.class));
		}
		else if (TypeUtil.isPrimitive(typeToBox)) {
			super.box(typeToBox);
		}
	}

	public void callThisGetter(MethodInstance method) {
		ParamChecker.assertParamNotNull(method, "method");
		MethodInstance existingMethodInstance = MethodInstance.findByTemplate(method, false);
		if (!existingMethodInstance.getOwner().equals(method.getOwner())) {
			callThisGetter(existingMethodInstance);
			return;
		}
		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			loadThis();
			invokeVirtual(method);
		}
		else {
			invokeStatic(method);
		}
	}

	public void callThisGetter(PropertyInstance property) {
		ParamChecker.assertParamNotNull(property, "property");
		callThisGetter(property.getGetter());
	}

	public void callThisSetter(MethodInstance method, Script script) {
		ParamChecker.assertParamNotNull(method, "method");
		ParamChecker.assertParamNotNull(script, "script");
		if ((method.access & Opcodes.ACC_STATIC) == 0) {
			loadThis();
			script.execute(this);
			invokeVirtual(method);
		}
		else {
			script.execute(this);
			invokeStatic(method);
		}
	}

	public void callThisSetter(PropertyInstance property, Script script) {
		ParamChecker.assertParamNotNull(property, "property");
		ParamChecker.assertParamNotNull(script, "script");
		callThisSetter(property.getSetter(), script);
	}

	public void getThisField(FieldInstance field) {
		ParamChecker.assertParamNotNull(field, "field");
		if ((field.access & Opcodes.ACC_STATIC) == 0) {
			loadThis();
			getField(field.getOwner(), field.getName(), field.getType());
		}
		else {
			getStatic(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void getField(FieldInstance field) {
		ParamChecker.assertParamNotNull(field, "field");
		if ((field.access & Opcodes.ACC_STATIC) == 0) {
			getField(field.getOwner(), field.getName(), field.getType());
		}
		else {
			getStatic(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void putThisField(FieldInstance field, Script script) {
		ParamChecker.assertParamNotNull(field, "field");
		ParamChecker.assertParamNotNull(script, "script");
		if ((field.access & Opcodes.ACC_STATIC) == 0) {
			loadThis();
			script.execute(this);
			putField(field.getOwner(), field.getName(), field.getType());
		}
		else {
			script.execute(this);
			putStatic(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void putField(FieldInstance field) {
		ParamChecker.assertParamNotNull(field, "field");
		if ((field.access & Opcodes.ACC_STATIC) == 0) {
			putField(field.getOwner(), field.getName(), field.getType());
		}
		else {
			putStatic(field.getOwner(), field.getName(), field.getType());
		}
	}

	public void returnVoidOrThis() {
		if (!Type.VOID_TYPE.equals(method.getReturnType())) {
			loadThis();
		}
		returnValue();
	}

	public void pushNull() {
		push((String) null);
	}

	public void pushNullOrZero(Class<?> type) {
		pushNullOrZero(Type.getType(type));
	}

	public void pushNullOrZero(Type type) {
		switch (type.getSort()) {
			case Type.LONG:
				push((long) 0);
				break;
			case Type.DOUBLE:
				push((double) 0);
				break;
			case Type.FLOAT:
				push((float) 0);
				break;
			case Type.BOOLEAN:
				push(false);
				break;
			case Type.BYTE:
			case Type.CHAR:
			case Type.SHORT:
			case Type.INT:
				push(0);
				break;
			case Type.ARRAY:
			case Type.OBJECT:
				pushNull();
				break;
			default:
				throw new IllegalArgumentException("Sort not supported: " + type.getSort());
		}
	}

	/**
	 * Generates the instructions to jump to a label based on the comparison of the top two stack
	 * values.
	 *
	 * @param type the type of the top two stack values.
	 * @param mode how these values must be compared. One of EQ, NE, LT, GE, GT, LE.
	 * @param label where to jump if the comparison result is <tt>true</tt>.
	 */
	public void ifCmp(final Class<?> type, final int mode, final Label label) {
		ifCmp(Type.getType(type), mode, label);
	}

	public void ifZCmp(final Type type, final int mode, final Label label) {
		if (Type.DOUBLE_TYPE.equals(type) || Type.FLOAT_TYPE.equals(type)
				|| Type.LONG_TYPE.equals(type)) {
			pushNullOrZero(type);
			ifCmp(type, mode, label);
		}
		else {
			ifZCmp(mode, label);
		}
	}

	public void ifZCmp(final Class<?> type, final int mode, final Label label) {
		ifZCmp(Type.getType(type), mode, label);
	}

	public void ifThisInstanceOf(final Class<?> instanceOfType, Script loadValue,
			Script executeIfTrue, Script executeIfFalse) {
		if (executeIfTrue == null && executeIfFalse == null) {
			// nothing to do
			return;
		}
		push(instanceOfType);
		loadValue.execute(this);
		invokeVirtual(m_getClass);
		invokeVirtual(m_isAssignableFrom);

		if (executeIfTrue != null) {
			if (executeIfFalse != null) {
				Label l_isFalse = newLabel();
				Label l_finish = newLabel();
				ifZCmp(GeneratorAdapter.EQ, l_isFalse);
				executeIfTrue.execute(this);
				goTo(l_finish);
				mark(l_isFalse);
				executeIfFalse.execute(this);
				mark(l_finish);
			}
			else {
				Label l_isFalse = newLabel();
				ifZCmp(GeneratorAdapter.EQ, l_isFalse);
				executeIfTrue.execute(this);
				mark(l_isFalse);
			}
			return;
		}
		Label l_isTrue = newLabel();
		ifZCmp(GeneratorAdapter.NE, l_isTrue);
		executeIfFalse.execute(this);
		mark(l_isTrue);
	}

	public void smartBox(Type unboxedType) {
		AccessorTypeProvider.smartBox(this, unboxedType);
	}

	@Override
	public String toString() {
		if (!(mv instanceof TraceMethodVisitor)) {
			return super.toString();
		}
		StringWriter sw = new StringWriter();

		PrintWriter pw = new PrintWriter(sw);

		((TraceMethodVisitor) mv).p.print(pw);

		return sw.toString();
	}

	public void println(CharSequence text) {
		Type type = Type.getType(PrintStream.class);
		getStatic(Type.getType(System.class), "out", type);
		MethodInstance m_println =
				new MethodInstance(type, PrintStream.class, void.class, "println", String.class);
		push(text.toString());
		invokeVirtual(m_println);
	}

	public void popIfReturnValue(MethodInstance method) {
		if (Type.VOID_TYPE.equals(method.getReturnType())) {
			return;
		}
		pop();
	}

	public void push(Class<?> type) {
		push(type != null ? Type.getType(type) : null);
	}

	public void push(Boolean value) {
		if (value == null) {
			pushNull();
		}
		else if (value.booleanValue()) {
			getThisField(new FieldInstance(ReflectUtil.getDeclaredField(Boolean.class, "TRUE")));
		}
		else {
			getThisField(new FieldInstance(ReflectUtil.getDeclaredField(Boolean.class, "FALSE")));
		}
	}

	public <V extends Enum<?>> void pushEnum(V enumInstance) {
		ParamChecker.assertParamNotNull(enumInstance, "enumInstance");
		ParamChecker.assertTrue(enumInstance.getClass().isEnum(), "enumInstance");
		Class<?> owner = enumInstance.getClass();
		String fieldName = enumInstance.name();
		getThisField(new FieldInstance(ReflectUtil.getDeclaredField(owner, fieldName)));
	}

	public void tryFinally(Script tryScript, Script finallyScript) {
		Label tryLabel = newLabel();
		Label catchLabel = newLabel();
		Label successLabel = newLabel();

		visitTryCatchBlock(tryLabel, catchLabel, catchLabel, null);

		mark(tryLabel);

		tryScript.execute(this);
		goTo(successLabel);

		mark(catchLabel);
		int loc_throwable = newLocal(Throwable.class);
		storeLocal(loc_throwable);

		finallyScript.execute(this);

		loadLocal(loc_throwable);
		throwException();

		mark(successLabel);
		finallyScript.execute(this);
	}

	public void checkCast(Class<?> type) {
		checkCast(Type.getType(type));
	}

	@Override
	public void endMethod() {
		try {
			super.endMethod();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e,
					"Error occured while finishing method: " + getMethod() + "\n" + toString());
		}
	}
}
