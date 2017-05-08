package com.koch.ambeth.bytecode.visitor;

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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.bytecode.core.ByVisitor;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.annotation.PropertyAccessor;

public class LogImplementationsClassVisitor extends ClassGenerator {
	@LogInstance
	private ILogger log;

	public LogImplementationsClassVisitor(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature,
			Object value) {
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		FieldInstance field =
				new FieldInstance(state.getNewType(), access, name, signature, Type.getType(desc));
		if (log.isDebugEnabled() && !getState().getOriginalType().getSimpleName().contains("Member")) {
			log.debug("Implement field: " + field.toString());
		}
		((BytecodeBehaviorState) state).fieldImplemented(field);
		FieldVisitor fv = super.visitField(access, name, desc, signature, value);
		{
			AnnotationVisitor av = fv.visitAnnotation(Type.getDescriptor(ByVisitor.class), true);
			av.visit("value", extractCallingVisitor("visitField"));
			av.visitEnd();
		}
		return fv;
	}

	protected String extractCallingVisitor(String fromMethodName) {
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		StackTraceElement ste = null;
		for (int index = 0, size = stes.length; index < size; index++) {
			if (stes[index].getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (stes[index].getClassName().equals(ClassGenerator.class.getName())) {
				continue;
			}
			if (stes[index].getClassName().equals(LogImplementationsClassVisitor.class.getName())) {
				continue;
			}
			if (stes[index].getMethodName().equals(fromMethodName)) {
				continue;
			}
			ste = stes[index];
			break;
		}
		return ste.getClassName() + "#" + ste.getMethodName();
	}

	@Override
	public MethodGenerator visitMethod(int access, String name, String desc, String signature,
			String[] exceptions) {
		IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
		MethodInstance method = new MethodInstance(state.getNewType(), access, name, signature, desc);
		if (log.isDebugEnabled() && !getState().getOriginalType().getSimpleName().contains("Member")) {
			log.debug("Implement method: " + method.toString());
		}
		PropertyInstance propertyInfo = null;
		String propertyName = null;// , eventName = null;
		Type propertyType = null;
		String propertySignature = null;
		String nameFromPropertyContext = ClassGenerator.getNameFromPropertyContext();
		Boolean isGetter = null;
		if (nameFromPropertyContext != null) {
			isGetter = Boolean.TRUE;
			propertyName = nameFromPropertyContext;
			// handle as getter
			propertyType = method.getReturnType();
			propertySignature = method.getSignatureFromReturnType();
		}
		else if (name.startsWith("get") && method.getParameters().length == 0) {
			isGetter = Boolean.TRUE;
			propertyName = name.substring(3);
			propertyType = method.getReturnType();
			propertySignature = method.getSignatureFromReturnType();
		}
		else if (name.startsWith("is") && method.getParameters().length == 0) {
			isGetter = Boolean.TRUE;
			propertyName = name.substring(2);
			propertyType = method.getReturnType();
			propertySignature = method.getSignatureFromReturnType();
		}
		else if (name.startsWith("set") && method.getParameters().length == 1) {
			isGetter = Boolean.FALSE;
			propertyName = name.substring(3);
			propertyType = method.getParameters()[0];
			propertySignature = method.getSignatureFromParameterType(0);
		}
		// else if (name.startsWith("add") && method.getParameters().length == 1)
		// {
		// eventName = name.substring(3);
		// propertyType = method.getParameters()[0];
		// }
		// else if (name.startsWith("remove") && method.getParameters().length == 1)
		// {
		// eventName = name.substring(6);
		// propertyType = method.getParameters()[0];
		// }
		if (propertyName != null) {
			propertyInfo = getState().getProperty(propertyName, propertyType);
			if (propertyInfo == null) {
				propertyInfo = new PropertyInstance(propertyName, propertySignature, propertyType);
				((BytecodeBehaviorState) getState()).propertyImplemented(propertyInfo);
			}
		}
		// else if (eventName != null)
		// {
		// eventInfo = ((BytecodeBehaviorState) State).GetAlreadyImplementedEvent(eventName);
		// if (eventInfo == null)
		// {
		// CallingConventions cc = access.HasFlag(MethodAttributes.Static) ? CallingConventions.Standard
		// : CallingConventions.HasThis;
		// EventBuilder eb = tb.DefineEvent(eventName, EventAttributes.None, propertyType);
		// ((BytecodeBehaviorState) State).EventImplemented(eventName, eb);
		// eventInfo = eb;
		// }
		// }
		((BytecodeBehaviorState) state).methodImplemented(method);
		if (propertyInfo != null && propertyInfo.isConfigurable()) {
			if (Boolean.TRUE.equals(isGetter)) {
				propertyInfo.setGetter(method);
			}
			else if (Boolean.FALSE.equals(isGetter)) {
				propertyInfo.setSetter(method);
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		MethodGenerator mg = super.visitMethod(access, name, desc, signature, exceptions);
		if (propertyInfo != null && propertyInfo.isConfigurable()) {
			if (Boolean.TRUE.equals(isGetter)) {
				propertyInfo.setGetterGen(mg);
			}
			else if (Boolean.FALSE.equals(isGetter)) {
				propertyInfo.setSetterGen(mg);
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		if (nameFromPropertyContext != null) {
			AnnotationVisitor av = mg.visitAnnotation(Type.getDescriptor(PropertyAccessor.class), true);
			av.visit("value", nameFromPropertyContext);
			av.visitEnd();
		}
		{
			AnnotationVisitor av = mg.visitAnnotation(Type.getDescriptor(ByVisitor.class), true);
			av.visit("value", extractCallingVisitor("visitMethod"));
			av.visitEnd();
		}
		return mg;
	}
}
