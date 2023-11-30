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
import com.koch.ambeth.bytecode.MethodGenerator;
import com.koch.ambeth.bytecode.MethodInstance;
import com.koch.ambeth.bytecode.PropertyInstance;
import com.koch.ambeth.bytecode.Script;
import com.koch.ambeth.bytecode.visitor.InterfaceAdder;
import com.koch.ambeth.merge.mixin.ObjRefMixin;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IPrintable;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.Constructor;

public class ObjRefVisitor extends ClassGenerator {
    public static final Class<?> templateType = ObjRefMixin.class;

    protected static final String templatePropertyName = templateType.getSimpleName();

    private static final ConstructorInstance c_stringBuilder;

    private static final PropertyInstance template_p_realType = PropertyInstance.findByTemplate(IObjRef.class, "RealType", Class.class, false);

    private static final PropertyInstance template_p_idIndex = PropertyInstance.findByTemplate(IObjRef.class, "IdNameIndex", byte.class, false);

    private static final PropertyInstance template_p_id = PropertyInstance.findByTemplate(IObjRef.class, "Id", Object.class, false);

    private static final PropertyInstance template_p_version = PropertyInstance.findByTemplate(IObjRef.class, "Version", Object.class, false);

    private static final MethodInstance template_m_equals = new MethodInstance(null, Object.class, boolean.class, "equals", Object.class);

    private static final MethodInstance template_m_hashCode = new MethodInstance(null, Object.class, int.class, "hashCode");

    private static final MethodInstance template_m_toString = new MethodInstance(null, Object.class, String.class, "toString");

    private static final MethodInstance template_m_toStringSb = new MethodInstance(null, IPrintable.class, void.class, "toString", StringBuilder.class);

    private static final MethodInstance m_objRef_equals = new MethodInstance(null, templateType, boolean.class, "objRefEquals", IObjRef.class, Object.class);

    private static final MethodInstance m_objRef_hashCode = new MethodInstance(null, templateType, int.class, "objRefHashCode", IObjRef.class);

    private static final MethodInstance m_objRef_toStringSb = new MethodInstance(null, templateType, void.class, "objRefToString", IObjRef.class, StringBuilder.class);

