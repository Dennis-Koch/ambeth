package com.koch.ambeth.cache.bytecode.visitor;

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
import com.koch.ambeth.bytecode.FieldInstance;
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.ioc.typeinfo.FieldPropertyInfo;
import com.koch.ambeth.ioc.typeinfo.MethodPropertyInfo;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.mixin.EmbeddedMemberMixin;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.EmbeddedMember;
import com.koch.ambeth.service.metadata.IEmbeddedMember;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;
import org.objectweb.asm.ClassVisitor;

import java.util.Set;

public class InitializeEmbeddedMemberVisitor extends ClassGenerator {
    public static final Class<?> templateType = EmbeddedMemberMixin.class;

    public static final String templatePropertyName = "__" + templateType.getSimpleName();

    protected static final MethodInstance template_m_createEmbeddedObject =
            new MethodInstance(null, templateType, Object.class, "createEmbeddedObject", Class.class, Class.class, Object.class, String.class);

    public static PropertyInstance getEmbeddedMemberTemplatePI(ClassGenerator cv) {
        var bean = getState().getBeanContext().getService(templateType);
        var pi = getState().getProperty(templatePropertyName, bean.getClass());
        if (pi != null) {
            return pi;
        }
        return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
    }

    public static boolean isEmbeddedMember(IEntityMetaData metaData, String name) {
        var nameSplit = EmbeddedMember.split(name);
        for (var member : metaData.getPrimitiveMembers()) {
            if (!(member instanceof IEmbeddedMember)) {
                continue;
            }
            if (((IEmbeddedMember) member).getMemberPathToken()[0].equals(nameSplit[0])) {
                return true;
            }
        }
        for (var member : metaData.getRelationMembers()) {
            if (!(member instanceof IEmbeddedMember)) {
                continue;
            }
            if (((IEmbeddedMember) member).getMemberPathToken()[0].equals(nameSplit[0])) {
                return true;
            }
        }
        return false;
    }

    protected IPropertyInfoProvider propertyInfoProvider;

    protected IEntityMetaData metaData;

    protected String memberPath;

    protected String[] memberPathSplit;

    public InitializeEmbeddedMemberVisitor(ClassVisitor cv, IEntityMetaData metaData, String memberPath, IPropertyInfoProvider propertyInfoProvider) {
        super(cv);
        this.metaData = metaData;
        this.memberPath = memberPath;
        memberPathSplit = memberPath != null ? EmbeddedMember.split(memberPath) : null;
        this.propertyInfoProvider = propertyInfoProvider;
    }

    @Override
    public void visitEnd() {
        PropertyInstance p_embeddedMemberTemplate = getEmbeddedMemberTemplatePI(this);
        implementConstructor(p_embeddedMemberTemplate);

        super.visitEnd();
    }

    protected void implementConstructor(PropertyInstance p_embeddedMemberTemplate) {
        var alreadyHandledFirstMembers = new HashSet<Member>();

        var scripts = new ArrayList<Script>();

        {
            var script = handleMember(p_embeddedMemberTemplate, metaData.getIdMember(), alreadyHandledFirstMembers);
            if (script != null) {
                scripts.add(script);
            }
        }
        for (var member : metaData.getPrimitiveMembers()) {
            var script = handleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
            if (script != null) {
                scripts.add(script);
            }
        }
        for (var member : metaData.getRelationMembers()) {
            var script = handleMember(p_embeddedMemberTemplate, member, alreadyHandledFirstMembers);
            if (script != null) {
                scripts.add(script);
            }
        }
        if (scripts.isEmpty()) {
            return;
        }
        overrideConstructors((cv, superConstructor) -> {
            var mv = cv.visitMethod(superConstructor);
            mv.loadThis();
            mv.loadArgs();
            mv.invokeSuperOfCurrentMethod();

            for (var script : scripts) {
                script.execute(mv);
            }
            mv.returnValue();
            mv.endMethod();
        });
    }

