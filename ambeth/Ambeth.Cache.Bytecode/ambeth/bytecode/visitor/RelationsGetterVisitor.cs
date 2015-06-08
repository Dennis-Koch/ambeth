using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class RelationsGetterVisitor : ClassVisitor
    {
        public class ValueHolderContainerEntryValueResolver : IValueResolveDelegate
        {
            private readonly ValueHolderIEC valueHolderContainerHelper;

            public ValueHolderContainerEntryValueResolver(ValueHolderIEC valueHolderContainerHelper)
            {
                this.valueHolderContainerHelper = valueHolderContainerHelper;
            }

            public Type ValueType
            {
                get { return typeof(ValueHolderContainerEntry); }
            }

            public Object Invoke(String fieldName, Type enhancedType)
            {
                return valueHolderContainerHelper.GetVhcEntry(enhancedType);
            }
        }

        public static readonly Type templateType = typeof(ValueHolderContainerMixin);

        protected static readonly String templatePropertyName = "__" + templateType.Name;

        private static readonly PropertyInstance p_template_targetCache = new PropertyInstance(typeof(IValueHolderContainer).GetProperty("__TargetCache"));

        private static readonly MethodInstance m_vhce_getState_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(ValueHolderState), "GetState", typeof(Object),
            typeof(int));

        private static readonly MethodInstance m_vhce_setInitPending_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(void), "SetInitPending", typeof(Object),
            typeof(int));

        private static readonly MethodInstance m_vhce_isInitialized_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(bool), "IsInitialized", typeof(Object),
            typeof(int));

        private static readonly MethodInstance m_vhce_getObjRefs_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(IObjRef[]), "GetObjRefs", typeof(Object),
            typeof(int));

       	private static readonly MethodInstance m_vhce_setObjRefs_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(void), "SetObjRefs",
			typeof(Object), typeof(int), typeof(IObjRef[]));

        private static readonly MethodInstance m_vhce_getValueDirect_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(Object), "GetValueDirect", typeof(Object),
            typeof(int));

        private static readonly MethodInstance m_vhce_setValueDirect_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(void), "SetValueDirect",
            typeof(Object), typeof(int), typeof(Object));

    	private static readonly MethodInstance m_vhce_setUninitialized_Member = new MethodInstance(null, typeof(ValueHolderContainerEntry), typeof(void),
			"SetUninitialized", typeof(Object), typeof(int), typeof(IObjRef[]));

        private static readonly MethodInstance m_template_getCache = new MethodInstance(null, typeof(IObjRefContainer), typeof(ICache), "Get__Cache");

		private static readonly MethodInstance m_template_detach = new MethodInstance(null, typeof(IObjRefContainer), typeof(void), "Detach");

        private static readonly MethodInstance m_template_getState_Member = new MethodInstance(null, typeof(IObjRefContainer), typeof(ValueHolderState), "Get__State", typeof(int));

        private static readonly MethodInstance m_template_setInitPending_Member = new MethodInstance(null, typeof(IValueHolderContainer), typeof(void), "Set__InitPending", typeof(int));

        public static readonly MethodInstance m_template_isInitialized_Member = new MethodInstance(null, typeof(IObjRefContainer), typeof(bool), "Is__Initialized", typeof(int));

        private static readonly MethodInstance m_template_getObjRefs_Member = new MethodInstance(null, typeof(IObjRefContainer), typeof(IObjRef[]), "Get__ObjRefs",
            typeof(int));

       	private static readonly MethodInstance m_template_setObjRefs_Member = new MethodInstance(null, typeof(IObjRefContainer), typeof(void), "Set__ObjRefs",
			typeof(int), typeof(IObjRef[]));

        public static readonly MethodInstance m_template_getValueDirect_Member = new MethodInstance(null, typeof(IValueHolderContainer), typeof(Object), "Get__ValueDirect",
            typeof(int));

        private static readonly MethodInstance m_template_setValueDirect_Member = new MethodInstance(null, typeof(IValueHolderContainer), typeof(void), "Set__ValueDirect",
            typeof(int), typeof(Object));

        private static readonly MethodInstance m_template_setUninitialized_Member = new MethodInstance(null, typeof(IValueHolderContainer), typeof(void), "Set__Uninitialized",
			typeof(int), typeof(IObjRef[]));

        private static readonly MethodInstance m_template_getSelf = new MethodInstance(null, templateType, typeof(IObjRelation), "GetSelf",
                typeof(IObjRefContainer), typeof(int));

        private static readonly MethodInstance m_template_getValue = new MethodInstance(null, templateType, typeof(Object), "GetValue",
                typeof(IValueHolderContainer), typeof(RelationMember[]), typeof(int), typeof(ICacheIntern), typeof(IObjRef[]));

        public static PropertyInstance GetValueHolderContainerTemplatePI(IClassVisitor cv)
        {
            Object bean = State.BeanContext.GetService(templateType);
            PropertyInstance pi = State.GetProperty(templatePropertyName, NewType.GetType(bean.GetType()));
            if (pi != null)
            {
                return pi;
            }
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        public static FieldInstance GetObjRefsField(String propertyName, bool expectExistance)
        {
            String fieldName = ValueHolderIEC.GetObjRefsFieldName(propertyName);
            FieldInstance field = State.GetAlreadyImplementedField(fieldName);
            if (field == null && expectExistance)
            {
                throw new Exception("Field not defined in type hierarchy: " + State.NewType.ClassName + "."
                        + fieldName);
            }
            return field;
        }

        public static FieldInstance GetInitializedField(String propertyName, bool expectExistance)
        {
            String fieldName = ValueHolderIEC.GetInitializedFieldName(propertyName);
            FieldInstance field = State.GetAlreadyImplementedField(fieldName);
            if (field == null && expectExistance)
            {
                throw new Exception("Field not defined in type hierarchy: " + State.NewType.ClassName + "."
                        + fieldName);
            }
            return field;
        }

        private readonly IEntityMetaData metaData;

        private readonly IPropertyInfoProvider propertyInfoProvider;

        private readonly ValueHolderIEC valueHolderContainerHelper;

        public RelationsGetterVisitor(IClassVisitor cv, IEntityMetaData metaData, ValueHolderIEC valueHolderContainerHelper, IPropertyInfoProvider propertyInfoProvider)
            : base(cv)
        {
            this.metaData = metaData;
            this.valueHolderContainerHelper = valueHolderContainerHelper;
            this.propertyInfoProvider = propertyInfoProvider;
        }

        public override void VisitEnd()
        {
            PropertyInstance p_valueHolderContainerTemplate = GetValueHolderContainerTemplatePI(this);
            PropertyInstance p_relationMembers = ImplementAssignedReadonlyProperty("__RelationMembers", metaData.RelationMembers);

            PropertyInstance p_targetCache = ImplementTargetCache(p_valueHolderContainerTemplate);

            if (!EmbeddedEnhancementHint.HasMemberPath(State.Context))
            {
                PropertyInstance p_valueHolderContainerEntry = ImplementAssignedReadonlyProperty("ValueHolderContainerEntry", new ValueHolderContainerEntryValueResolver(valueHolderContainerHelper));
                ImplementGetState(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementSetInitPending(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementIsInitialized(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementGetObjRefs(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementSetObjRefs(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementGetValueDirect(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementSetValueDirect(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
                ImplementSetUninitialized(p_valueHolderContainerTemplate, p_valueHolderContainerEntry);
            }

            ImplementValueHolderCode(p_valueHolderContainerTemplate, p_targetCache, p_relationMembers);

            ImplementConstructors();
            base.VisitEnd();
        }

        protected void ImplementConstructors()
        {
            if (metaData.RelationMembers.Length == 0)
            {
                return;
            }
            RelationMember[] relationMembers = metaData.RelationMembers;
            List<FieldInstance[]> fieldsList = new List<FieldInstance[]>();

            for (int a = relationMembers.Length; a-- > 0; )
            {
                RelationMember relationMember = relationMembers[a];
                relationMember = (RelationMember) GetApplicableMember(relationMember);
                if (relationMember == null)
                {
                    // member is handled in another type
                    continue;
                }
                String propertyName = relationMember.Name;
                String fieldName = ValueHolderIEC.GetObjRefsFieldName(propertyName);
                FieldInstance field = State.GetAlreadyImplementedField(fieldName);

                String fieldName2 = ValueHolderIEC.GetInitializedFieldName(propertyName);
                FieldInstance field2 = State.GetAlreadyImplementedField(fieldName2);

                fieldsList.Add(new FieldInstance[] { field, field2 });
            }
            if (fieldsList.Count == 0)
            {
                return;
            }
            PropertyInstance p_emptyRelations = ImplementAssignedReadonlyProperty("EmptyRelations", ObjRef.EMPTY_ARRAY);

            OverrideConstructors(delegate(IClassVisitor cv, ConstructorInstance superConstructor)
            {
                IMethodVisitor mv = cv.VisitMethod(superConstructor);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeSuperOfCurrentMethod();

                LocalVariableInfo loc_emptyRelations = mv.NewLocal<IObjRef[]>();
                LocalVariableInfo loc_lazyState = mv.NewLocal<ValueHolderState>();
                mv.CallThisGetter(p_emptyRelations);
                mv.StoreLocal(loc_emptyRelations);
                mv.PushEnum(ValueHolderState.LAZY);
                mv.StoreLocal(loc_lazyState);
                foreach (FieldInstance[] fields in fieldsList)
                {
                    mv.PutThisField(fields[0], delegate(IMethodVisitor mv2)
                        {
                            mv2.LoadLocal(loc_emptyRelations);
                        });
                    mv.PutThisField(fields[1], delegate(IMethodVisitor mv2)
                    {
                        mv2.LoadLocal(loc_lazyState);
                    });
                }
                mv.ReturnValue();
                mv.EndMethod();
            });
        }

        protected FieldInstance GetObjRefsFieldByPropertyName(String propertyName)
        {
            String fieldName = ValueHolderIEC.GetObjRefsFieldName(propertyName);

            FieldInstance field = State.GetAlreadyImplementedField(fieldName);
            if (field == null)
            {
                field = new FieldInstance(FieldAttributes.Public, fieldName, typeof(IObjRef[]));
            }
            return field;
        }

        protected FieldInstance GetInitializedFieldByPropertyName(String propertyName)
        {
            String fieldName = ValueHolderIEC.GetInitializedFieldName(propertyName);

            FieldInstance field = State.GetAlreadyImplementedField(fieldName);
            if (field == null)
            {
                field = new FieldInstance(FieldAttributes.Public, fieldName, typeof(ValueHolderState));
            }
            return field;
        }

        protected void ImplementGetState(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_getState_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_getState_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementSetInitPending(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_setInitPending_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_setInitPending_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementIsInitialized(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_isInitialized_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_isInitialized_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementGetObjRefs(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_getObjRefs_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_getObjRefs_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementGetValueDirect(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_getValueDirect_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_getValueDirect_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementSetValueDirect(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_setValueDirect_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_setValueDirect_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementSetObjRefs(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_setObjRefs_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_setObjRefs_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementSetUninitialized(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_valueHolderContainerEntry)
        {
            {
                IMethodVisitor mv = VisitMethod(m_template_setUninitialized_Member);
                mv.CallThisGetter(p_valueHolderContainerEntry);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeVirtual(m_vhce_setUninitialized_Member);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected PropertyInstance ImplementTargetCache(PropertyInstance p_valueHolderContainerTemplate)
        {
            if (EmbeddedEnhancementHint.HasMemberPath(State.Context))
            {
                PropertyInstance p_rootEntity = EmbeddedTypeVisitor.GetRootEntityProperty(this);
                PropertyInstance p_targetCache2 = ImplementProperty(p_template_targetCache, delegate(IMethodVisitor mv)
                {
                    Label l_finish = mv.NewLabel();
                    mv.CallThisGetter(p_rootEntity);
                    mv.Dup();
                    mv.IfNull(l_finish);
                    mv.CheckCast(typeof(IValueHolderContainer));
                    mv.InvokeInterface(p_template_targetCache.Getter);
                    mv.Mark(l_finish);
                    mv.ReturnValue();
                }, null);
                return p_targetCache2;
            }
            ImplementSelfGetter(p_valueHolderContainerTemplate);

            FieldInstance f_targetCache = ImplementField(new FieldInstance(FieldAttributes.Private, "__targetCache", p_template_targetCache.PropertyType));

            PropertyInstance p_targetCache = ImplementProperty(p_template_targetCache, delegate(IMethodVisitor mv)
            {
                mv.GetThisField(f_targetCache);
                mv.ReturnValue();
            }, delegate(IMethodVisitor mv)
            {
                mv.PutThisField(f_targetCache, delegate(IMethodVisitor mv2)
                {
                    mv.LoadArg(0);
                });
                mv.ReturnValue();
            });
            {
                IMethodVisitor mg = VisitMethod(m_template_getCache);
                mg.CallThisGetter(p_targetCache);
                mg.ReturnValue();
                mg.EndMethod();
            }
			{
				IMethodVisitor mg = VisitMethod(m_template_detach);
				mg.CallThisSetter(p_targetCache, delegate(IMethodVisitor mg2)
					{
						mg2.PushNull();
					});
				mg.ReturnValue();
				mg.EndMethod();
			}
            return p_targetCache;
        }

        protected void ImplementValueHolderCode(PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_targetCache, PropertyInstance p_relationMembers)
        {
            RelationMember[] relationMembers = metaData.RelationMembers;
            NewType owner = State.NewType;
            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                RelationMember relationMember = relationMembers[relationIndex];

                relationMember = (RelationMember) GetApplicableMember(relationMember);
                if (relationMember == null)
                {
                    // member is handled in another type
                    continue;
                }
                String propertyName = relationMember.Name;
                IPropertyInfo propertyInfo = propertyInfoProvider.GetProperty(relationMember.DeclaringType, propertyName);
                PropertyInstance prop = PropertyInstance.FindByTemplate(propertyInfo, true);
                MethodInstance m_get = prop != null ? prop.Getter : new MethodInstance(((MethodPropertyInfo)propertyInfo).Getter);
                MethodInstance m_set = prop != null ? prop.Setter : new MethodInstance(((MethodPropertyInfo)propertyInfo).Setter);

                FieldInstance f_objRefs = GetObjRefsFieldByPropertyName(propertyName);
                FieldInstance f_objRefs_existing = State.GetAlreadyImplementedField(f_objRefs.Name);

                FieldInstance f_initialized = GetInitializedFieldByPropertyName(propertyName);
                FieldInstance f_initialized_existing = State.GetAlreadyImplementedField(f_initialized.Name);

                if (f_objRefs_existing == null)
                {
                    f_objRefs_existing = ImplementField(f_objRefs);
                    ImplementGetter(new MethodInstance(null, MethodAttributes.Public, f_objRefs_existing.Type, "get_" + f_objRefs_existing.Name),
                        f_objRefs_existing);
                    ImplementSetter(
                            new MethodInstance(null, MethodAttributes.Public, NewType.VOID_TYPE, "set_" + f_objRefs_existing.Name, f_objRefs_existing.Type),
                            f_objRefs_existing);
                }
                if (f_initialized_existing == null)
                {
                    f_initialized_existing = ImplementField(f_initialized);
                    ImplementGetter(new MethodInstance(null, MethodAttributes.Public, f_initialized_existing.Type, "get_" + f_initialized_existing.Name),
                        f_initialized_existing);
                    ImplementSetter(new MethodInstance(null, MethodAttributes.Public, NewType.VOID_TYPE, "set_" + f_initialized_existing.Name, f_initialized_existing.Type), f_initialized_existing);
                }

                ImplementRelationGetter(propertyName, m_get, m_set, relationIndex, p_valueHolderContainerTemplate, p_targetCache, p_relationMembers, f_initialized_existing, f_objRefs_existing);
                ImplementRelationSetter(propertyName, m_set, f_initialized_existing, f_objRefs_existing);
            }
        }

        protected Member GetApplicableMember(Member relationMember)
        {
            String propertyName = relationMember.Name;
            if (relationMember is IEmbeddedMember)
            {
                String memberPath = EmbeddedEnhancementHint.GetMemberPath(State.Context);
                if (memberPath != null)
                {
                    if (!propertyName.StartsWith(memberPath + "."))
                    {
                        // This relation has to be handled by another embedded type
                        return null;
                    }
                    propertyName = propertyName.Substring(memberPath.Length + 1);
                    if (propertyName.Contains("."))
                    {
                        // This relation has to be handled by another child embedded type of this embedded type
                        return null;
                    }
                    if (relationMember is IEmbeddedMember)
				    {
					    relationMember = ((IEmbeddedMember) relationMember).ChildMember;
				    }
                }
                else if (propertyName.Contains("."))
                {
                    // This is an embedded member which will be implemented in the enhanced embedded type
                    return null;
                }
            }
            else if (EmbeddedEnhancementHint.HasMemberPath(State.Context))
            {
                // entities are already enhanced
                return null;
            }
            return relationMember;
        }

        protected void ImplementSelfGetter(PropertyInstance p_valueHolderContainerTemplate)
        {
            NewType owner = State.NewType;
            MethodInstance m_getSelf = new MethodInstance(owner, typeof(IValueHolderContainer), typeof(IObjRelation), "Get__Self", typeof(int));
            {
                // public IObjRelation getSelf(String memberName)
                // {
                // return ValueHolderContainerTemplate.GetSelf(this, relationIndex);
                // }
                IMethodVisitor mv = VisitMethod(m_getSelf);
                mv.CallThisGetter(p_valueHolderContainerTemplate);
                // this
                mv.LoadThis();
                // relationIndex
                mv.LoadArgs();
                mv.InvokeVirtual(m_template_getSelf);
                mv.ReturnValue();
                mv.EndMethod();
            }
        }

        protected void ImplementRelationGetter(String propertyName, MethodInstance m_getMethod_template, MethodInstance m_setMethod, int relationIndex,
                PropertyInstance p_valueHolderContainerTemplate, PropertyInstance p_targetCache, PropertyInstance p_relationMembers, FieldInstance f_initialized,
                FieldInstance f_objRefs)
        {
            // public String getPropertyName()
            // {
            // if (!PropertyName$initialized)
            // {
            // setPropertyName(RelationsGetterVisitor.valueHolderContainer_getValue(this, $relationMembers, get__IndexOfPropertyName(), $targetCache, $beanContext,
            // propertyName$objRefs));
            // }
            // return super.getPropertyName();
            // }
            Script script_getVHC;
            if (EmbeddedEnhancementHint.HasMemberPath(State.Context))
            {
                PropertyInstance p_rootEntity = EmbeddedTypeVisitor.GetRootEntityProperty(this);
                script_getVHC = delegate(IMethodVisitor mv)
                {
                    mv.CallThisGetter(p_rootEntity);
                };
            }
            else
            {
                script_getVHC = delegate(IMethodVisitor mv)
                {
                    // this
                    mv.LoadThis();
                };
            }

            MethodInstance m_getMethod;
            {
                PropertyInstance p_cacheModification = SetCacheModificationMethodCreator.GetCacheModificationPI(this);
                MethodInstance m_getMethod_scoped = new MethodInstance(State.NewType,
                        MethodAttributes.HideBySig | MethodAttributes.Private | MethodAttributes.Final, NewType.VOID_TYPE, propertyName + "$DoInitialize");
                {
                    IMethodVisitor mg = VisitMethod(m_getMethod_scoped);

                    // this => for this.setPropertyName(...)
                    mg.LoadThis();
                    // call template.getValue(..)
                    mg.CallThisGetter(p_valueHolderContainerTemplate);
                    // getVhc()
                    script_getVHC.Invoke(mg);
                    // $relationMembers
                    mg.CallThisGetter(p_relationMembers);
                    // get__IndexOfPropertyName()
                    mg.Push(relationIndex);
                    // $targetCache
                    mg.CallThisGetter(p_targetCache);
                    // propertyName$objRefs
                    mg.GetThisField(f_objRefs);
                    mg.InvokeVirtual(m_template_getValue);
                    mg.CheckCast(m_setMethod.Parameters[0].Type);
                    mg.InvokeVirtual(m_setMethod);
                    mg.ReturnValue();
                    mg.EndMethod();
                }
                {
                    IMethodVisitor mg = base.VisitMethod(m_getMethod_template);
                    m_getMethod = mg.Method;
                    HideFromDebug(m_getMethod);
                    Label l_initialized = mg.NewLabel();
                    mg.GetThisField(f_initialized);
                    mg.PushEnum(ValueHolderState.INIT);
                    mg.IfCmp(typeof(ValueHolderState), CompareOperator.EQ, l_initialized);

                    SetCacheModificationMethodCreator.CacheModificationInternalUpdate(p_cacheModification, mg,
                        delegate(IMethodVisitor mv2)
                        {
                            mv2.LoadThis();
                            mv2.InvokeOnExactOwner(m_getMethod_scoped);
                        });

                    mg.Mark(l_initialized);
                    mg.LoadThis();
                    mg.InvokeSuperOfCurrentMethod();
                    mg.ReturnValue();
                    mg.EndMethod();
                }
            }

            // public String getPropertyName$NoInit()
            // {
            // return super.getPropertyName();
            // }
            {
                MethodInstance m_getNoInit = m_getMethod_template.DeriveName(ValueHolderIEC.GetGetterNameOfRelationPropertyWithNoInit(propertyName));
                IMethodVisitor mg = base.VisitMethod(m_getNoInit);
                PropertyInstance p_getNoInit = PropertyInstance.FindByTemplate(propertyName + ValueHolderIEC.GetNoInitSuffix(), m_getNoInit.ReturnType, false);
                p_getNoInit.AddAnnotation(c_fireThisOPC, propertyName);
                p_getNoInit.AddAnnotation(c_fireTargetOPC, propertyName);
                mg.LoadThis();
                mg.InvokeSuper(m_getMethod);
                mg.ReturnValue();
                mg.EndMethod();
            }
        }

        protected void ImplementRelationSetter(String propertyName, MethodInstance m_set_template, FieldInstance f_initialized, FieldInstance f_objRefs)
        {
            // public void setPropertyName(String propertyName)
            // {
            // PropertyName$initialized = true;
            // PropertyName$objRefs = null;
            // super.setPropertyName(propertyName);
            // }
            MethodInstance m_set;
            {
                IMethodVisitor mg = base.VisitMethod(m_set_template);
                m_set = mg.Method;
                mg.PutThisField(f_initialized, delegate(IMethodVisitor mv2)
                    {
                        mg.PushEnum(ValueHolderState.INIT);
                    });
                mg.PutThisField(f_objRefs, delegate(IMethodVisitor mv2)
                    {
                        mv2.PushNull();
                    });
                mg.LoadThis();
                mg.LoadArgs();
                mg.InvokeSuperOfCurrentMethod();
                mg.ReturnVoidOrThis();
                mg.EndMethod();
            }

            // public void setPropertyName$NoInit(String propertyName)
            // {
            // super.setPropertyName(propertyName);
            // }
            {
                String noInitSetMethodName = ValueHolderIEC.GetSetterNameOfRelationPropertyWithNoInit(propertyName);
                IMethodVisitor mv = base.VisitMethod(m_set.Access, noInitSetMethodName, m_set.ReturnType, m_set.Parameters);
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeSuper(m_set);
                mv.ReturnVoidOrThis();
                mv.EndMethod();
            }
        }
    }
}