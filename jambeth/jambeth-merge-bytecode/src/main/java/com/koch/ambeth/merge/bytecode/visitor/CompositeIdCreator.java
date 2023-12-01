package com.koch.ambeth.merge.bytecode.visitor;

/*-
 * #%L
 * jambeth-merge-bytecode
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

import com.koch.ambeth.bytecode.ClassGenerator;
import com.koch.ambeth.bytecode.ConstructorInstance;
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.IValueResolveDelegate;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.behavior.BytecodeBehaviorState;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.bytecode.compositeid.CompositeIdEnhancementHint;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.mixin.CompositeIdMixin;
import com.koch.ambeth.repackaged.com.esotericsoftware.reflectasm.FieldAccess;
import com.koch.ambeth.service.typeinfo.FieldInfoItemASM;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.typeinfo.ITypeInfoItem;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CompositeIdCreator extends ClassGenerator {
    public static final Class<?> templateType = CompositeIdMixin.class;
    public static final MethodInstance m_equalsCompositeId = new MethodInstance(null, templateType, boolean.class, "equalsCompositeId", ITypeInfoItem[].class, Object.class, Object.class);
    public static final MethodInstance m_hashCodeCompositeId = new MethodInstance(null, templateType, int.class, "hashCodeCompositeId", ITypeInfoItem[].class, Object.class);
    public static final MethodInstance m_toStringCompositeId = new MethodInstance(null, templateType, String.class, "toStringCompositeId", ITypeInfoItem[].class, Object.class);
    public static final MethodInstance m_toStringSbCompositeId = new MethodInstance(null, templateType, void.class, "toStringSbCompositeId", ITypeInfoItem[].class, Object.class, StringBuilder.class);
    protected static final String templatePropertyName = templateType.getSimpleName();

    public static PropertyInstance getCompositeIdTemplatePI(ClassGenerator cv) {
        var bean = getState().getBeanContext().getService(templateType);
        var pi = getState().getProperty(templatePropertyName, bean.getClass());
        if (pi != null) {
            return pi;
        }
        return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
    }

    protected static void implementDefaultDelegatingMethod(MethodGenerator mg, PropertyInstance p_compositeIdTemplate, PropertyInstance p_idMembers, MethodInstance delegatedMethod) {
        mg.callThisGetter(p_compositeIdTemplate);
        mg.callThisGetter(p_idMembers);
        mg.loadThis();
        mg.loadArgs();
        mg.invokeVirtual(delegatedMethod);
        mg.returnValue();
        mg.endMethod();
    }

    public CompositeIdCreator(ClassVisitor cv) {
        super(new InterfaceAdder(cv, Type.getInternalName(IPrintable.class)));
    }

    @SneakyThrows
    @Override
    public void visitEnd() {
        var context = BytecodeBehaviorState.getState().getContext(CompositeIdEnhancementHint.class);
        var idMembers = context.getIdMembers();

        var p_compositeIdTemplate = getCompositeIdTemplatePI(this);

        var constructorTypes = new Type[idMembers.length];
        var fields = new FieldInstance[idMembers.length];
        // order does matter here (to maintain field order for debugging purpose on later objects)
        for (int a = 0, size = idMembers.length; a < size; a++) {
            var member = idMembers[a];
            var fieldName = CompositeIdMember.filterEmbeddedFieldName(member.getName());
            constructorTypes[a] = Type.getType(member.getRealType());
            fields[a] = new FieldInstance(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, fieldName, null, constructorTypes[a]);
            implementField(fields[a], fv -> {
                var av = fv.visitAnnotation(Type.getDescriptor(Property.class), true);
                av.visitEnd();
            });
        }
        {
            var mg = visitMethod(new ConstructorInstance(Opcodes.ACC_PUBLIC, null, constructorTypes));
            mg.loadThis();
            mg.invokeOnExactOwner(new ConstructorInstance(Object.class.getConstructor()));
            // order does matter here
            for (int a = 0, size = fields.length; a < size; a++) {
                var index = a;
                mg.putThisField(fields[a], currMg -> currMg.loadArg(index));
            }
            mg.returnValue();
            mg.endMethod();
        }
        var p_idMembers = implementAssignedReadonlyProperty("IdMembers", new CompositeIdValueResolveDelegate(fields));

        {
            // Implement boolean Object.equals(Object)
            var mg = visitMethod(new MethodInstance(null, Object.class, boolean.class, "equals", Object.class));
            // public boolean CompositeIdTemplate.equalsCompositeId(ITypeInfoItem[] members, Object left,
            // Object right)
            implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_equalsCompositeId);
        }
        {
            // Implement int Object.hashCode()
            var mg = visitMethod(new MethodInstance(null, Object.class, int.class, "hashCode"));
            // public int CompositeIdTemplate.hashCodeCompositeId(ITypeInfoItem[] members, Object
            // compositeId)
            implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_hashCodeCompositeId);
        }
        {
            // Implement String Object.toString()
            var mg = visitMethod(new MethodInstance(null, Object.class, String.class, "toString"));
            // public int CompositeIdTemplate.toStringCompositeId(ITypeInfoItem[] members, Object
            // compositeId)
            implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_toStringCompositeId);
        }
        {
            // Implement void IPrintable.toString(StringBuilder)
            var mg = visitMethod(new MethodInstance(null, IPrintable.class, void.class, "toString", StringBuilder.class));
            // public int CompositeIdTemplate.toStringCompositeId(ITypeInfoItem[] members, Object
            // compositeId)
            implementDefaultDelegatingMethod(mg, p_compositeIdTemplate, p_idMembers, m_toStringSbCompositeId);
        }
        super.visitEnd();
    }

    public static class CompositeIdValueResolveDelegate implements IValueResolveDelegate {
        private final FieldInstance[] fields;

        public CompositeIdValueResolveDelegate(FieldInstance[] fields) {
            this.fields = fields;
        }

        @Override
        public Object invoke(String fieldName, Class<?> enhancedType) {
            var fieldAccess = FieldAccess.get(enhancedType);
            var members = new ITypeInfoItem[fields.length];
            for (int a = members.length; a-- > 0; ) {
                var field = ReflectUtil.getDeclaredFieldInHierarchy(enhancedType, fields[a].getName());
                members[a] = new FieldInfoItemASM(field[0], fieldAccess);
            }
            return members;
        }

        @Override
        public Class<?> getValueType() {
            return ITypeInfoItem[].class;
        }
    }
}
