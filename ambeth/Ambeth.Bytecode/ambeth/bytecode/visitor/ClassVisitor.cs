using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Reflection.Emit;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public abstract class ClassVisitor : IClassVisitor
    {
        public static readonly ConstructorInfo c_fireThisOPC = typeof(FireThisOnPropertyChange).GetConstructor(new Type[] { typeof(String) });

        public static readonly ConstructorInfo c_fireTargetOPC = typeof(FireTargetOnPropertyChange).GetConstructor(new Type[] { typeof(String) });

        private readonly IClassVisitor cv;

        public ClassVisitor(IClassVisitor cv)
        {
            this.cv = cv;
        }

        protected static IBytecodeBehaviorState State
        {
            get
            {
                return BytecodeBehaviorState.State;
            }
        }

        public virtual FieldInfo HideFromDebug(FieldInfo fieldInfo)
        {
            if (cv != null)
            {
                return cv.HideFromDebug(fieldInfo);
            }
            return fieldInfo;
        }

        public virtual FieldInstance HideFromDebug(FieldInstance fieldInfo)
        {
            if (cv != null)
            {
                return cv.HideFromDebug(fieldInfo);
            }
            return fieldInfo;
        }

        public virtual MethodInstance HideFromDebug(MethodInstance method)
        {
            if (cv != null)
            {
                return cv.HideFromDebug(method);
            }
            return method;
        }

        public virtual PropertyInfo HideFromDebug(PropertyInfo propertyInfo)
        {
            if (cv != null)
            {
                return cv.HideFromDebug(propertyInfo);
            }
            return propertyInfo;
        }

        public virtual PropertyInstance HideFromDebug(PropertyInstance property)
        {
            if (cv != null)
            {
                return cv.HideFromDebug(property);
            }
            return property;
        }

        public virtual IMethodVisitor VisitConstructor(MethodAttributes access, params Type[] parameters)
        {
            if (cv != null)
            {
                return cv.VisitConstructor(access, parameters);
            }
            return null;
        }

        public virtual void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments)
        {
            if (cv != null)
            {
                cv.VisitAnnotation(annotationConstructor, arguments);
            }
        }

        public virtual IMethodVisitor VisitConstructor(MethodAttributes access, params NewType[] parameters)
        {
            if (cv != null)
            {
                return cv.VisitConstructor(access, parameters);
            }
            return null;
        }

        public virtual IFieldVisitor VisitField(FieldInstance field)
        {
            if (cv != null)
            {
                return cv.VisitField(field);
            }
            return null;
        }

        public virtual IMethodVisitor VisitMethod(MethodInstance method)
        {
            if (cv != null)
            {
                return cv.VisitMethod(method);
            }
            return null;
        }

        public virtual IMethodVisitor VisitMethod(MethodAttributes access, String name, NewType returnType, params NewType[] parameters)
        {
            return VisitMethod(new MethodInstance(State.NewType, access, returnType, name, parameters));
        }

        public virtual FieldInstance ImplementStaticAssignedField(String staticFieldName, Object fieldValue)
        {
            if (cv != null)
            {
                return cv.ImplementStaticAssignedField(staticFieldName, fieldValue);
            }
            return null;
        }

        public virtual FieldInstance ImplementStaticAssignedField(String staticFieldName, Type fieldType, Object fieldValue)
        {
            if (cv != null)
            {
                return cv.ImplementStaticAssignedField(staticFieldName, fieldType, fieldValue);
            }
            return null;
        }

        public virtual PropertyInstance ImplementAssignedReadonlyProperty(String propertyName, Object fieldValue)
        {
            if (cv != null)
            {
                return cv.ImplementAssignedReadonlyProperty(propertyName, fieldValue);
            }
            return null;
        }

        public virtual FieldInstance ImplementField(FieldInstance field)
        {
            if (cv != null)
            {
                return cv.ImplementField(field);
            }
            return null;
        }

        public virtual FieldInstance ImplementField(FieldInstance field, FScript script)
        {
            if (cv != null)
            {
                return cv.ImplementField(field, script);
            }
            return null;
        }

        public virtual MethodInstance ImplementSetter(MethodInstance method, FieldInstance field)
        {
            if (cv != null)
            {
                return cv.ImplementSetter(method, field);
            }
            return null;
        }

        public virtual PropertyInstance ImplementSetter(PropertyInstance property, FieldInstance field)
        {
            if (cv != null)
            {
                return cv.ImplementSetter(property, field);
            }
            return null;
        }

        public virtual MethodInstance ImplementGetter(MethodInstance method, FieldInstance field)
        {
            if (cv != null)
            {
                return cv.ImplementGetter(method, field);
            }
            return null;
        }

        public virtual PropertyInstance ImplementGetter(PropertyInstance property, FieldInstance field)
        {
            if (cv != null)
            {
                return cv.ImplementGetter(property, field);
            }
            return null;
        }

        public virtual PropertyInstance ImplementLazyInitProperty(PropertyInstance property, Script script, params String[] fireThisOnPropertyNames)
        {
            if (cv != null)
            {
                return cv.ImplementLazyInitProperty(property, script, fireThisOnPropertyNames);
            }
            return null;
        }

        public virtual PropertyInstance ImplementProperty(PropertyInstance property, Script getterScript, Script setterScript)
        {
            if (cv != null)
            {
                return cv.ImplementProperty(property, getterScript, setterScript);
            }
            return null;
        }

        public virtual void OverrideConstructors(IOverrideConstructorDelegate overrideConstructorDelegate)
        {
            if (cv != null)
            {
                cv.OverrideConstructors(overrideConstructorDelegate);
            }
        }

        public virtual IMethodVisitor StartOverrideWithSuperCall(MethodInstance superMethod)
        {
            if (cv != null)
            {
                return cv.StartOverrideWithSuperCall(superMethod);
            }
            return null;
        }

        public MethodInstance ImplementSwitchByIndex(MethodInstance method, String exceptionMessageOnIllegalIndex, int indexSize, ScriptWithIndex script)
	    {
            if (cv != null)
            {
                return cv.ImplementSwitchByIndex(method, exceptionMessageOnIllegalIndex, indexSize, script);
            }
            return null;
	    }

        public virtual void Visit(TypeAttributes access, String name, Type superName, Type[] interfaces)
        {
            if (cv != null)
            {
                cv.Visit(access, name, superName, interfaces);
            }
        }

        public virtual void VisitEnd()
        {
            if (cv != null)
            {
                cv.VisitEnd();
            }
        }
    }
}
