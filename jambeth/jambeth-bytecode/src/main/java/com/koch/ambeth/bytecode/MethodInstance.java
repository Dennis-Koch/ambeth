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

import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.behavior.IBytecodeBehaviorState;
import com.koch.ambeth.util.ReflectUtil;
import lombok.SneakyThrows;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.burningwave.core.assembler.StaticComponentContainer.Methods;

public class MethodInstance {

    @SneakyThrows
    public static final String getSignature(java.lang.reflect.Method method) {
        return Methods.invokeDirect(method, "getGenericSignature");
    }

    public static final String buildSetterSignatureFromGetterSignature(String getterSignature) {
        if (getterSignature == null) {
            return null;
        }
        SignatureWriter sw = new SignatureWriter();

        SignatureReader sr = new SignatureReader(getterSignature);

        {
            SignatureVisitor pw = sw.visitParameterType();

            sr.accept(pw);

            pw.visitEnd();
        }
        return sw.toString();
    }

    public static MethodInstance findByTemplate(java.lang.reflect.Method methodTemplate) {
        return findByTemplate(methodTemplate, false);
    }

    public static MethodInstance findByTemplate(java.lang.reflect.Method methodTemplate, boolean tryOnly) {
        if (methodTemplate == null && tryOnly) {
            return null;
        }
        return findByTemplate(new MethodInstance(methodTemplate), tryOnly);
    }

    public static MethodInstance findByTemplate(MethodInstance methodTemplate, boolean tryOnly) {
        if (methodTemplate == null && tryOnly) {
            return null;
        }
        return findByTemplate(tryOnly, methodTemplate.getReturnType(), methodTemplate.getName(), methodTemplate.getParameters());
    }

    public static MethodInstance findByTemplate(boolean tryOnly, Class<?> returnType, String methodName, Class<?>... parameters) {
        return findByTemplate(tryOnly, Type.getType(returnType), methodName, TypeUtil.getClassesToTypes(parameters));
    }

    public static MethodInstance findByTemplate(boolean tryOnly, Type returnType, String methodName, Type... parameters) {
        IBytecodeBehaviorState state = BytecodeBehaviorState.getState();
        for (MethodInstance methodOnNewType : state.getAlreadyImplementedMethodsOnNewType()) {
            if (!methodOnNewType.getName().equals(methodName)) {
                continue;
            }
            Type[] paramsOnNewType = methodOnNewType.getParameters();
            if (paramsOnNewType.length != parameters.length) {
                continue;
            }
            boolean paramsEqual = true;
            for (int a = paramsOnNewType.length; a-- > 0; ) {
                if (!paramsOnNewType[a].equals(parameters[a])) {
                    paramsEqual = false;
                    break;
                }
            }
            if (!paramsEqual) {
                continue;
            }
            if (returnType == null || methodOnNewType.getReturnType().equals(returnType)) {
                return methodOnNewType;
            }
        }
        Class<?> currType = state.getCurrentType();
        if (!currType.isInterface()) {
            while (currType != null && currType != Object.class) {
                java.lang.reflect.Method method = ReflectUtil.getDeclaredMethod(true, currType, returnType, methodName, parameters);
                if (method != null) {
                    if ((method.getModifiers() & Modifier.ABSTRACT) != 0) {
                        // Method found but it is abstract. So it is not a callable instance
                        break;
                    }
                    return new MethodInstance(method);
                }
                currType = currType.getSuperclass();
            }
        }
        if (tryOnly) {
            return null;
        }
        throw new IllegalStateException("No method found on class hierarchy: " + methodName + ". Start type: " + state.getNewType());
    }

    protected final Type owner;

    protected final Method method;

    protected final int access;

    protected final String signature;

    public MethodInstance(Type owner, Class<?> declaringTypeOfMethod, Class<?> returnType, String methodName, Class<?>... parameters) {
        this(owner != null ? owner : Type.getType(declaringTypeOfMethod), ReflectUtil.getDeclaredMethod(false, declaringTypeOfMethod, returnType, methodName, parameters));
    }

    public MethodInstance(java.lang.reflect.Method method) {
        this(Type.getType(method.getDeclaringClass()), method);
    }

    public MethodInstance(java.lang.reflect.Method method, String signature) {
        this(Type.getType(method.getDeclaringClass()), method, signature);
    }

