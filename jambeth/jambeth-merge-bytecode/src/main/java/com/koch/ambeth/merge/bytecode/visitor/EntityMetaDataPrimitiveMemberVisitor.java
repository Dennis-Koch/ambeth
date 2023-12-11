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
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.service.metadata.IPrimitiveMemberWrite;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class EntityMetaDataPrimitiveMemberVisitor extends ClassGenerator {
    protected static final MethodInstance template_m_isTechnicalMember = new MethodInstance(null, PrimitiveMember.class, boolean.class, "isTechnicalMember");

    protected static final MethodInstance template_m_setTechnicalMember = new MethodInstance(null, IPrimitiveMemberWrite.class, void.class, "setTechnicalMember", boolean.class);

    protected static final MethodInstance template_m_isTransient = new MethodInstance(null, PrimitiveMember.class, boolean.class, "isTransient");

    protected static final MethodInstance template_m_setTransient = new MethodInstance(null, IPrimitiveMemberWrite.class, void.class, "setTransient", boolean.class);

    protected static final MethodInstance template_m_getDefinedBy = new MethodInstance(null, PrimitiveMember.class, PrimitiveMember.class, "getDefinedBy");

    protected static final MethodInstance template_m_setDefinedBy = new MethodInstance(null, IPrimitiveMemberWrite.class, void.class, "setDefinedBy", PrimitiveMember.class);

    protected final Class<?> entityType;

    protected final String memberName;

    protected IPropertyInfo[] propertyPath;

    public EntityMetaDataPrimitiveMemberVisitor(ClassVisitor cv, Class<?> entityType, String memberName, IPropertyInfo[] propertyPath) {
        super(new InterfaceAdder(cv, IPrimitiveMemberWrite.class));
        this.entityType = entityType;
        this.memberName = memberName;
        this.propertyPath = propertyPath;
    }

    @Override
    public void visitEnd() {
        implementTechnicalMember();
        implementTransient();
        implementDefinedBy();
        super.visitEnd();
    }

    protected void implementTechnicalMember() {
        var f_technicalMember = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__technicalMember", null, boolean.class));

        implementGetter(template_m_isTechnicalMember, f_technicalMember);
        implementSetter(template_m_setTechnicalMember, f_technicalMember);
    }

    protected void implementTransient() {
        var f_transient = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__transient", null, boolean.class));

        implementGetter(template_m_isTransient, f_transient);
        implementSetter(template_m_setTransient, f_transient);
    }

    protected void implementDefinedBy() {
        var f_definedBy = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "__definedBy", null, PrimitiveMember.class));

        implementGetter(template_m_getDefinedBy, f_definedBy);
        implementSetter(template_m_setDefinedBy, f_definedBy);
    }
}
