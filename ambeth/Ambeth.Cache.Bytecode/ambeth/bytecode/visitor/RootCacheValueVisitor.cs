using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class RootCacheValueVisitor : ClassVisitor
    {
        private static readonly Type objType = typeof(Object);

        private static readonly Type objRefArrayType = typeof(IObjRef[]);

        protected readonly IEntityMetaData metaData;

        public RootCacheValueVisitor(IClassVisitor cv, IEntityMetaData metaData)
            : base(cv)
        {
            this.metaData = metaData;
        }

        public override void VisitEnd()
        {
            ImplementGetEntityType();
            ImplementId();
            ImplementVersion();
            ImplementPrimitives();
            ImplementRelations();
            base.VisitEnd();
        }

        protected void ImplementGetEntityType()
        {
            MethodInstance template_m_getEntityType = new MethodInstance(null, typeof(RootCacheValue), typeof(Type), "getEntityType");

            IMethodVisitor mv = VisitMethod(template_m_getEntityType);
            mv.Push(metaData.EntityType);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementId()
        {
            MethodInstance template_m_getId = new MethodInstance(null, typeof(RootCacheValue), typeof(Object), "get_Id");
            MethodInstance template_m_setId = new MethodInstance(null, typeof(RootCacheValue), typeof(void), "set_Id", typeof(Object));

            CacheMapEntryVisitor.ImplementNativeField(this, metaData.IdMember, template_m_getId, template_m_setId);
        }

        protected void ImplementVersion()
        {
            MethodInstance template_m_getVersion = new MethodInstance(null, typeof(RootCacheValue), typeof(Object), "get_Version");
            MethodInstance template_m_setVersion = new MethodInstance(null, typeof(RootCacheValue), typeof(void), "set_Version", typeof(Object));

            CacheMapEntryVisitor.ImplementNativeField(this, metaData.VersionMember, template_m_getVersion, template_m_setVersion);
        }

        protected void ImplementPrimitives()
        {
            Member[] primitiveMembers = metaData.PrimitiveMembers;
            FieldInstance[] f_primitives = new FieldInstance[primitiveMembers.Length];
            FieldInstance[] f_nullFlags = new FieldInstance[primitiveMembers.Length];
            Type[] fieldType = new Type[primitiveMembers.Length];

            for (int primitiveIndex = 0, size = primitiveMembers.Length; primitiveIndex < size; primitiveIndex++)
            {
                Member member = primitiveMembers[primitiveIndex];
                Type realType = member.RealType;
                Type nativeType = ImmutableTypeSet.GetUnwrappedType(realType);
                bool isNullable = true;
                if (nativeType == null)
                {
                    nativeType = realType;
                    isNullable = false;
                }
                if (!nativeType.IsPrimitive)
			    {
				    nativeType = typeof(Object);
			    }
                if (isNullable)
                {
                    // field is a nullable numeric field. We need a flag field to handle true null case
                    FieldInstance f_nullFlag = ImplementField(new FieldInstance(FieldAttributes.Private, CacheMapEntryVisitor.GetFieldName(member) + "$isNull", typeof(bool)));
                    f_nullFlags[primitiveIndex] = f_nullFlag;
                }
                fieldType[primitiveIndex] = nativeType;
                FieldInstance f_primitive = ImplementField(new FieldInstance(FieldAttributes.Private, CacheMapEntryVisitor.GetFieldName(member), nativeType));
                f_primitives[primitiveIndex] = f_primitive;
            }
            ImplementGetPrimitive(primitiveMembers, f_primitives, f_nullFlags);
            ImplementGetPrimitives(primitiveMembers, f_primitives, f_nullFlags);
            ImplementSetPrimitives(primitiveMembers, f_primitives, f_nullFlags);
        }

        protected void ImplementGetPrimitive(Member[] primitiveMember, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
	    {
		    MethodInstance template_m_getPrimitive = new MethodInstance(null, typeof(RootCacheValue), typeof(Object), "GetPrimitive", typeof(int));

		    IMethodVisitor mv = VisitMethod(template_m_getPrimitive);

            if (f_primitives.Length > 0)
            {
                Label l_default = mv.NewLabel();
                Label[] l_primitives = new Label[f_primitives.Length];
                for (int primitiveIndex = 0, size = f_primitives.Length; primitiveIndex < size; primitiveIndex++)
                {
                    l_primitives[primitiveIndex] = mv.NewLabel();
                }

                mv.LoadArg(0);
                mv.Switch(0, l_primitives.Length - 1, l_default, l_primitives);

                for (int primitiveIndex = 0, size = f_primitives.Length; primitiveIndex < size; primitiveIndex++)
                {
                    FieldInstance f_primitive = f_primitives[primitiveIndex];
                    FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];

                    mv.Mark(l_primitives[primitiveIndex]);

                    Label? l_fieldIsNull = null;

                    if (f_nullFlag != null)
                    {
                        l_fieldIsNull = mv.NewLabel();
                        // only do something if the field is non-null
                        mv.GetThisField(f_nullFlag);
                        mv.IfZCmp(CompareOperator.NE, l_fieldIsNull.Value);
                    }
                    mv.GetThisField(f_primitive);
                    mv.ValueOf(f_primitive.Type.Type);
                    mv.ReturnValue();

                    if (f_nullFlag != null)
                    {
                        mv.Mark(l_fieldIsNull.Value);
                        mv.PushNull();
                    }
                    mv.ReturnValue();
                }
                mv.Mark(l_default);
            }
		    mv.ThrowException(typeof(ArgumentException), "Given relationIndex not known");
		    mv.PushNull();
		    mv.ReturnValue();
		    mv.EndMethod();
	    }

        protected void ImplementGetPrimitives(Member[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
        {
            MethodInstance template_m_getPrimitives = new MethodInstance(null, typeof(RootCacheValue), typeof(Object[]), "GetPrimitives");

            IMethodVisitor mv = VisitMethod(template_m_getPrimitives);

            mv.Push(f_primitives.Length);
            mv.NewArray(objType);

            for (int primitiveIndex = 0, size = f_primitives.Length; primitiveIndex < size; primitiveIndex++)
            {
                FieldInstance f_primitive = f_primitives[primitiveIndex];
                FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];

                Label? l_fieldIsNull = null;

                if (f_nullFlag != null)
                {
                    l_fieldIsNull = mv.NewLabel();
                    // only do something if the field is non-null
                    mv.GetThisField(f_nullFlag);
                    mv.IfZCmp(CompareOperator.NE, l_fieldIsNull.Value);
                }
                // duplicate array instance on stack
                mv.Dup();

                mv.Push(primitiveIndex);
                mv.GetThisField(f_primitive);

                mv.ValueOf(f_primitive.Type.Type);
                mv.ArrayStore(objType);

                if (f_nullFlag != null)
                {
                    mv.Mark(l_fieldIsNull.Value);
                }
            }
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementSetPrimitives(Member[] primitiveMembers, FieldInstance[] f_primitives, FieldInstance[] f_nullFlags)
        {
            MethodInstance template_m_setPrimitives = new MethodInstance(null, typeof(RootCacheValue), typeof(void), "SetPrimitives", typeof(Object[]));

            IMethodVisitor mv = VisitMethod(template_m_setPrimitives);
            LocalVariableInfo loc_item = mv.NewLocal(objType);

            for (int primitiveIndex = 0, size = f_primitives.Length; primitiveIndex < size; primitiveIndex++)
            {
                FieldInstance f_primitive = f_primitives[primitiveIndex];
                FieldInstance f_nullFlag = f_nullFlags[primitiveIndex];
                Member member = primitiveMembers[primitiveIndex];
			    Type originalType = member.RealType;

                Script script_loadArrayValue = new Script(delegate(IMethodVisitor mg)
			    {
					mg.LoadArg(0);
					mg.Push(primitiveIndex);
					mg.ArrayLoad(objType);
			    });

                Label l_finish = mv.NewLabel();

                if (f_nullFlag == null)
			    {
				    if (!originalType.IsValueType)
				    {
					    mv.PutThisField(f_primitive, script_loadArrayValue);
					    continue;
				    }
				    script_loadArrayValue(mv);
				    mv.StoreLocal(loc_item);
				    mv.LoadLocal(loc_item);
				    mv.IfNull(l_finish);

				    mv.PutThisField(f_primitive, new Script(delegate(IMethodVisitor mg)
					    {
						    mg.LoadLocal(loc_item);
						    mg.Unbox(f_primitive.Type.Type);
					    }));

				    mv.Mark(l_finish);
				    continue;
			    }

                Label l_itemIsNull = mv.NewLabel();
                
                script_loadArrayValue(mv);
                mv.StoreLocal(loc_item);

                mv.LoadLocal(loc_item);
                mv.IfNull(l_itemIsNull);

                mv.PutThisField(f_primitive, delegate(IMethodVisitor mg)
                {
                    mg.LoadLocal(loc_item);
                    mg.Unbox(f_primitive.Type.Type);
                });

                if (f_nullFlag != null)
                {
                    // field is a nullable numeric value in the entity, but a native numeric value in our RCV
                    mv.PutThisField(f_nullFlag, delegate(IMethodVisitor mg)
                    {
                        mg.Push(false);
                    });
                }

                mv.GoTo(l_finish);
                mv.Mark(l_itemIsNull);

                if (f_nullFlag != null)
                {
                    // field is a nullable numeric value in the entity, but a native numeric value in our RCV
                    mv.PutThisField(f_nullFlag, delegate(IMethodVisitor mg)
                    {
                        mg.Push(true);
                    });
                }
                else
                {
                    mv.PutThisField(f_primitive, delegate(IMethodVisitor mg)
                    {
                        mg.PushNullOrZero(f_primitive.Type.Type);
                    });
                }
                mv.Mark(l_finish);
            }
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementRelations()
        {
            RelationMember[] relationMembers = metaData.RelationMembers;
            FieldInstance[] f_relations = new FieldInstance[relationMembers.Length];

            for (int relationIndex = 0, size = relationMembers.Length; relationIndex < size; relationIndex++)
            {
                RelationMember member = relationMembers[relationIndex];
                FieldInstance f_relation = ImplementField(new FieldInstance(FieldAttributes.Private, CacheMapEntryVisitor.GetFieldName(member), typeof(IObjRef[])));
                f_relations[relationIndex] = f_relation;
            }
            ImplementGetRelations(relationMembers, f_relations);
            ImplementSetRelations(relationMembers, f_relations);
            ImplementGetRelation(relationMembers, f_relations);
            ImplementSetRelation(relationMembers, f_relations);
        }

        protected void ImplementGetRelation(RelationMember[] relationMembers, FieldInstance[] f_relations)
	    {
		    MethodInstance template_m_getRelation = new MethodInstance(null, typeof(RootCacheValue), typeof(IObjRef[]), "GetRelation", typeof(int));

		    IMethodVisitor mv = VisitMethod(template_m_getRelation);

            if (f_relations.Length > 0)
            {
                Label l_default = mv.NewLabel();
                Label[] l_relations = new Label[f_relations.Length];
                for (int relationIndex = 0, size = l_relations.Length; relationIndex < size; relationIndex++)
                {
                    l_relations[relationIndex] = mv.NewLabel();
                }

                mv.LoadArg(0);
                mv.Switch(0, l_relations.Length - 1, l_default, l_relations);

                for (int relationIndex = 0, size = f_relations.Length; relationIndex < size; relationIndex++)
                {
                    FieldInstance f_relation = f_relations[relationIndex];

                    mv.Mark(l_relations[relationIndex]);
                    mv.GetThisField(f_relation);
                    mv.ReturnValue();
                }
                mv.Mark(l_default);
            }
		    mv.ThrowException(typeof(ArgumentException), "Given relationIndex not known");
            mv.PushNull();
		    mv.ReturnValue();
		    mv.EndMethod();
	    }

        protected void ImplementSetRelation(RelationMember[] relationMembers, FieldInstance[] f_relations)
        {
            MethodInstance template_m_setRelation = new MethodInstance(null, typeof(RootCacheValue), typeof(void), "SetRelation", typeof(int), typeof(IObjRef[]));

            IMethodVisitor mv = VisitMethod(template_m_setRelation);
            Label l_finish = mv.NewLabel();

            for (int relationIndex = 0, size = f_relations.Length; relationIndex < size; relationIndex++)
            {
                FieldInstance f_relation = f_relations[relationIndex];

                Label l_notEqual = mv.NewLabel();

                mv.LoadArg(0);
                mv.Push(relationIndex);

                mv.IfCmp(typeof(int), CompareOperator.NE, l_notEqual);

                mv.PutThisField(f_relation, delegate(IMethodVisitor mg)
                {
                    mg.LoadArg(1);
                });
                mv.GoTo(l_finish);
                mv.Mark(l_notEqual);
            }
            mv.ThrowException(typeof(ArgumentException), "Given relationIndex not known");
            mv.Mark(l_finish);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementSetRelations(RelationMember[] relationMembers, FieldInstance[] fields)
        {
            MethodInstance template_m_setRelations = new MethodInstance(null, typeof(RootCacheValue), typeof(void), "SetRelations", typeof(IObjRef[][]));

            IMethodVisitor mv = VisitMethod(template_m_setRelations);

            for (int relationIndex = 0, size = fields.Length; relationIndex < size; relationIndex++)
            {
                FieldInstance f_relation = fields[relationIndex];
                int f_relationIndex = relationIndex;

                mv.PutThisField(f_relation, delegate(IMethodVisitor mg)
                {
                    mg.LoadArg(0);
                    mg.Push(f_relationIndex);
                    mg.ArrayLoad(objRefArrayType);
                });
            }
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementGetRelations(RelationMember[] relationMembers, FieldInstance[] f_relations)
        {
            MethodInstance template_m_getRelations = new MethodInstance(null, typeof(RootCacheValue), typeof(IObjRef[][]), "GetRelations");

            IMethodVisitor mv = VisitMethod(template_m_getRelations);

            mv.Push(f_relations.Length);
            mv.NewArray(objRefArrayType);

            for (int relationIndex = 0, size = f_relations.Length; relationIndex < size; relationIndex++)
            {
                FieldInstance f_primitive = f_relations[relationIndex];

                // duplicate array instance on stack
                mv.Dup();

                mv.Push(relationIndex);
                mv.GetThisField(f_primitive);
                mv.ArrayStore(objRefArrayType);
            }
            mv.ReturnValue();
            mv.EndMethod();
        }
    }
}