    protected Script handleMember(PropertyInstance p_embeddedMemberTemplate, Member member, Set<Member> alreadyHandledFirstMembers) {
        if (member instanceof CompositeIdMember) {
            var members = ((CompositeIdMember) member).getMembers();
            Script aggregatedScript = null;
            for (int a = 0, size = members.length; a < size; a++) {
                var script = handleMember(p_embeddedMemberTemplate, members[a], alreadyHandledFirstMembers);
                if (script == null) {
                    continue;
                }
                if (aggregatedScript == null) {
                    aggregatedScript = script;
                    continue;
                }
                var oldAggregatedScript = aggregatedScript;
                aggregatedScript = mg -> {
                    oldAggregatedScript.execute(mg);
                    script.execute(mg);
                };
            }
            return aggregatedScript;
        }
        if (!(member instanceof IEmbeddedMember)) {
            return null;
        }
        var memberPath = ((IEmbeddedMember) member).getMemberPath();
        Member firstMember;
        if (memberPathSplit != null) {
            if (memberPath.length < memberPathSplit.length) {
                // nothing to do in this case. This member has nothing to do with our current scope
                return null;
            }
            for (int a = 0, size = memberPathSplit.length; a < size; a++) {
                if (!memberPathSplit[a].equals(memberPath[a].getName())) {
                    // nothing to do in this case. This member has nothing to do with our current scope
                    return null;
                }
            }
            if (memberPath.length > memberPathSplit.length) {
                firstMember = memberPath[memberPathSplit.length];
            } else {
                // nothing to do in this case. This is a leaf member
                return null;
            }
        } else {
            firstMember = memberPath[0];
        }
        if (!alreadyHandledFirstMembers.add(firstMember)) {
            return null;
        }
        return createEmbeddedObjectInstance(p_embeddedMemberTemplate, firstMember, this.memberPath != null ? this.memberPath + "." + firstMember.getName() : firstMember.getName());
    }

    protected Script createEmbeddedObjectInstance(final PropertyInstance p_embeddedMemberTemplate, final Member firstMember, final String memberPath) {
        var property = PropertyInstance.findByTemplate(firstMember.getName(), firstMember.getRealType(), false);
        var p_rootEntity = memberPathSplit == null ? null : EmbeddedTypeVisitor.getRootEntityProperty(this);

        return mg2 -> {
            mg2.callThisSetter(property, mg -> {
                // Object p_embeddedMemberTemplate.createEmbeddedObject(Class<?> embeddedType, Class<?> entityType, Object parentObject, String memberPath)
                mg.callThisGetter(p_embeddedMemberTemplate);

                mg.push(firstMember.getRealType()); // embeddedType

                if (p_rootEntity != null) {
                    mg.callThisGetter(p_rootEntity);
                    mg.checkCast(EntityMetaDataHolderVisitor.m_template_getEntityMetaData.getOwner());
                    mg.invokeInterface(EntityMetaDataHolderVisitor.m_template_getEntityMetaData);
                } else {
                    mg.callThisGetter(EntityMetaDataHolderVisitor.m_template_getEntityMetaData);
                }
                mg.invokeInterface(new MethodInstance(null, IEntityMetaData.class, Class.class, "getEnhancedType"));
                mg.loadThis(); // parentObject
                mg.push(memberPath);

                mg.invokeVirtual(template_m_createEmbeddedObject);
                mg.checkCast(firstMember.getRealType());
            });
        };
    }

    protected void invokeGetProperty(MethodGenerator mv, IPropertyInfo property) {
        if (property instanceof MethodPropertyInfo) {
            var method = ((MethodPropertyInfo) property).getGetter();
            mv.invokeVirtual(new MethodInstance(method));
        } else {
            var field = ((FieldPropertyInfo) property).getBackingField();
            mv.getField(new FieldInstance(field));
        }
    }

    protected void invokeSetProperty(MethodGenerator mv, IPropertyInfo property) {
        if (property instanceof MethodPropertyInfo) {
            var method = ((MethodPropertyInfo) property).getSetter();
            mv.invokeVirtual(new MethodInstance(method));
        } else {
            var field = ((FieldPropertyInfo) property).getBackingField();
            mv.putField(new FieldInstance(field));
        }
    }
}