    public MethodInstance(Type owner, java.lang.reflect.Method method) {
        this(owner, method, getSignature(method));
    }

    public MethodInstance(Type owner, java.lang.reflect.Method method, String signature) {
        this(owner, TypeUtil.getModifiersToAccess(method.getModifiers()), Type.getType(method.getReturnType()), method.getName(), signature, TypeUtil.getClassesToTypes(method.getParameterTypes()));
    }

    public MethodInstance(Type owner, MethodInstance superMethod) {
        this(owner, superMethod.getAccess(), superMethod.getName(), superMethod.getSignature(), superMethod.getDescriptor());
    }

    public MethodInstance(Class<?> owner, int access, Class<?> returnType, String name, String signature, Class<?>... parameters) {
        this(owner != null ? Type.getType(owner) : null, access, Type.getType(returnType), name, signature, TypeUtil.getClassesToTypes(parameters));
    }

    public MethodInstance(Type owner, int access, String name, String signature, String desc) {
        this(owner, access, new Method(name, desc), signature);
    }

    public MethodInstance(Type owner, int access, Method method, String signature) {
        this.owner = owner;
        this.access = access;
        this.method = method;
        this.signature = signature;
    }

    public MethodInstance(Type owner, int access, Type returnType, String name, String signature, Type... parameters) {
        super();
        this.owner = owner;
        this.access = access;
        this.signature = signature;
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.getClassName()).append(' ');
        sb.append(name).append(" (");
        for (int a = 0, size = parameters.length; a < size; a++) {
            if (a > 0) {
                sb.append(", ");
            }
            sb.append(parameters[a].getClassName());
        }
        sb.append(')');
        method = Method.getMethod(sb.toString());
    }

    public MethodInstance deriveOwner() {
        return new MethodInstance(BytecodeBehaviorState.getState().getNewType(), this);
    }

    public MethodInstance deriveAccess(int access) {
        return new MethodInstance(getOwner(), access, getReturnType(), getName(), getSignature(), getParameters());
    }

    public MethodInstance deriveName(String methodName) {
        return new MethodInstance(getOwner(), getAccess(), getReturnType(), methodName, getSignature(), getParameters());
    }

    public Type getOwner() {
        return owner;
    }

    public int getAccess() {
        return access;
    }

    public Method getMethod() {
        return method;
    }

    public String getSignature() {
        return signature;
    }

    public String getSignatureFromParameterType(int parameterIndex) {
        return FieldInstance.getSignatureFromParameterType(getSignature(), parameterIndex);
    }

    public String getSignatureFromReturnType() {
        return FieldInstance.getSignatureFromReturnType(getSignature());
    }

    public Type getReturnType() {
        return method.getReturnType();
    }

    public Type[] getParameters() {
        return method.getArgumentTypes();
    }

    public String getName() {
        return method.getName();
    }

    public String getDescriptor() {
        return method.getDescriptor();
    }

    public boolean equalsSignature(MethodInstance method) {
        return getName().equals(method.getName()) && Arrays.equals(getParameters(), method.getParameters());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if ((access & Opcodes.ACC_PUBLIC) != 0) {
            sb.append("public ");
        } else if ((access & Opcodes.ACC_PROTECTED) != 0) {
            sb.append("protected ");
        } else if ((access & Opcodes.ACC_PRIVATE) != 0) {
            sb.append("private ");
        } else {
            throw new IllegalStateException("No visibility for method defined: " + method);
        }
        if ((access & Opcodes.ACC_STATIC) != 0) {
            sb.append("static ");
        }
        if ((access & Opcodes.ACC_FINAL) != 0) {
            sb.append("final ");
        }
        sb.append(method.getReturnType().getClassName()).append(' ');
        if (owner != null) {
            sb.append(owner.getClassName()).append('.');
        }
        sb.append(method.getName()).append('(');
        Type[] parameters = method.getArgumentTypes();
        for (int a = 0, size = parameters.length; a < size; a++) {
            if (a > 0) {
                sb.append(',');
            }
            sb.append(parameters[a].getClassName());
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodInstance)) {
            return false;
        }
        MethodInstance other = (MethodInstance) obj;
        return getOwner().equals(other.getOwner()) && getMethod().equals(other.getMethod());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ getOwner().hashCode() ^ getMethod().hashCode();
    }
}
