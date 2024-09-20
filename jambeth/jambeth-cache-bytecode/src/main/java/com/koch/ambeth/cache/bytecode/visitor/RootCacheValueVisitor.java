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
import com.koch.ambeth.bytecode.ScriptWithIndex;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.collections.IListElem;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class RootCacheValueVisitor extends ClassGenerator {
    private static final Type objType = Type.getType(Object.class);

    private static final Type objRefArrayType = Type.getType(IObjRef[].class);

    public static final Object[][] OBJ_EMPTY_ARRAY_ARRAY = new Object[0][0];

    private static final FieldInstance sf_objArrayArray = new FieldInstance(ReflectUtil.getDeclaredField(RootCacheValueVisitor.class, "OBJ_EMPTY_ARRAY_ARRAY"));

    private static final FieldInstance sf_objRefArrayArray = new FieldInstance(ReflectUtil.getDeclaredField(IObjRef.class, "EMPTY_ARRAY_ARRAY"));

    public static final PropertyInstance template_p_listHandle = PropertyInstance.findByTemplate(RootCacheValue.class, "ListHandle", Object.class, false);

    public static final PropertyInstance template_p_next = PropertyInstance.findByTemplate(RootCacheValue.class, "Next", IListElem.class, false);

    public static final PropertyInstance template_p_prev = PropertyInstance.findByTemplate(RootCacheValue.class, "Prev", IListElem.class, false);

    protected final IEntityMetaData metaData;

    protected final boolean lruMode;

    public RootCacheValueVisitor(ClassVisitor cv, IEntityMetaData metaData, boolean lruMode) {
        super(cv);
        this.metaData = metaData;
        this.lruMode = lruMode;
    }

    @Override
    public void visitEnd() {
        implementListHandle();
        implementNext();
        implementPrev();
        implementGetEntityType();
        implementId();
        implementVersion();
        implementPrimitives();
        implementRelations();
        super.visitEnd();
    }

    protected void implementListHandle() {
        if (!lruMode) {
            implementProperty(template_p_listHandle, mg -> {
                mg.pushNull();
                mg.returnValue();
            }, mg -> {
                mg.throwException(Type.getType(UnsupportedOperationException.class), "LRU mode not active. This setter is not supported");
                mg.returnValue();
            });
            return;
        }
        var f_listHandle = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$" + template_p_listHandle.getName(), null, template_p_listHandle.getPropertyType()));

        implementProperty(template_p_listHandle, mgGet -> {
            /*
                return this.listHandle;
             */
            mgGet.getThisField(f_listHandle);
            mgGet.returnValue();
        }, mgSet -> {
            /*
                if (this.listHandle != null && listHandle != null) {
                    throw new UnsupportedOperationException();
                }
                this.listHandle = listHandle;
            */
            var l_callIsValid = mgSet.newLabel();

            mgSet.getThisField(f_listHandle);
            mgSet.ifNull(l_callIsValid);

            mgSet.loadArg(0);
            mgSet.ifNull(l_callIsValid);

            mgSet.throwException(Type.getType(UnsupportedOperationException.class), "list handle already set");

            mgSet.mark(l_callIsValid);

            mgSet.putThisField(f_listHandle, currMg -> currMg.loadArg(0));
            mgSet.returnValue();
        });
    }

    protected void implementNext() {
        if (!lruMode) {
            implementProperty(template_p_next, mg -> {
                mg.pushNull();
                mg.returnValue();
            }, mg -> {
                mg.throwException(Type.getType(UnsupportedOperationException.class), "LRU mode not active. This setter is not supported");
                mg.returnValue();
            });
            return;
        }
        var f_next = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$" + template_p_next.getName(), null, template_p_next.getPropertyType()));

        implementProperty(template_p_next, mg -> {
            mg.getThisField(f_next);
            mg.returnValue();
        }, mg -> {
            mg.putThisField(f_next, mg2 -> {
                mg2.loadArg(0);
            });
            mg.returnValue();
        });
    }

    protected void implementPrev() {
        if (!lruMode) {
            implementProperty(template_p_prev, mg -> {
                mg.pushNull();
                mg.returnValue();
            }, mg -> {
                mg.throwException(Type.getType(UnsupportedOperationException.class), "LRU mode not active. This setter is not supported");
                mg.returnValue();
            });
            return;
        }
        var f_prev = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, "$" + template_p_prev.getName(), null, template_p_prev.getPropertyType()));

        implementProperty(template_p_prev, mg -> {
            mg.getThisField(f_prev);
            mg.returnValue();
        }, mg -> {
            mg.putThisField(f_prev, mg2 -> {
                mg2.loadArg(0);
            });
            mg.returnValue();
        });
    }

    protected void implementGetEntityType() {
        var template_m_getEntityType = new MethodInstance(null, RootCacheValue.class, Class.class, "getEntityType");

        var mv = visitMethod(template_m_getEntityType);
        mv.push(Type.getType(metaData.getEntityType()));
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementId() {
        var template_m_getId = new MethodInstance(null, RootCacheValue.class, Object.class, "getId");
        var template_m_setId = new MethodInstance(null, RootCacheValue.class, void.class, "setId", Object.class);

        CacheMapEntryVisitor.implementNativeField(this, metaData.getIdMember(), template_m_getId, template_m_setId);
    }

    protected void implementVersion() {
        var template_m_getVersion = new MethodInstance(null, RootCacheValue.class, Object.class, "getVersion");
        var template_m_setVersion = new MethodInstance(null, RootCacheValue.class, void.class, "setVersion", Object.class);

        CacheMapEntryVisitor.implementNativeField(this, metaData.getVersionMember(), template_m_getVersion, template_m_setVersion);
    }

    protected void implementPrimitives() {
        var primitiveMembers = metaData.getPrimitiveMembers();
        var f_primitives = new FieldInstance[primitiveMembers.length];
        var f_nullFlags = new FieldInstance[primitiveMembers.length];
        var fieldType = new Class<?>[primitiveMembers.length];

        for (int primitiveIndex = 0, size = primitiveMembers.length; primitiveIndex < size; primitiveIndex++) {
            var member = primitiveMembers[primitiveIndex];
            var realType = member.getRealType();
            var nativeType = WrapperTypeSet.getUnwrappedType(realType);
            var isNullable = true;
            if (nativeType == null) {
                nativeType = realType;
                isNullable = false;
            }
            if (java.util.Date.class.isAssignableFrom(nativeType)) {
                nativeType = long.class;
                isNullable = true;
            }
            if (!nativeType.isPrimitive()) {
                nativeType = Object.class;
            }
            if (isNullable) {
                // field is a nullable numeric field. We need a flag field to handle true null case
                var f_nullFlag = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member) + "$isNull", null, boolean.class));
                f_nullFlags[primitiveIndex] = f_nullFlag;
            }
            fieldType[primitiveIndex] = nativeType;
            var f_primitive = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member), null, nativeType));
            f_primitives[primitiveIndex] = f_primitive;
        }
        implementGetPrimitives(primitiveMembers, f_primitives, f_nullFlags);
        implementSetPrimitives(primitiveMembers, f_primitives, f_nullFlags);
        implementGetPrimitive(primitiveMembers, f_primitives, f_nullFlags);
    }

    protected void implementGetPrimitive(Member[] primitiveMember, final FieldInstance[] f_primitives, final FieldInstance[] f_nullFlags) {
        var template_m_getPrimitive = new MethodInstance(null, RootCacheValue.class, Object.class, "getPrimitive", int.class);

        implementSwitchByIndex(template_m_getPrimitive, "Given primitiveIndex not known", f_primitives.length, new ScriptWithIndex() {
            @Override
            public void execute(MethodGenerator mg, int primitiveIndex) {
                var f_primitive = f_primitives[primitiveIndex];
                var f_nullFlag = f_nullFlags[primitiveIndex];

                Label l_fieldIsNull = null;

                if (f_nullFlag != null) {
                    l_fieldIsNull = mg.newLabel();
                    // only do something if the field is non-null
                    mg.getThisField(f_nullFlag);
                    mg.ifZCmp(GeneratorAdapter.NE, l_fieldIsNull);
                }
                mg.getThisField(f_primitive);
                mg.box(f_primitive.getType());
                mg.returnValue();

                if (f_nullFlag != null) {
                    mg.mark(l_fieldIsNull);
                    mg.pushNull();
                }
                mg.returnValue();
            }
        });
    }

    protected void implementGetPrimitives(Member[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags) {
        var template_m_getPrimitives = new MethodInstance(null, RootCacheValue.class, Object[].class, "getPrimitives");

        var mv = visitMethod(template_m_getPrimitives);

        if (f_primitives.length == 0) {
            mv.getField(sf_objArrayArray);
            mv.returnValue();
        }
        mv.push(f_primitives.length);
        mv.newArray(objType);

        for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++) {
            var f_primitive = f_primitives[primitiveIndex];
            var f_nullFlag = f_nullFlags[primitiveIndex];

            Label l_fieldIsNull = null;

            if (f_nullFlag != null) {
                l_fieldIsNull = mv.newLabel();
                // only do something if the field is non-null
                mv.getThisField(f_nullFlag);
                mv.ifZCmp(GeneratorAdapter.NE, l_fieldIsNull);
            }
            // duplicate array instance on stack
            mv.dup();

            mv.push(primitiveIndex);
            mv.getThisField(f_primitive);

            mv.valueOf(f_primitive.getType());
            mv.arrayStore(objType);

            if (f_nullFlag != null) {
                mv.mark(l_fieldIsNull);
            }
        }
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementSetPrimitives(Member[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags) {
        var template_m_setPrimitives = new MethodInstance(null, RootCacheValue.class, void.class, "setPrimitives", Object[].class);

        var mv = visitMethod(template_m_setPrimitives);
        var loc_item = mv.newLocal(objType);

        for (int primitiveIndex = 0, size = f_primitives.length; primitiveIndex < size; primitiveIndex++) {
            var f_primitive = f_primitives[primitiveIndex];
            var f_nullFlag = f_nullFlags[primitiveIndex];
            var member = primitiveMembers[primitiveIndex];
            var originalType = member.getRealType();

            var fPrimitiveIndex = primitiveIndex;

            var script_loadArrayValue = new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.loadArg(0);
                    mg.push(fPrimitiveIndex);
                    mg.arrayLoad(objType);
                }
            };

            var l_finish = mv.newLabel();

            if (f_nullFlag == null) {
                if (!originalType.isPrimitive()) {
                    mv.putThisField(f_primitive, script_loadArrayValue);
                    continue;
                }
                script_loadArrayValue.execute(mv);
                mv.storeLocal(loc_item);
                mv.loadLocal(loc_item);
                mv.ifNull(l_finish);

                mv.putThisField(f_primitive, new Script() {
                    @Override
                    public void execute(MethodGenerator mg) {
                        mg.loadLocal(loc_item);
                        mg.unbox(f_primitive.getType());
                    }
                });

                mv.mark(l_finish);
                continue;
            }
            var l_itemIsNull = mv.newLabel();

            script_loadArrayValue.execute(mv);
            mv.storeLocal(loc_item);

            mv.loadLocal(loc_item);
            mv.ifNull(l_itemIsNull);

            mv.putThisField(f_primitive, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    if (java.util.Date.class.equals(originalType)) {
                        // simple unboxing would result in a ClassCast with Date beeing casted to Number
                        // to deal with this specific case:
                        mg.ifThisInstanceOf(java.util.Date.class, new Script() {
                            @Override
                            public void execute(MethodGenerator mg) {
                                mg.loadLocal(loc_item);
                            }
                        }, new Script() {
                            @Override
                            public void execute(MethodGenerator mg) {
                                mg.loadLocal(loc_item);
                                mg.checkCast(java.util.Date.class);
                                mg.invokeVirtual(new MethodInstance(ReflectUtil.getDeclaredMethod(false, java.util.Date.class, long.class, "getTime")));
                            }
                        }, new Script() {
                            @Override
                            public void execute(MethodGenerator mg) {
                                mg.loadLocal(loc_item);
                                mg.unbox(f_primitive.getType());
                            }
                        });
                    } else {
                        mg.loadLocal(loc_item);
                        mg.unbox(f_primitive.getType());
                    }
                }
            });

            // field is a nullable numeric value in the entity, but a native numeric value in our RCV
            mv.putThisField(f_nullFlag, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.push(false);
                }
            });

            mv.goTo(l_finish);
            mv.mark(l_itemIsNull);

            // field is a nullable numeric value in the entity, but a native numeric value in our RCV
            mv.putThisField(f_nullFlag, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.push(true);
                }
            });
            mv.mark(l_finish);
        }
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementRelations() {
        var relationMembers = metaData.getRelationMembers();
        var f_relations = new FieldInstance[relationMembers.length];

        for (int relationIndex = 0, size = relationMembers.length; relationIndex < size; relationIndex++) {
            var member = relationMembers[relationIndex];
            var f_relation = implementField(new FieldInstance(Opcodes.ACC_PRIVATE, CacheMapEntryVisitor.getFieldName(member), null, IObjRef[].class));
            f_relations[relationIndex] = f_relation;
        }
        implementGetRelations(relationMembers, f_relations);
        implementSetRelations(relationMembers, f_relations);
        implementGetRelation(relationMembers, f_relations);
        implementSetRelation(relationMembers, f_relations);
    }

    protected void implementGetRelation(RelationMember[] relationMembers, final FieldInstance[] f_relations) {
        var template_m_getRelation = new MethodInstance(null, RootCacheValue.class, IObjRef[].class, "getRelation", int.class);

        implementSwitchByIndex(template_m_getRelation, "Given relationIndex not known", f_relations.length, new ScriptWithIndex() {
            @Override
            public void execute(MethodGenerator mg, int relationIndex) {
                var f_relation = f_relations[relationIndex];

                mg.getThisField(f_relation);
                mg.returnValue();
            }
        });
    }

    protected void implementSetRelation(RelationMember[] relationMembers, FieldInstance[] f_relations) {
        var template_m_setRelation = new MethodInstance(null, RootCacheValue.class, void.class, "setRelation", int.class, IObjRef[].class);

        var mv = visitMethod(template_m_setRelation);
        var l_finish = mv.newLabel();

        for (int relationIndex = 0, size = f_relations.length; relationIndex < size; relationIndex++) {
            var f_relation = f_relations[relationIndex];

            var l_notEqual = mv.newLabel();

            mv.loadArg(0);
            mv.push(relationIndex);

            mv.ifCmp(int.class, GeneratorAdapter.NE, l_notEqual);

            mv.putThisField(f_relation, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.loadArg(1);
                }
            });
            mv.goTo(l_finish);
            mv.mark(l_notEqual);
        }
        mv.throwException(Type.getType(IllegalArgumentException.class), "Given relationIndex not known");
        mv.mark(l_finish);
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementSetRelations(RelationMember[] relationMembers, FieldInstance[] fields) {
        var template_m_setRelations = new MethodInstance(null, RootCacheValue.class, void.class, "setRelations", IObjRef[][].class);

        var mv = visitMethod(template_m_setRelations);

        for (int relationIndex = 0, size = fields.length; relationIndex < size; relationIndex++) {
            var f_relation = fields[relationIndex];
            var f_relationIndex = relationIndex;

            mv.putThisField(f_relation, new Script() {
                @Override
                public void execute(MethodGenerator mg) {
                    mg.loadArg(0);
                    mg.push(f_relationIndex);
                    mg.arrayLoad(objRefArrayType);
                }
            });
        }
        mv.returnValue();
        mv.endMethod();
    }

    protected void implementGetRelations(RelationMember[] relationMembers, FieldInstance[] f_relations) {
        var template_m_getRelations = new MethodInstance(null, RootCacheValue.class, IObjRef[][].class, "getRelations");

        var mv = visitMethod(template_m_getRelations);

        if (f_relations.length == 0) {
            mv.getField(sf_objRefArrayArray);
            mv.returnValue();
        }
        mv.push(f_relations.length);
        mv.newArray(objRefArrayType);

        for (int relationIndex = 0, size = f_relations.length; relationIndex < size; relationIndex++) {
            var f_primitive = f_relations[relationIndex];

            // duplicate array instance on stack
            mv.dup();

            mv.push(relationIndex);
            mv.getThisField(f_primitive);
            mv.arrayStore(objRefArrayType);
        }
        mv.returnValue();
        mv.endMethod();
    }
}
