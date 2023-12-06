package com.koch.ambeth.merge.bytecode.visitor;

/*-
 * #%L
 * jambeth-cache-bytecode
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
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.merge.mixin.ObjRefTypeMixin;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.merge.model.IObjRefType;
import org.objectweb.asm.ClassVisitor;

import java.util.List;

public class ObjRefTypeVisitor extends ClassGenerator {
    public static final Class<?> templateType = ObjRefTypeMixin.class;

    public static final String templatePropertyName = "__" + templateType.getSimpleName();

    public static final PropertyInstance t_p_ObjRef = PropertyInstance.findByTemplate(IObjRefType.class, "ObjRef", IObjRef.class, false);

    public static final MethodInstance t_m_getObjRefByName = new MethodInstance(null, IObjRefType.class, IObjRef.class, "getObjRef", String.class);

    public static final MethodInstance t_m_getAllObjRefs = new MethodInstance(null, IObjRefType.class, List.class, "getAllObjRefs");

    public static final MethodInstance m_getObjRef = new MethodInstance(null, templateType, IObjRef.class, "getObjRef", IObjRefType.class);

    public static final MethodInstance m_getObjRefByName = new MethodInstance(null, templateType, IObjRef.class, "getObjRef", IObjRefType.class, String.class);

    public static final MethodInstance m_getAllObjRefs = new MethodInstance(null, templateType, List.class, "getAllObjRefs", IObjRefType.class);

    public static PropertyInstance getObjRefTypeTemplateProperty(ClassGenerator cv) {
        Object bean = getState().getBeanContext().getService(templateType);
        PropertyInstance p_embeddedTypeTemplate = PropertyInstance.findByTemplate(templatePropertyName, bean.getClass(), true);
        if (p_embeddedTypeTemplate != null) {
            return p_embeddedTypeTemplate;
        }
        return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
    }

    public ObjRefTypeVisitor(ClassVisitor cv) {
        super(cv);
    }

    @Override
    public void visitEnd() {
        implementForwardProperty(t_p_ObjRef, m_getObjRef);
        implementForwardMethod(t_m_getObjRefByName, m_getObjRefByName);
        implementForwardMethod(t_m_getAllObjRefs, m_getAllObjRefs);
        super.visitEnd();
    }

    private void implementForwardProperty(PropertyInstance p_template, final MethodInstance mixinMethod) {
        PropertyInstance p_objRef = PropertyInstance.findByTemplate(p_template, true);
        if (p_objRef != null) {
            return;
        }
        final PropertyInstance p_objRefTypeTemplate = getObjRefTypeTemplateProperty(this);
        p_objRef = implementProperty(p_template, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.callThisGetter(p_objRefTypeTemplate);
                mg.loadThis();
                mg.loadArgs();
                mg.invokeVirtual(mixinMethod);
                mg.returnValue();
            }
        }, null);

        if (p_objRef == null) {
            throw new IllegalStateException("Must never happen");
        }
    }

    private void implementForwardMethod(MethodInstance m_template, final MethodInstance mixinMethod) {
        final PropertyInstance p_objRefTypeTemplate = getObjRefTypeTemplateProperty(this);
        MethodGenerator mg = visitMethod(m_template);
        mg.callThisGetter(p_objRefTypeTemplate);
        mg.loadThis();
        mg.loadArgs();
        mg.invokeVirtual(mixinMethod);
        mg.returnValue();
        mg.endMethod();
    }
}