    static {
        try {
            c_stringBuilder = new ConstructorInstance(StringBuilder.class.getDeclaredConstructor());
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    public static PropertyInstance getObjRefTemplatePI(ClassGenerator cv) {
        Object bean = getState().getBeanContext().getService(templateType);
        PropertyInstance pi = getState().getProperty(templatePropertyName, bean.getClass());
        if (pi != null) {
            return pi;
        }
        return cv.implementAssignedReadonlyProperty(templatePropertyName, bean);
    }

    public static PropertyInstance getConversionHelperPI(ClassGenerator cv) {
        Object bean = getState().getBeanContext().getService(IConversionHelper.class);
        PropertyInstance pi = getState().getProperty("ConversionHelper", bean.getClass());
        if (pi != null) {
            return pi;
        }
        return cv.implementAssignedReadonlyProperty("ConversionHelper", bean);
    }

    protected final IEntityMetaData metaData;

    protected final int idIndex;

    public ObjRefVisitor(ClassVisitor cv, IEntityMetaData metaData, int idIndex) {
        super(new InterfaceAdder(cv, IObjRef.class));
        this.metaData = metaData;
        this.idIndex = idIndex;
    }

    @Override
    public void visitEnd() {
        implementRealType();
        implementIdIndex();
        PropertyInstance p_id = implementId();
        PropertyInstance p_version = implementVersion();
        implementDefaultConstructor();
        implementIdVersionConstructor(p_id, p_version);
        implementToString();
        implementEquals();
        implementHashCode();

        super.visitEnd();
    }

    protected void implementDefaultConstructor() {
        MethodGenerator mv = visitMethod(ConstructorInstance.defaultConstructor);
        mv.loadThis();
        mv.invokeSuperOfCurrentMethod();
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementIdVersionConstructor(final PropertyInstance p_id, final PropertyInstance p_version) {
        ConstructorInstance ci_super;
        try {
            Constructor<?> superConstructor = getState().getCurrentType().getDeclaredConstructor();
            ci_super = new ConstructorInstance(superConstructor);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
        MethodGenerator mv = visitMethod(new ConstructorInstance(Opcodes.ACC_PUBLIC, null, Object.class, Object.class));
        mv.loadThis();
        mv.invokeConstructor(ci_super);
        mv.callThisSetter(p_id, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.loadArg(0);
            }
        });
        mv.callThisSetter(p_version, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.loadArg(1);
            }
        });
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementRealType() {
        implementProperty(template_p_realType, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.push(metaData.getEntityType());
                mg.returnValue();
            }
        }, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.throwException(Type.getType(UnsupportedOperationException.class), "Property is read-only");
                mg.returnValue();
            }
        });
    }

    protected void implementIdIndex() {

        implementProperty(template_p_idIndex, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.push(idIndex);
                mg.returnValue();
            }
        }, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.throwException(Type.getType(UnsupportedOperationException.class), "Property is read-only");
                mg.returnValue();
            }
        });
    }

    protected PropertyInstance implementPotentialPrimitive(PropertyInstance property, final Member member) {
        if (member == null) {
            return implementProperty(property, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.pushNull();
                    mg.returnValue();
                }
            }, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    Label l_isNull = mg.newLabel();

                    mg.loadArg(0);
                    mg.ifNull(l_isNull);
                    mg.throwException(Type.getType(UnsupportedOperationException.class), "Property is read-only");
                    mg.mark(l_isNull);
                    mg.returnValue();
                }
            });
        }
        final PropertyInstance p_conversionHelper = getConversionHelperPI(this);

        final MethodInstance m_convertValueToType = new MethodInstance(ReflectUtil.getDeclaredMethod(false, IConversionHelper.class, Object.class, "convertValueToType", Class.class, Object.class));

        final Type type = Type.getType(member.getRealType());
        final FieldInstance field = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "f_" + property.getName(), null, type));
        return implementProperty(property, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                if (member.getRealType().isPrimitive()) {
                    Label l_isNull = mg.newLabel();
                    int loc_value = mg.newLocal(field.getType());

                    mg.getThisField(field);
                    mg.storeLocal(loc_value);

                    mg.loadLocal(loc_value);
                    mg.ifZCmp(field.getType(), GeneratorAdapter.EQ, l_isNull);

                    mg.loadLocal(loc_value);
                    mg.box(type);
                    mg.returnValue();

                    mg.mark(l_isNull);
                    mg.pushNull();
                    mg.returnValue();
                } else {
                    mg.getThisField(field);
                    mg.returnValue();
                }
            }
        }, new Script() {
            @Override
            public void execute(MethodGenerator mg) {
                mg.putThisField(field, new Script() {
                    @Override
                    public void execute(MethodGenerator mg) {
                        Label l_isNull = mg.newLabel();
                        Label l_finish = mg.newLabel();

                        mg.loadArg(0);
                        mg.ifNull(l_isNull);

                        mg.callThisGetter(p_conversionHelper);
                        mg.push(type);
                        mg.loadArg(0);
                        mg.invokeVirtual(m_convertValueToType);

                        mg.unbox(type);
                        mg.goTo(l_finish);

                        mg.mark(l_isNull);
                        mg.pushNullOrZero(field.getType());

                        mg.mark(l_finish);
                    }
                });
                mg.returnValue();
            }
        });
    }

    protected PropertyInstance implementId() {
        return implementPotentialPrimitive(template_p_id, metaData.getIdMemberByIdIndex((byte) idIndex));
    }

    protected PropertyInstance implementVersion() {
        return implementPotentialPrimitive(template_p_version, metaData.getVersionMember());
    }

    protected void implementToString() {
        PropertyInstance p_objRefTemplate = getObjRefTemplatePI(this);

        MethodInstance methodSb;
        {
            methodSb = MethodInstance.findByTemplate(template_m_toStringSb, true);
            if (methodSb == null || (methodSb.getAccess() & Opcodes.ACC_ABSTRACT) != 0) {
                MethodGenerator mg = visitMethod(template_m_toStringSb);
                mg.callThisGetter(p_objRefTemplate);
                mg.loadThis();
                mg.loadArgs();
                mg.invokeVirtual(m_objRef_toStringSb);
                mg.returnValue();
                mg.endMethod();
                methodSb = mg.getMethod();
            }
        }
        {
            MethodInstance method = MethodInstance.findByTemplate(template_m_toString, true);
            if (method == null || Type.getType(Object.class).equals(method.getOwner()) || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0) {
                MethodGenerator mg = visitMethod(template_m_toString);
                int loc_sb = mg.newLocal(StringBuilder.class);
                mg.loadThis();
                mg.newInstance(c_stringBuilder, null);
                mg.storeLocal(loc_sb);
                mg.loadLocal(loc_sb);
                mg.invokeVirtual(methodSb);
                mg.loadLocal(loc_sb);
                mg.invokeVirtual(new MethodInstance(null, StringBuilder.class, String.class, "toString"));
                mg.returnValue();
                mg.endMethod();
            }
        }
    }

    protected void implementEquals() {
        PropertyInstance p_objRefTemplate = getObjRefTemplatePI(this);

        MethodInstance method = MethodInstance.findByTemplate(template_m_equals, true);
        if (method == null || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0) {
            MethodGenerator mg = visitMethod(template_m_equals);
            mg.callThisGetter(p_objRefTemplate);
            mg.loadThis();
            mg.loadArgs();
            mg.invokeVirtual(m_objRef_equals);
            mg.returnValue();
            mg.endMethod();
        }
    }

    protected void implementHashCode() {
        PropertyInstance p_objRefTemplate = getObjRefTemplatePI(this);

        MethodInstance method = MethodInstance.findByTemplate(template_m_hashCode, true);
        if (method == null || (method.getAccess() & Opcodes.ACC_ABSTRACT) != 0) {
            MethodGenerator mg = visitMethod(template_m_hashCode);
            mg.callThisGetter(p_objRefTemplate);
            mg.loadThis();
            mg.loadArgs();
            mg.invokeVirtual(m_objRef_hashCode);
            mg.returnValue();
            mg.endMethod();
        }
    }
}
