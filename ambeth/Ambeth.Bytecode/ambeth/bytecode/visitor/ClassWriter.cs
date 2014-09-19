using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Bytecode.Core;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System;
using System.Diagnostics;
using System.Reflection;
using System.Reflection.Emit;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class ClassWriter : IClassVisitor
    {
        public static readonly ConstructorInfo c_hideFromDebug = typeof(DebuggerBrowsableAttribute).GetConstructor(new Type[] { typeof(DebuggerBrowsableState) });

        public static readonly ConstructorInfo c_ftopc = typeof(FireThisOnPropertyChange).GetConstructor(new Type[] { typeof(String) });

        public static readonly ConstructorInstance c_obj = new ConstructorInstance(typeof(Object).GetConstructor(Type.EmptyTypes));

        [LogInstance]
        public ILogger Log { private get; set; }

        protected TypeBuilder tb;

        protected AmbethClassLoader ambethClassLoader;

        protected Type createdType;

        protected NewType newType;

        protected StringBuilder sb;

        public ClassWriter(AmbethClassLoader ambethClassLoader, StringBuilder sb)
        {
            this.ambethClassLoader = ambethClassLoader;
            this.sb = sb;
        }

        protected bool HideFromDebugActive
        {
            get
            {
                return true;
            }
        }

        protected IBytecodeBehaviorState State
        {
            get
            {
                return BytecodeBehaviorState.State;
            }
        }

        //public PropertyBuilder DefineFireThisOnPropertyChange(PropertyBuilder pb, params String[] propertyNames)
        //{
        //    foreach (String propertyName in propertyNames)
        //    {
        //        CustomAttributeBuilder caBuilder = new CustomAttributeBuilder(ftopcCI, new Object[] { propertyName });

        //        pb.SetCustomAttribute(caBuilder);
        //    }
        //    return pb;
        //}

        public virtual FieldInfo HideFromDebug(FieldInfo fieldInfo)
        {
            if (HideFromDebugActive)
            {
                ((FieldBuilder)fieldInfo).SetCustomAttribute(new CustomAttributeBuilder(c_hideFromDebug, new Object[] { DebuggerBrowsableState.Never }));
            }
            return fieldInfo;
        }

        public virtual FieldInstance HideFromDebug(FieldInstance field)
        {
            if (HideFromDebugActive)
            {
                ((FieldBuilder)field.Field).SetCustomAttribute(new CustomAttributeBuilder(c_hideFromDebug, new Object[] { DebuggerBrowsableState.Never }));
            }
            return field;
        }

        public virtual MethodInstance HideFromDebug(MethodInstance method)
        {
            if (HideFromDebugActive)
            {
                ((MethodBuilder)method.Method).SetCustomAttribute(new CustomAttributeBuilder(c_hideFromDebug, new Object[] { DebuggerBrowsableState.Never }));
            }
            return method;
        }

        public virtual PropertyInfo HideFromDebug(PropertyInfo propertyInfo)
        {
            if (HideFromDebugActive)
            {
                ((PropertyBuilder)propertyInfo).SetCustomAttribute(new CustomAttributeBuilder(c_hideFromDebug, new Object[] { DebuggerBrowsableState.Never }));
            }
            return propertyInfo;
        }

        public virtual PropertyInstance HideFromDebug(PropertyInstance property)
        {
            if (HideFromDebugActive)
            {
                property.AddAnnotation(c_hideFromDebug, DebuggerBrowsableState.Never);
            }
            return property;
        }

        public void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments)
        {
            tb.SetCustomAttribute(new CustomAttributeBuilder(annotationConstructor, arguments));
        }

        public virtual IMethodVisitor VisitConstructor(MethodAttributes access, params Type[] parameters)
        {
            return VisitMethod(access, ConstructorInstance.CONSTRUCTOR_NAME, typeof(void), parameters);
        }

        public virtual IMethodVisitor VisitConstructor(MethodAttributes access, params NewType[] parameters)
        {
            return VisitMethod(access, ConstructorInstance.CONSTRUCTOR_NAME, NewType.VOID_TYPE, parameters);
        }

        public virtual IFieldVisitor VisitField(FieldInstance field)
        {
            if (Log.DebugEnabled)
            {
                Log.Debug("Implement field: " + field.ToString());
            }
            FieldBuilder fb = tb.DefineField(field.Name, field.Type.Type, field.Access);
            ((BytecodeBehaviorState)State).FieldImplemented(new FieldInstance(fb));
            return new FieldVisitor(fb);
        }

        public virtual IMethodVisitor VisitMethod(MethodAttributes access, String name, Type returnType, params Type[] parameters)
        {
            IBytecodeBehaviorState state = State;
            return VisitMethod(new MethodInstance(state.NewType.Type, access, returnType, name, parameters));
        }

        public virtual IMethodVisitor VisitMethod(MethodAttributes access, String name, NewType returnType, params NewType[] parameters)
        {
            IBytecodeBehaviorState state = State;
            return VisitMethod(new MethodInstance(state.NewType, access, returnType, name, parameters));
        }

        public virtual IMethodVisitor VisitMethod(MethodInstance method)
        {
            if (Log.DebugEnabled)
            {
                Log.Debug("Implement method: " + method.ToString());
            }
            NewType owner = State.NewType;
            method = method.DeriveOwner();
            Type[] parameters = new Type[method.Parameters.Length];
            for (int a = parameters.Length; a-- > 0; )
            {
                parameters[a] = method.Parameters[a].Type;
            }
            MethodAttributes access = method.Access;
            if (ConstructorInstance.CONSTRUCTOR_NAME.Equals(method.Name))
            {
                ConstructorBuilder mb = tb.DefineConstructor(access, access.HasFlag(MethodAttributes.Static) ? CallingConventions.Standard : CallingConventions.HasThis, parameters);
                ((BytecodeBehaviorState)State).MethodImplemented(new ConstructorInstance(newType, mb, method.Parameters));
                sb.Append("\r\n" + method.ToString());
                return new MethodWriter(mb, new ConstructorInstance(owner, mb, method.Parameters), sb);
            }
            else
            {
                PropertyInstance propertyInfo = null;
                Object eventInfo = null;
                if (!access.HasFlag(MethodAttributes.Static))
                {
                    access |= MethodAttributes.Virtual;
                }
                access |= MethodAttributes.HideBySig;
                access |= MethodAttributes.ReuseSlot;
                access &= ~MethodAttributes.VtableLayoutMask;
                String propertyName = null, eventName = null;
                Type propertyType = null;
                if (method.Name.StartsWith("get_") && method.Parameters.Length == 0)
                {
                    propertyName = method.Name.Substring(4);
                    propertyType = method.ReturnType.Type;
                }
                else if (method.Name.StartsWith("set_") && method.Parameters.Length == 1)
                {
                    propertyName = method.Name.Substring(4);
                    propertyType = method.Parameters[0].Type;
                }
                else if (method.Name.StartsWith("add_") && method.Parameters.Length == 1)
                {
                    eventName = method.Name.Substring(4);
                    propertyType = method.Parameters[0].Type;
                }
                else if (method.Name.StartsWith("remove_") && method.Parameters.Length == 1)
                {
                    eventName = method.Name.Substring(7);
                    propertyType = method.Parameters[0].Type;
                }
                if (propertyName != null)
                {
                    propertyInfo = State.GetProperty(propertyName, NewType.GetType(propertyType));
                    if (propertyInfo == null)
                    {
#if SILVERLIGHT
                        PropertyInfo pi = tb.DefineProperty(propertyName, PropertyAttributes.None, propertyType, null);
#else
                        CallingConventions cc = access.HasFlag(MethodAttributes.Static) ? CallingConventions.Standard : CallingConventions.HasThis;

                        PropertyInfo pi = tb.DefineProperty(propertyName, PropertyAttributes.None, cc, propertyType, null);
#endif
                        propertyInfo = new PropertyInstance(pi);
                        ((BytecodeBehaviorState)State).PropertyImplemented(propertyInfo);
                    }
                }
                else if (eventName != null)
                {
                    eventInfo = ((BytecodeBehaviorState)State).GetAlreadyImplementedEvent(eventName);
                    if (eventInfo == null)
                    {
                        CallingConventions cc = access.HasFlag(MethodAttributes.Static) ? CallingConventions.Standard : CallingConventions.HasThis;
                        EventBuilder eb = tb.DefineEvent(eventName, EventAttributes.None, propertyType);
                        ((BytecodeBehaviorState)State).EventImplemented(eventName, eb);
                        eventInfo = eb;
                    }
                }
                MethodBuilder mb = tb.DefineMethod(method.Name, access, access.HasFlag(MethodAttributes.Static) ? CallingConventions.Standard : CallingConventions.HasThis, method.ReturnType.Type, parameters);
                //MethodInfo parentMI = State.CurrentType.GetMethod(method.Name, BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Static | BindingFlags.FlattenHierarchy);
                //if (parentMI != null)
                //{
                //    tb.DefineMethodOverride(mb, parentMI);
                //}
                method = new MethodInstance(newType, mb, method.Parameters);
                ((BytecodeBehaviorState)State).MethodImplemented(method);

                if (propertyInfo != null && propertyInfo.Configurable)
                {
                    if (method.Name.StartsWith("get_"))
                    {
                        propertyInfo.Getter = method;
                    }
                    else if (method.Name.StartsWith("set_"))
                    {
                        propertyInfo.Setter = method;
                    }
                    else
                    {
                        throw new ArgumentException();
                    }
                }
                if (eventInfo != null && eventInfo is EventBuilder)
                {
                    EventBuilder eb = (EventBuilder)eventInfo;
                    if (method.Name.StartsWith("add_"))
                    {
                        eb.SetAddOnMethod(mb);
                    }
                    else if (method.Name.StartsWith("remove_"))
                    {
                        eb.SetRemoveOnMethod(mb);
                    }
                    else
                    {
                        throw new ArgumentException();
                    }
                }
                sb.Append("\r\n" + method.ToString());
                return new MethodWriter(mb, method, sb);
            }
        }

        public virtual FieldInstance ImplementStaticAssignedField(String staticFieldName, Object fieldValue)
        {
            ParamChecker.AssertParamNotNull(fieldValue, "fieldValue");
            Type fieldType = fieldValue.GetType();
            if (fieldValue is IValueResolveDelegate)
            {
                fieldType = ((IValueResolveDelegate)fieldValue).ValueType;
            }
            return ImplementStaticAssignedField(staticFieldName, fieldType, fieldValue);
        }

        public virtual FieldInstance ImplementStaticAssignedField(String staticFieldName, Type fieldType, Object fieldValue)
        {
            FieldInstance field = new FieldInstance(FieldAttributes.Public | FieldAttributes.Static, staticFieldName, NewType.GetType(fieldType));
            field = ImplementField(field, null);
            field = HideFromDebug(field);
            if (fieldValue != null)
            {
                IValueResolveDelegate vrd = null;
                if (fieldValue is IValueResolveDelegate)
                {
                    vrd = (IValueResolveDelegate)fieldValue;
                }
                else
                {
                    vrd = new NoOpValueResolveDelegate(fieldValue);
                }
                ((BytecodeBehaviorState)State).QueueFieldInitialization(field.Name, vrd);
            }
            return field;
        }

        public PropertyInstance ImplementAssignedReadonlyProperty(String propertyName, Object fieldValue)
        {
            ParamChecker.AssertParamNotNull(propertyName, "propertyName");
            ParamChecker.AssertParamNotNull(fieldValue, "fieldValue");
            FieldInstance field = ImplementStaticAssignedField("sf_" + propertyName, fieldValue);
            MethodInstance getter = new MethodInstance(State.NewType, MethodAttributes.Public | MethodAttributes.Static, field.Type, "get_" + propertyName);
            getter = HideFromDebug(ImplementGetter(getter, field));
            PropertyInstance property = State.GetProperty(propertyName, field.Type);
            if (property == null)
            {
                throw new Exception("Should never happen");
            }
            return property;
        }

        public virtual FieldInstance ImplementField(FieldInstance field)
        {
            ParamChecker.AssertParamNotNull(field, "field");
            return ImplementField(field, null);
        }

        public virtual FieldInstance ImplementField(FieldInstance field, FScript script)
        {
            ParamChecker.AssertParamNotNull(field, "field");
            IFieldVisitor fv = VisitField(field);
            if (script != null)
            {
                script.Invoke(fv);
            }
            fv.VisitEnd();
            return State.GetAlreadyImplementedField(field.Name);
        }

        public virtual MethodInstance ImplementSetter(MethodInstance method, FieldInstance field)
        {
            ParamChecker.AssertParamNotNull(method, "method");
            ParamChecker.AssertParamNotNull(field, "field");
            IMethodVisitor mg = VisitMethod(method);
            mg.PutThisField(field, delegate(IMethodVisitor mg2)
            {
                mg2.LoadArg(0);
            });
            mg.ReturnVoidOrThis();
            mg.EndMethod();
            return MethodInstance.FindByTemplate(method, false);
        }

        public virtual PropertyInstance ImplementSetter(PropertyInstance property, FieldInstance field)
        {
            ParamChecker.AssertParamNotNull(property, "property");
            ParamChecker.AssertParamNotNull(field, "field");
            MethodInstance setter = property.Setter;
            if (setter == null)
            {
                setter = new MethodInstance(State.NewType, MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig | MethodAttributes.SpecialName,
                    NewType.VOID_TYPE, "set_" + property.Name, property.PropertyType);
            }
            ImplementSetter(setter, field);
            return PropertyInstance.FindByTemplate(property, false);
        }

        public virtual MethodInstance ImplementGetter(MethodInstance method, FieldInstance field)
        {
            IMethodVisitor mg = VisitMethod(method);
            mg.GetThisField(field);
            mg.ReturnValue();
            mg.EndMethod();
            return MethodInstance.FindByTemplate(method, false);
        }

        public virtual PropertyInstance ImplementGetter(PropertyInstance property, FieldInstance field)
        {
            MethodInstance getter = property.Getter;
            if (getter == null)
            {
                getter = new MethodInstance(State.NewType, MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig | MethodAttributes.SpecialName,
                    property.PropertyType, "get_" + property.Name);
            }
            ImplementGetter(getter, field);
            return PropertyInstance.FindByTemplate(property, false);
        }

        public virtual PropertyInstance ImplementLazyInitProperty(PropertyInstance property, Script script, params String[] fireThisOnPropertyNames)
        {
            FieldInstance field = ImplementField(new FieldInstance(FieldAttributes.Private, "f_" + property.Name, property.PropertyType));
            IMethodVisitor mv = VisitMethod(property.Getter);
            Label returnInstance = mv.NewLabel();
            mv.GetThisField(field);
            mv.IfNonNull(returnInstance);
            mv.PutThisField(field, script);
            mv.Mark(returnInstance);
            mv.GetThisField(field);
            mv.ReturnValue();
            mv.EndMethod();
            return FireThisOnPropertyChange(property, fireThisOnPropertyNames);
        }

        public virtual PropertyInstance ImplementProperty(PropertyInstance property, Script getterScript, Script setterScript)
        {
            if (getterScript != null)
            {
                IMethodVisitor mv = VisitMethod(property.Getter);
                getterScript.Invoke(mv);
                mv.EndMethod();
            }
            if (setterScript != null)
            {
                IMethodVisitor mv = VisitMethod(property.Setter);
                setterScript.Invoke(mv);
                mv.EndMethod();
            }
            return PropertyInstance.FindByTemplate(property, false);
        }

        public virtual PropertyInstance FireThisOnPropertyChange(PropertyInstance property, params String[] propertyNames)
        {
            property = State.GetProperty(property.Name, property.PropertyType);
            foreach (String propertyName in propertyNames)
            {
                property.AddAnnotation(c_ftopc, propertyName);
            }
            return property;
        }

        public virtual void OverrideConstructors(IOverrideConstructorDelegate overrideConstructorDelegate)
        {
            if (State.CurrentType.IsInterface)
            {
                overrideConstructorDelegate.Invoke(this, c_obj);
                return;
            }
            ConstructorInfo[] constructors = State.CurrentType.GetConstructors();
            foreach (ConstructorInfo superConstructor in constructors)
            {
                overrideConstructorDelegate.Invoke(this, new ConstructorInstance(superConstructor));
            }
        }

        public virtual IMethodVisitor StartOverrideWithSuperCall(MethodInstance superMethod)
        {
            IBytecodeBehaviorState state = State;

            NewType superType = NewType.GetType(state.CurrentType);
            if (!superType.Equals(superMethod.Owner))
            {
                throw new ArgumentException("Not a method of " + state.CurrentType + ": " + superMethod);
            }
            IMethodVisitor mg = VisitMethod(superMethod);

            mg.LoadThis();
            mg.LoadArgs();
            mg.InvokeSuper(superMethod);

            return mg;
        }

        public MethodInstance ImplementSwitchByIndex(MethodInstance method, String exceptionMessageOnIllegalIndex, int indexSize, ScriptWithIndex script)
        {
            IMethodVisitor mv = VisitMethod(method);

            if (indexSize == 0)
            {
                mv.ThrowException(typeof(ArgumentException), exceptionMessageOnIllegalIndex);
                mv.PushNull();
                mv.ReturnValue();
                mv.EndMethod();
                return mv.Method;
            }

            Label l_default = mv.NewLabel();
            Label[] l_fields = new Label[indexSize];
            for (int index = 0, size = indexSize; index < size; index++)
            {
                l_fields[index] = mv.NewLabel();
            }

            mv.LoadArg(0);
            mv.VisitTableSwitchInsn(0, l_fields.Length - 1, l_default, l_fields);

            for (int index = 0, size = l_fields.Length; index < size; index++)
            {
                mv.Mark(l_fields[index]);

                script(mv, index);
            }
            mv.Mark(l_default);

            mv.ThrowException(typeof(ArgumentException), "Given relationIndex not known");
            mv.PushNull();
            mv.ReturnValue();
            mv.EndMethod();
            return mv.Method;
        }

        public void Visit(TypeAttributes access, String name, Type superName, Type[] interfaces)
        {
            tb = ambethClassLoader.CreateNewType(access, name, superName, interfaces);
            newType = NewType.GetType(tb);
            ambethClassLoader = null;
        }

        public void VisitEnd()
        {
            createdType = tb.CreateType();
            tb = null;
        }

        public TypeBuilder GetTypeBuilder()
        {
            return tb;
        }

        public Type GetCreatedType()
        {
            return createdType;
        }
    }
}
