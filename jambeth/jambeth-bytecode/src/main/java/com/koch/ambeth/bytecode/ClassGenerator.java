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
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.annotation.FireTargetOnPropertyChange;
import com.koch.ambeth.util.annotation.FireThisOnPropertyChange;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedSupplier;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ClassGenerator extends ClassVisitor {
    public static final Class<FireThisOnPropertyChange> c_fireThisOPC = FireThisOnPropertyChange.class;

    public static final Class<FireTargetOnPropertyChange> c_fireTargetOPC = FireTargetOnPropertyChange.class;

    private static final ThreadLocal<String> propertyNameTL = new ThreadLocal<>();

    private static final ConstructorInstance c_obj;

    static {
        try {
            c_obj = new ConstructorInstance(Object.class.getConstructor());
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    public static String getNameFromPropertyContext() {
        return propertyNameTL.get();
    }

    public static <R> R setPropertyContext(String propertyName, CheckedSupplier<R> runnable) {
        var oldValue = propertyNameTL.get();
        propertyNameTL.set(propertyName);
        try {
            return CheckedSupplier.invoke(runnable);
        } finally {
            if (oldValue == null) {
                propertyNameTL.remove();
            } else {
                propertyNameTL.set(oldValue);
            }
        }
    }

    protected static IBytecodeBehaviorState getState() {
        return BytecodeBehaviorState.getState();
    }

    public ClassGenerator(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    public FieldInstance implementStaticAssignedField(String staticFieldName, Object fieldValue) {
        ParamChecker.assertParamNotNull(fieldValue, "fieldValue");
        var fieldType = fieldValue.getClass();
        if (fieldValue instanceof IValueResolveDelegate) {
            fieldType = ((IValueResolveDelegate) fieldValue).getValueType();
        }
        return implementStaticAssignedField(staticFieldName, fieldType, fieldValue);
    }

    public FieldInstance implementStaticAssignedField(String staticFieldName, Class<?> fieldType, Object fieldValue) {
        var field = new FieldInstance(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, staticFieldName, null, Type.getType(fieldType));
        field = implementField(field, null);
        if (fieldValue != null) {
            IValueResolveDelegate vrd = null;
            if (fieldValue instanceof IValueResolveDelegate) {
                vrd = (IValueResolveDelegate) fieldValue;
            } else {
                vrd = new NoOpValueResolveDelegate(fieldValue);
            }
            ((BytecodeBehaviorState) getState()).queueFieldInitialization(field.getName(), vrd);
        }
        return getState().getAlreadyImplementedField(field.getName());
    }

    public PropertyInstance implementAssignedReadonlyProperty(String propertyName, Object fieldValue) {
        ParamChecker.assertParamNotNull(fieldValue, "fieldValue");
        var fieldName = propertyName.startsWith("__") ? "sf" + propertyName : "sf__" + propertyName;
        var field = implementStaticAssignedField(fieldName, fieldValue);
        var getter = new MethodInstance(getState().getNewType(), Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, field.getType(), "get" + propertyName, null);
        getter = implementGetter(getter, field);
        var property = getState().getProperty(propertyName, field.getType());
        if (property == null) {
            throw new IllegalStateException("Should never happen");
        }
        return property;
    }

    public FieldInstance implementField(FieldInstance field) {
        return implementField(field, null);
    }

    public FieldInstance implementField(FieldInstance field, FScript script) {
        var fv = visitField(field.getAccess(), field.getName(), field.getType().getDescriptor(), field.getSignature(), null);
        if (script != null) {
            script.execute(fv);
        }
        fv.visitEnd();
        return getState().getAlreadyImplementedField(field.getName());
    }

    @Override
    public MethodGenerator visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        var state = BytecodeBehaviorState.getState();
        var mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (mv instanceof MethodGenerator) {
            return (MethodGenerator) mv;
        }
        return new MethodGenerator(this, mv, new MethodInstance(state.getNewType(), access, name, signature, desc));
    }

    public MethodGenerator visitMethod(MethodInstance method) {
        var owner = BytecodeBehaviorState.getState().getNewType();
        method = new MethodInstance(owner, method);
        var mv = super.visitMethod(method.getAccess(), method.getName(), method.getDescriptor(), method.getSignature(), null);
        return new MethodGenerator(this, mv, method);
    }

    public MethodInstance implementSetter(MethodInstance method, FieldInstance field) {
        var mg = visitMethod(method);
        mg.putThisField(field, currMg -> currMg.loadArg(0));
        mg.returnVoidOrThis();
        mg.endMethod();
        return MethodInstance.findByTemplate(method, false);
    }

    public PropertyInstance implementSetter(PropertyInstance property, FieldInstance field) {
        var setter = property.getSetter();
        if (setter == null) {
            setter = new MethodInstance(getState().getNewType(), Opcodes.ACC_PUBLIC, Type.VOID_TYPE, "set" + property.getName(), property.getSignature(), property.getPropertyType());
        }
        implementSetter(setter, field);
        return PropertyInstance.findByTemplate(property, false);
    }

    public MethodInstance implementGetter(MethodInstance method, FieldInstance field) {
        return implementGetter(method, field, null);
    }

    public MethodInstance implementGetter(MethodInstance method, FieldInstance field, Script script) {
        var mg = visitMethod(method);
        mg.getThisField(field);
        mg.returnValue();
        mg.endMethod();
        if (script != null) {
            script.execute(mg);
        }
        return MethodInstance.findByTemplate(method, false);
    }

    public PropertyInstance implementGetter(PropertyInstance property, FieldInstance field) {
        var getter = property.getGetter();
        if (getter == null) {
            getter = new MethodInstance(getState().getNewType(), Opcodes.ACC_PUBLIC, property.getPropertyType(), "get" + property.getName(), property.getSignature());
        }
        getter = implementGetter(getter, field);
        return PropertyInstance.findByTemplate(property, false);
    }

    public PropertyInstance implementLazyInitProperty(PropertyInstance property, Script script, String... fireThisOnPropertyNames) {
        var field = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "f_" + property.getName(), property.getSignature(), property.getPropertyType()));
        var mv = visitMethod(property.getGetter());
        var returnInstance = mv.newLabel();
        mv.getThisField(field);
        mv.ifNonNull(returnInstance);
        mv.putThisField(field, script);
        mv.mark(returnInstance);
        mv.getThisField(field);
        mv.returnValue();
        mv.endMethod();
        return fireThisOnPropertyChange(property, fireThisOnPropertyNames);
    }

    public PropertyInstance implementProperty(PropertyInstance property, Script getterScript, Script setterScript) {
        if (getterScript != null) {
            var mv = visitMethod(property.getGetter());
            getterScript.execute(mv);
            mv.endMethod();
        }
        if (setterScript != null) {
            var mv = visitMethod(property.getSetter());
            setterScript.execute(mv);
            mv.endMethod();
        }
        return PropertyInstance.findByTemplate(property, false);
    }

    public PropertyInstance fireThisOnPropertyChange(PropertyInstance property, String... propertyNames) {
        property = getState().getProperty(property.getName(), property.getPropertyType());
        for (var propertyName : propertyNames) {
            property.addAnnotation(c_fireThisOPC, propertyName);
        }
        return property;
    }

    public void overrideConstructors(IOverrideConstructorDelegate overrideConstructorDelegate) {
        if (getState().getCurrentType().isInterface()) {
            overrideConstructorDelegate.invoke(this, c_obj);
            return;
        }
        var constructors = getState().getCurrentType().getDeclaredConstructors();
        for (var superConstructor : constructors) {
            overrideConstructorDelegate.invoke(this, new ConstructorInstance(superConstructor));
        }
    }

    public MethodGenerator startOverrideWithSuperCall(MethodInstance superMethod) {
        var state = BytecodeBehaviorState.getState();

        var superType = Type.getType(state.getCurrentType());
        if (!superType.equals(superMethod.getOwner())) {
            throw new IllegalArgumentException("Not a method of " + state.getCurrentType() + ": " + superMethod);
        }

        var overridingMethod = new MethodInstance(state.getNewType(), superMethod);

        var mg = visitMethod(overridingMethod);

        mg.loadThis();
        mg.loadArgs();
        mg.invokeSuper(superMethod);

        return mg;
    }

    public MethodInstance implementSwitchByIndex(MethodInstance method, String exceptionMessageOnIllegalIndex, int indexSize, ScriptWithIndex script) {
        var mv = visitMethod(method);

        if (indexSize == 0) {
            mv.throwException(Type.getType(IllegalArgumentException.class), exceptionMessageOnIllegalIndex);
            mv.pushNull();
            mv.returnValue();
            mv.endMethod();
            return mv.getMethod();
        }

        var l_default = mv.newLabel();
        var l_fields = new Label[indexSize];
        for (int index = 0, size = indexSize; index < size; index++) {
            l_fields[index] = mv.newLabel();
        }

        mv.loadArg(0);
        mv.visitTableSwitchInsn(0, l_fields.length - 1, l_default, l_fields);

        for (int index = 0, size = l_fields.length; index < size; index++) {
            mv.mark(l_fields[index]);

            script.execute(mv, index);
        }
        mv.mark(l_default);

        mv.throwException(Type.getType(IllegalArgumentException.class), "Given relationIndex not known");
        mv.pushNull();
        mv.returnValue();
        mv.endMethod();
        return mv.getMethod();
    }
}
