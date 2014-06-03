using De.Osthus.Ambeth.Bytecode.Util;
using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Collections.Specialized;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    /**
     * NotifyPropertyChangedMethodVisitor implements {@link IPropertyChanged} and invokes {@link PropertyChangeListener#propertyChanged} when a property is changed.
     * If the enhanced object implements {@link PropertyChangeListener} it is registered using
     * {@link IPropertyChanged#addPropertyChangeListener(PropertyChangeListener)}
     */
    public class NotifyPropertyChangedClassVisitor : ClassVisitor
    {
        public class MethodHandleValueResolver : IValueResolveDelegate
        {
            private readonly String propertyName;

            private readonly IPropertyInfoProvider propertyInfoProvider;

            public MethodHandleValueResolver(IPropertyInfoProvider propertyInfoProvider, String propertyName)
            {
                this.propertyInfoProvider = propertyInfoProvider;
                this.propertyName = propertyName;
            }

            public Type ValueType
            {
                get { return typeof(IPropertyInfo); }
            }

            public Object Invoke(string fieldName, Type enhancedType)
            {
                return propertyInfoProvider.GetProperty(enhancedType, propertyName);
            }
        }

        public static readonly Type templateType = typeof(PropertyChangeTemplate);

        protected static readonly String templatePropertyName = templateType.Name;

        public static readonly MethodInstance template_m_collectionChanged = new MethodInstance(null, typeof(INotifyCollectionChangedListener),
                "CollectionChanged", typeof(Object), typeof(NotifyCollectionChangedEventArgs));

        public static readonly MethodInstance template_m_PropertyChanged = new MethodInstance(null, typeof(IPropertyChangedEventHandler), "PropertyChanged", typeof(Object), typeof(PropertyChangedEventArgs));

        public static readonly MethodInstance template_m_onPropertyChanged = new MethodInstance(null, typeof(INotifyPropertyChangedSource),
            "OnPropertyChanged", typeof(String));

        public static readonly MethodInstance template_m_onPropertyChanged_Values = new MethodInstance(null, typeof(INotifyPropertyChangedSource),
            "OnPropertyChanged", typeof(String), typeof(Object), typeof(Object));

        public static readonly MethodInstance m_parentChildPropertyChange = new MethodInstance(null, templateType, "HandleParentChildPropertyChange", typeof(INotifyPropertyChangedSource), typeof(Object), typeof(PropertyChangedEventArgs));

        public static readonly MethodInstance m_handleCollectionChange = new MethodInstance(null, templateType, "HandleCollectionChange", typeof(INotifyPropertyChangedSource), typeof(Object), typeof(NotifyCollectionChangedEventArgs));

        protected static readonly MethodInstance m_newPropertyChangeSupport = new MethodInstance(null, templateType,
                "NewPropertyChangeSupport", typeof(Object));

        protected static readonly MethodInstance m_getMethodHandle = new MethodInstance(null, templateType, "GetMethodHandle", typeof(INotifyPropertyChangedSource),
            typeof(String));

        protected static readonly MethodInstance m_createParentChildEventHandler = new MethodInstance(null, templateType, "CreateParentChildEventHandler", typeof(Object));

        protected static readonly MethodInstance m_createCollectionEventHandler = new MethodInstance(null, templateType, "CreateCollectionEventHandler", typeof(Object));
        
        protected static readonly MethodInstance m_firePropertyChange = new MethodInstance(null, templateType, "FirePropertyChange",
                typeof(INotifyPropertyChangedSource), typeof(PropertyChangeSupport), typeof(IPropertyInfo), typeof(Object), typeof(Object));

        protected static readonly MethodInstance m_addPropertyChangeListener = new MethodInstance(null, templateType,
                "AddPropertyChangeListener", typeof(PropertyChangeSupport), typeof(PropertyChangedEventHandler));

        protected static readonly MethodInstance m_removePropertyChangeListener = new MethodInstance(null, templateType,
                "RemovePropertyChangeListener", typeof(PropertyChangeSupport), typeof(PropertyChangedEventHandler));

        public static readonly MethodInstance template_m_firePropertyChange = new MethodInstance(null, MethodAttributes.HideBySig | MethodAttributes.Family, "FirePropertyChange", typeof(void),
                typeof(PropertyChangeSupport), typeof(IPropertyInfo), typeof(Object), typeof(Object));

        protected static readonly MethodInstance template_m_getOrCreatePropertyChangeSupport = new MethodInstance(null, MethodAttributes.HideBySig | MethodAttributes.Public, "Use$PropertyChangeSupport",
            typeof(PropertyChangeSupport));

        public static readonly PropertyInstance p_propertyChangeSupport = new PropertyInstance(typeof(INotifyPropertyChangedSource).GetProperty("PropertyChangeSupport"));

        public static readonly PropertyInstance p_parentChildEventHandler = new PropertyInstance(typeof(INotifyPropertyChangedSource).GetProperty("ParentChildEventHandler"));

        public static readonly PropertyInstance p_collectionEventHandler = new PropertyInstance(typeof(INotifyPropertyChangedSource).GetProperty("CollectionEventHandler"));

        public static readonly MethodInstance sm_hasPropertyChangedDefault = new MethodInstance(null, templateType, "HasPropertyChangedDefault", typeof(Object), typeof(String), typeof(Object), typeof(Object));

        public static readonly MethodInstance sm_hasPropertyChangedValueType = new MethodInstance(null, templateType, "HasPropertyChangedValueType", typeof(Object), typeof(String), typeof(Object), typeof(Object));

        public static readonly HashMap<Type, MethodInstance> propertyTypeToHasPropertyChangeMI = new HashMap<Type, MethodInstance>();

        static NotifyPropertyChangedClassVisitor()
        {
            propertyTypeToHasPropertyChangeMI.Put(typeof(int), LookupHasPropertyChangeMI(typeof(int)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(long), LookupHasPropertyChangeMI(typeof(long)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(double), LookupHasPropertyChangeMI(typeof(double)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(float), LookupHasPropertyChangeMI(typeof(float)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(short), LookupHasPropertyChangeMI(typeof(short)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(byte), LookupHasPropertyChangeMI(typeof(byte)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(sbyte), LookupHasPropertyChangeMI(typeof(sbyte)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(char), LookupHasPropertyChangeMI(typeof(char)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(bool), LookupHasPropertyChangeMI(typeof(bool)));
            propertyTypeToHasPropertyChangeMI.Put(typeof(String), LookupHasPropertyChangeMI(typeof(String)));
        }

        public static PropertyInstance GetPropertyChangeTemplatePI(IClassVisitor cv)
        {
            PropertyInstance pi = State.GetProperty(templatePropertyName);
            if (pi != null)
            {
                return pi;
            }
            Object bean = State.BeanContext.GetService(templateType);
            return cv.ImplementAssignedReadonlyProperty(templatePropertyName, bean);
        }

        public static MethodInstance GetApplicableHasPropertyChangedOverload(Type propertyType)
        {
            MethodInstance method = propertyTypeToHasPropertyChangeMI.Get(propertyType);
            if (method != null)
            {
                return method;
            }
            if (propertyType.IsValueType)
            {
                return sm_hasPropertyChangedValueType;
            }
            return sm_hasPropertyChangedDefault;
        }

        public static bool IsBoxingNeededForHasPropertyChangedOverload(MethodInstance method, Type propertyType)
        {
            return propertyType.IsValueType && !method.Parameters[method.Parameters.Length - 1].Type.IsValueType;
        }

        protected static MethodInstance LookupHasPropertyChangeMI(Type propertyType)
        {
            return new MethodInstance(null, templateType, "HasPropertyChanged", typeof(Object), typeof(String), propertyType, propertyType);
        }

        /** property infos of enhanced type */
        protected readonly String[] properties;

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        public NotifyPropertyChangedClassVisitor(IClassVisitor cv, String[] properties)
            : base(cv)
        {
            this.properties = properties;
        }

        /**
         * {@inheritDoc}
         */
        public override void VisitEnd()
        {
            FieldInstance f_propertyChangeSupport = GetPropertyChangeSupportField();
            PropertyInstance p_propertyChangeTemplate = GetPropertyChangeTemplatePI(this);

            MethodInstance m_getPropertyChangeSupport = ImplementGetPropertyChangeSupport(p_propertyChangeTemplate, f_propertyChangeSupport);

            ImplementNotifyPropertyChanged(p_propertyChangeTemplate, m_getPropertyChangeSupport);

            MethodInstance m_firePropertyChange = ImplementFirePropertyChange(p_propertyChangeTemplate);

            ImplementNotifyPropertyChangedSource(p_propertyChangeTemplate, f_propertyChangeSupport);

            if (properties == null)
            {
                ImplementSelfAsListener();

                ImplementCollectionChanged(p_propertyChangeTemplate);
                ImplementPropertyChanged(p_propertyChangeTemplate);

                // handle all properties found
                IPropertyInfo[] props = PropertyInfoProvider.GetProperties(State.CurrentType);
                foreach (IPropertyInfo prop in props)
                {
                    if (prop.Name.EndsWith(ValueHolderIEC.GetNoInitSuffix()))
                    {
                        continue;
                    }
                    PropertyInstance propInfo = PropertyInstance.FindByTemplate(prop.Name, true);
                    if (propInfo == null)
                    {
                        continue;
                    }
                    ImplementPropertyChangeOnProperty(propInfo, p_propertyChangeTemplate, m_firePropertyChange, f_propertyChangeSupport);
                }
            }
            else
            {
                foreach (String propertyName in properties)
                {
                    PropertyInstance propInfo = PropertyInstance.FindByTemplate(propertyName, false);
                    ImplementPropertyChangeOnProperty(propInfo, p_propertyChangeTemplate, m_firePropertyChange, f_propertyChangeSupport);
                }
            }
            base.VisitEnd();
        }

        protected void ImplementSelfAsListener()
        {
            Type currentType = State.CurrentType;
            ConstructorInfo[] constructors = currentType.GetConstructors(BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static);

            foreach (ConstructorInfo constructor in constructors)
            {
                ConstructorInstance c_method = new ConstructorInstance(constructor);

                IMethodVisitor mg = VisitMethod(c_method);
                mg.LoadThis();
                mg.LoadArgs();
                mg.InvokeConstructor(c_method);

                // if (PropertyChangeListener.class.isAssignableFrom(getClass()))
                mg.IfThisInstanceOf(typeof(IPropertyChangedEventHandler), delegate(IMethodVisitor mv2)
                    {
                        MethodInstance m_addPropertyChangeListener = MethodInstance.FindByTemplate(false, "add_PropertyChanged", typeof(PropertyChangedEventHandler));

                        MethodInstance sm_createDelegate = new MethodInstance(null, typeof(Delegate), "CreateDelegate", typeof(Type), typeof(Object), typeof(String));

                        mv2.LoadThis();

                        mv2.Push(typeof(PropertyChangedEventHandler));
                        mv2.LoadThis();
                        mv2.Push("PropertyChanged");
                        mv2.InvokeStatic(sm_createDelegate);
                        mv2.InvokeVirtual(m_addPropertyChangeListener);
                        //
                        // mg.callThisGetter(m_usePropertyChangeSupport);
                        // fgfg
                        // mg.pop();
                    }
                , null);

                mg.ReturnValue();
                mg.EndMethod();
            }
        }

        protected void ImplementPropertyChangeOnProperty(PropertyInstance propertyInfo,
            PropertyInstance p_propertyChangeTemplate, MethodInstance m_firePropertyChange, FieldInstance f_propertyChangeSupport)
        {
            // add property change detection and notification
            if (propertyInfo.Getter == null || propertyInfo.Setter == null)
            {
                return;
            }
            PropertyInstance p_getterMethodHandle = ImplementAssignedReadonlyProperty(propertyInfo.Name + "$MethodHandle",
                new MethodHandleValueResolver(PropertyInfoProvider, propertyInfo.Name));
            Type propertyType = propertyInfo.PropertyType.Type;
            MethodInstance m_hasPropertyChanged = GetApplicableHasPropertyChangedOverload(propertyType);

            // check value type of last parameter
            bool isBoxingNeededForHasPropertyChanged = IsBoxingNeededForHasPropertyChangedOverload(m_hasPropertyChanged, propertyType);

            IMethodVisitor mg = VisitMethod(propertyInfo.Setter);
            Label l_finish = mg.NewLabel();
            Label l_noOldValue = mg.NewLabel();
            Label l_noChangeCheck = mg.NewLabel();
            LocalVariableInfo loc_oldValue;
            if (isBoxingNeededForHasPropertyChanged)
            {
                loc_oldValue = mg.NewLocal(typeof(Object));
            }
            else
            {
                loc_oldValue = mg.NewLocal(propertyType);
            }
            LocalVariableInfo loc_valueChanged = mg.NewLocal<bool>();

            MethodInstance m_getSuper = EnhancerUtil.GetSuperGetter(propertyInfo);
            bool relationProperty = m_getSuper.Name.EndsWith(ValueHolderIEC.GetNoInitSuffix());

            // initialize flag with false
            mg.Push(false);
            mg.StoreLocal(loc_valueChanged);

            // initialize oldValue with null
            mg.PushNullOrZero(loc_oldValue.LocalType);
            mg.StoreLocal(loc_oldValue);

            if (relationProperty)
            {
                // check if a setter call to an UNINITIALIZED relation occured with value null
                // if it the case there would be no PCE because oldValue & newValue are both null
                // but we need a PCE in this special case
                Label l_noSpecialHandling = mg.NewLabel();
                FieldInstance f_state = State.GetAlreadyImplementedField(ValueHolderIEC.GetInitializedFieldName(propertyInfo.Name));
                mg.GetThisField(f_state);
                mg.PushEnum(ValueHolderState.INIT);
                mg.IfCmp(typeof(ValueHolderState), CompareOperator.EQ, l_noSpecialHandling);
                mg.Push(true);
                mg.StoreLocal(loc_valueChanged);
                mg.Mark(l_noSpecialHandling);
            }

            // check if value should be checked to decide for a PCE
            mg.LoadLocal(loc_valueChanged);
            mg.IfZCmp(CompareOperator.NE, l_noOldValue);

            // get old field value calling super property getter
            mg.LoadThis();
            mg.InvokeOnExactOwner(m_getSuper);
            if (isBoxingNeededForHasPropertyChanged)
            {
                mg.Box(propertyType);
            }
            mg.StoreLocal(loc_oldValue);

            mg.Mark(l_noOldValue);

            // set new field value calling super property setter
            mg.LoadThis();
            mg.LoadArg(0);
            mg.InvokeOnExactOwner(EnhancerUtil.GetSuperSetter(propertyInfo));
            mg.PopIfReturnValue(EnhancerUtil.GetSuperSetter(propertyInfo));

            // check if value should be checked to decide for a PCE
            mg.LoadLocal(loc_valueChanged);
            mg.IfZCmp(CompareOperator.NE, l_noChangeCheck);

            LocalVariableInfo loc_newValue = null;
            if (isBoxingNeededForHasPropertyChanged)
            {
                loc_newValue = mg.NewLocal(typeof(Object)); // loc_1  Object newValue
                // Object loc_1 = (Object)value;
                mg.LoadArg(0);
                mg.Box(propertyType);
                mg.StoreLocal(loc_newValue);
            }
            mg.CallThisGetter(p_propertyChangeTemplate);
            // call HasPropertyChanged (static)
            mg.LoadThis();              // "this" as Object obj
            mg.Push(propertyInfo.Name);     // String propertyName
            mg.LoadLocal(loc_oldValue);
            if (loc_newValue != null)
            {
                mg.LoadLocal(loc_newValue);
            }
            else
            {
                mg.LoadArg(0);
            }
            mg.InvokeVirtual(m_hasPropertyChanged);
            //// if (!result)
            //// { return; }
            mg.IfZCmp(CompareOperator.EQ, l_finish);

            mg.Mark(l_noChangeCheck);
            // call firePropertyChange on this
            mg.LoadThis();
            // propertyChangeSupport
            mg.GetThisField(f_propertyChangeSupport);
            // property
            mg.CallThisGetter(p_getterMethodHandle);
            // oldValue
            mg.LoadLocal(loc_oldValue);
            if (!isBoxingNeededForHasPropertyChanged && propertyType.IsValueType)
            {
                // old value has not already been boxed but it is now necessary
                mg.Box(propertyType);
            }
            // newValue
            if (loc_newValue != null)
            {
                mg.LoadLocal(loc_newValue);
            }
            else
            {
                mg.LoadArg(0);
                if (propertyType.IsValueType)
                {
                    mg.Box(propertyType);
                }
            }
            // firePropertyChange(propertyChangeSupport, property, oldValue, newValue)
            mg.InvokeVirtual(m_firePropertyChange);

            // return
            mg.Mark(l_finish);
            mg.ReturnVoidOrThis();
            mg.EndMethod();
        }

        protected void ImplementCollectionChanged(PropertyInstance p_propertyChangeTemplate)
        {
            MethodInstance m_collectionChanged_super = MethodInstance.FindByTemplate(template_m_collectionChanged,
                    true);

            IMethodVisitor mv = VisitMethod(template_m_collectionChanged);
            if (m_collectionChanged_super != null)
            {
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeSuperOfCurrentMethod();
            }
            mv.CallThisGetter(p_propertyChangeTemplate);
            mv.LoadThis();
            mv.LoadArgs();
            // call PCT.HandleCollectionChange(this, sender, arg)
            mv.InvokeVirtual(m_handleCollectionChange);
            mv.ReturnValue();
            mv.EndMethod();
        }

        protected void ImplementPropertyChanged(PropertyInstance p_propertyChangeTemplate)
        {
            MethodInstance m_propertyChanged_internal_super = MethodInstance.FindByTemplate(template_m_PropertyChanged, true);
            IMethodVisitor mv = VisitMethod(template_m_PropertyChanged);
            if (m_propertyChanged_internal_super != null)
            {
                mv.LoadThis();
                mv.LoadArgs();
                mv.InvokeSuperOfCurrentMethod();
            }
            mv.CallThisGetter(p_propertyChangeTemplate);
            mv.LoadThis();
            mv.LoadArgs();
            // call PCT.HandleParentChildPropertyChange(this, sender, arg)
            mv.InvokeVirtual(m_parentChildPropertyChange);
            mv.ReturnValue();
            mv.EndMethod();
        }

        /**
         * Almost empty implementation (just calling the static method) to be able to override and act on the property change
         */
        protected MethodInstance ImplementFirePropertyChange(PropertyInstance p_propertyChangeTemplate)
        {
            MethodInstance existingMethod = MethodInstance.FindByTemplate(template_m_firePropertyChange, true);
            if (existingMethod != null)
            {
                return existingMethod;
            }
            IMethodVisitor mg = VisitMethod(template_m_firePropertyChange);

            mg.CallThisGetter(p_propertyChangeTemplate);
            mg.LoadThis();
            mg.LoadArgs();
            // firePropertyChange(thisPointer, propertyChangeSupport, property, oldValue, newValue)
            mg.InvokeVirtual(m_firePropertyChange);
            mg.PopIfReturnValue(m_firePropertyChange);

            mg.ReturnVoidOrThis();
            mg.EndMethod();

            return mg.Method;
        }

        protected FieldInstance GetPropertyChangeSupportField()
        {
            FieldInstance f_propertyChangeSupport = State.GetAlreadyImplementedField("f_propertyChangeSupport");
            if (f_propertyChangeSupport == null)
            {
                f_propertyChangeSupport = new FieldInstance(FieldAttributes.Family, "f_propertyChangeSupport", typeof(PropertyChangeSupport));
            }
            return f_propertyChangeSupport;
        }

        protected MethodInstance ImplementGetPropertyChangeSupport(PropertyInstance p_propertyChangeTemplate, FieldInstance f_propertyChangeSupport)
        {
            MethodInstance m_getPropertyChangeSupport = MethodInstance.FindByTemplate(template_m_getOrCreatePropertyChangeSupport, true);

            if (m_getPropertyChangeSupport == null)
            {
                // create field that holds propertyChangeSupport
                f_propertyChangeSupport = ImplementField(f_propertyChangeSupport);
                IMethodVisitor mg = VisitMethod(template_m_getOrCreatePropertyChangeSupport);
                HideFromDebug(mg.Method);
                Label l_pcsValid = mg.NewLabel();
                mg.GetThisField(f_propertyChangeSupport);
                mg.Dup();
                mg.IfNonNull(l_pcsValid);

                mg.Pop(); // remove 2nd null instance from stack caused by previous dup
                mg.PutThisField(f_propertyChangeSupport, delegate(IMethodVisitor mg2)
                {
                    mg.CallThisGetter(p_propertyChangeTemplate);
                    mg.LoadThis();
                    mg.InvokeVirtual(m_newPropertyChangeSupport);
                });
                mg.GetThisField(f_propertyChangeSupport);

                mg.Mark(l_pcsValid);
                mg.ReturnValue(); // return instance already on the stack by both branches
                mg.EndMethod();

                m_getPropertyChangeSupport = mg.Method;
            }
            return m_getPropertyChangeSupport;
        }

        protected PropertyInstance ImplementNotifyPropertyChangedSource(PropertyInstance p_propertyChangeTemplate,
            FieldInstance f_propertyChangeSupport)
        {
            MethodInstance m_onPropertyChanged_Values = MethodInstance.FindByTemplate(template_m_onPropertyChanged_Values, true);
            if (m_onPropertyChanged_Values == null)
            {
                IMethodVisitor mv = VisitMethod(template_m_onPropertyChanged_Values);
                mv.CallThisGetter(p_propertyChangeTemplate);
                mv.LoadThis();
                mv.GetThisField(f_propertyChangeSupport);

                // getMethodHandle(sender, propertyName)
                mv.CallThisGetter(p_propertyChangeTemplate);
                mv.LoadThis();
                mv.LoadArg(0);
                mv.InvokeVirtual(m_getMethodHandle);

                mv.LoadArg(0);
                mv.LoadArg(1);
                // firePropertyChange(sender, propertyChangeSupport, property, oldValue, newValue)
                mv.InvokeVirtual(m_firePropertyChange);
                mv.PopIfReturnValue(m_firePropertyChange);
                mv.ReturnVoidOrThis();
                mv.EndMethod();
                m_onPropertyChanged_Values = mv.Method;
            }
            MethodInstance m_onPropertyChanged = MethodInstance.FindByTemplate(template_m_onPropertyChanged, true);
            if (m_onPropertyChanged == null)
            {
                IMethodVisitor mv = VisitMethod(template_m_onPropertyChanged);
                mv.LoadThis();
                mv.LoadArg(0);
                mv.PushNull();
                mv.PushNull();
                mv.InvokeVirtual(m_onPropertyChanged_Values);
                mv.PopIfReturnValue(m_onPropertyChanged_Values);
                mv.ReturnVoidOrThis();
                mv.EndMethod();
                m_onPropertyChanged = mv.Method;
            }
            PropertyInstance p_pceHandlers = PropertyInstance.FindByTemplate(p_propertyChangeSupport, true);
            if (p_pceHandlers == null)
            {
                HideFromDebug(ImplementGetter(p_propertyChangeSupport.Getter, f_propertyChangeSupport));
                p_pceHandlers = PropertyInstance.FindByTemplate(p_propertyChangeSupport, false);
            }
            if (EmbeddedEnhancementHint.HasMemberPath(State.Context))
            {
                PropertyInstance p_parentEntity = EmbeddedTypeVisitor.GetParentObjectProperty(this);
                if (MethodInstance.FindByTemplate(p_parentChildEventHandler.Getter, true) == null)
                {
                    IMethodVisitor mv = VisitMethod(p_parentChildEventHandler.Getter);
                    mv.CallThisGetter(p_parentEntity);
                    mv.InvokeInterface(p_parentChildEventHandler.Getter);
                    mv.ReturnValue();
                    mv.EndMethod();
                    HideFromDebug(mv.Method);
                }
                if (MethodInstance.FindByTemplate(p_collectionEventHandler.Getter, true) == null)
                {
                    IMethodVisitor mv = VisitMethod(p_collectionEventHandler.Getter);
                    mv.CallThisGetter(p_parentEntity);
                    mv.InvokeInterface(p_collectionEventHandler.Getter);
                    mv.ReturnValue();
                    mv.EndMethod();
                    HideFromDebug(mv.Method);
                }
            }
            else
            {
                if (MethodInstance.FindByTemplate(p_parentChildEventHandler.Getter, true) == null)
                {
                    HideFromDebug(ImplementLazyInitProperty(p_parentChildEventHandler, delegate(IMethodVisitor mv)
                    {
                        mv.CallThisGetter(p_propertyChangeTemplate);
                        mv.LoadThis();
                        mv.InvokeVirtual(m_createParentChildEventHandler);
                    }));
                }
                if (MethodInstance.FindByTemplate(p_collectionEventHandler.Getter, true) == null)
                {
                    HideFromDebug(ImplementLazyInitProperty(p_collectionEventHandler, delegate(IMethodVisitor mv)
                    {
                        mv.CallThisGetter(p_propertyChangeTemplate);
                        mv.LoadThis();
                        mv.InvokeVirtual(m_createCollectionEventHandler);
                    }));
                }
            }

            //MethodAttributes ma = MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig;
            //{
            //    ConstructorInfo pceaCI = typeof(PropertyChangedEventArgs).GetConstructor(new Type[] { typeof(String) });

            //    MethodBuilder mb = VisitorUtil.DefineMethod(vs, onPropertyChangedMI_string, ma);
            //    ILGenerator gen = mb.GetILGenerator();
            //    gen.Emit(OpCodes.Ldarg_0);
            //    gen.Emit(OpCodes.Ldarg_1);
            //    gen.Emit(OpCodes.Newobj, pceaCI);
            //    gen.Emit(OpCodes.Call, onPropertyChangedMI_pceArg);
            //    gen.Emit(OpCodes.Ret);
            //}
            //{
            //    MethodBuilder mb = VisitorUtil.DefineMethod(vs, onPropertyChangedMI_pceArg, ma);
            //    ILGenerator gen = mb.GetILGenerator();
            //    gen.Emit(OpCodes.Ldarg_0);
            //    gen.Emit(OpCodes.Call, pctPI.GetGetMethod());
            //    gen.Emit(OpCodes.Ldarg_0);
            //    gen.Emit(OpCodes.Ldarg_1);
            //    gen.Emit(OpCodes.Call, FirePropertyChangedMI);
            //    gen.Emit(OpCodes.Ret);
            //}
            //    List<PropertyChangedEventHandler> PropertyChangedEventHandlers { get; }

            //void OnPropertyChanged(String propertyName);

            //void OnPropertyChanged(PropertyChangedEventArgs args);
            return p_pceHandlers;
        }

        protected void ImplementNotifyPropertyChanged(PropertyInstance p_propertyChangeTemplate, MethodInstance m_getPropertyChangeSupport)
        {
            // implement IPropertyChanged
            foreach (MethodInfo rMethod in typeof(INotifyPropertyChanged).GetMethods())
            {
                MethodInstance existingMethod = MethodInstance.FindByTemplate(rMethod, true);
                if (existingMethod != null)
                {
                    continue;
                }
                MethodInstance method = new MethodInstance(rMethod);

                IMethodVisitor mg = VisitMethod(method);
                mg.CallThisGetter(p_propertyChangeTemplate);
                // this.propertyChangeSupport
                mg.CallThisGetter(m_getPropertyChangeSupport);
                // listener
                mg.LoadArg(0);
                if ("add_PropertyChanged".Equals(method.Name))
                {
                    // addPropertyChangeListener(propertyChangeSupport, listener)
                    mg.InvokeVirtual(m_addPropertyChangeListener);
                }
                else
                {
                    // removePropertyChangeListener(propertyChangeSupport, listener)
                    mg.InvokeVirtual(m_removePropertyChangeListener);
                }
                mg.ReturnValue();
                mg.EndMethod();
            }
        }
    }
}