using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class TraceClassVisitor : ClassVisitor
    {
        protected readonly StringBuilder sb;

        public TraceClassVisitor(IClassVisitor cv, StringBuilder sb)
            : base(cv)
        {
            this.sb = sb;
        }

        public override FieldInfo HideFromDebug(FieldInfo fieldInfo)
        {
            return base.HideFromDebug(fieldInfo);
        }

        public override FieldInstance HideFromDebug(FieldInstance field)
        {
            return base.HideFromDebug(field);
        }

        public override MethodInstance HideFromDebug(MethodInstance method)
        {
            return base.HideFromDebug(method);
        }

        public override PropertyInfo HideFromDebug(PropertyInfo propertyInfo)
        {
            return base.HideFromDebug(propertyInfo);
        }

        public override PropertyInstance HideFromDebug(PropertyInstance property)
        {
            return base.HideFromDebug(property);
        }

        public override void VisitAnnotation(ConstructorInfo annotationConstructor, params object[] arguments)
        {
            base.VisitAnnotation(annotationConstructor, arguments);
        }

        public override IMethodVisitor VisitConstructor(MethodAttributes access, params Type[] parameters)
        {
            return new TraceMethodVisitor(base.VisitConstructor(access, parameters), sb);
        }

        public override IMethodVisitor VisitConstructor(MethodAttributes access, params NewType[] parameters)
        {
            return new TraceMethodVisitor(base.VisitConstructor(access, parameters), sb);
        }

        public override IFieldVisitor VisitField(FieldInstance field)
        {
            return new TraceFieldVisitor(base.VisitField(field), sb);
        }

        public override IMethodVisitor VisitMethod(MethodInstance method)
        {
            return new TraceMethodVisitor(base.VisitMethod(method), sb);
        }

        public override IMethodVisitor VisitMethod(MethodAttributes access, string name, NewType returnType, params NewType[] parameters)
        {
            return new TraceMethodVisitor(base.VisitMethod(access, name, returnType, parameters), sb);
        }

        public override FieldInstance ImplementStaticAssignedField(string staticFieldName, object fieldValue)
        {
            return ImplementStaticAssignedField(staticFieldName, fieldValue);
        }

        public override FieldInstance ImplementStaticAssignedField(string staticFieldName, Type fieldType, object fieldValue)
        {
            return ImplementStaticAssignedField(staticFieldName, fieldType, fieldValue);
        }

        public override PropertyInstance ImplementAssignedReadonlyProperty(string propertyName, object fieldValue)
        {
            return base.ImplementAssignedReadonlyProperty(propertyName, fieldValue);
        }

        public override FieldInstance ImplementField(FieldInstance field)
        {
            return base.ImplementField(field);
        }

        public override FieldInstance ImplementField(FieldInstance field, FScript script)
        {
            return base.ImplementField(field, script);
        }

        public override PropertyInstance ImplementLazyInitProperty(PropertyInstance property, Script script, params string[] fireThisOnPropertyNames)
        {
            return base.ImplementLazyInitProperty(property, script, fireThisOnPropertyNames);
        }

        public override PropertyInstance ImplementProperty(PropertyInstance property, Script getterScript, Script setterScript)
        {
            return base.ImplementProperty(property, getterScript, setterScript);
        }

        public override MethodInstance ImplementSetter(MethodInstance method, FieldInstance field)
        {
            return base.ImplementSetter(method, field);
        }

        public override PropertyInstance ImplementSetter(PropertyInstance property, FieldInstance field)
        {
            return base.ImplementSetter(property, field);
        }

        public override MethodInstance ImplementGetter(MethodInstance method, FieldInstance field)
        {
            return base.ImplementGetter(method, field);
        }

        public override PropertyInstance ImplementGetter(PropertyInstance property, FieldInstance field)
        {
            return base.ImplementGetter(property, field);
        }

        public override void OverrideConstructors(IOverrideConstructorDelegate overrideConstructorDelegate)
        {
            base.OverrideConstructors(overrideConstructorDelegate);
        }

        public override IMethodVisitor StartOverrideWithSuperCall(MethodInstance superMethod)
        {
            return base.StartOverrideWithSuperCall(superMethod);
        }

        public override void Visit(TypeAttributes access, string name, Type superName, Type[] interfaces)
        {
            base.Visit(access, name, superName, interfaces);
        }

        public override void VisitEnd()
        {
            base.VisitEnd();
        }
    }
}