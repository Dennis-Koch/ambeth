using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public interface IClassVisitor
    {
        FieldInfo HideFromDebug(FieldInfo fieldInfo);

        FieldInstance HideFromDebug(FieldInstance field);

        MethodInstance HideFromDebug(MethodInstance method);

        PropertyInfo HideFromDebug(PropertyInfo propertyInfo);

        PropertyInstance HideFromDebug(PropertyInstance property);
        
        void VisitAnnotation(ConstructorInfo annotationConstructor, params Object[] arguments);

        IMethodVisitor VisitConstructor(MethodAttributes access, params Type[] parameters);

        IMethodVisitor VisitConstructor(MethodAttributes access, params NewType[] parameters);

        IFieldVisitor VisitField(FieldInstance field);

        IMethodVisitor VisitMethod(MethodInstance method);

        IMethodVisitor VisitMethod(MethodAttributes access, String name, NewType returnType, params NewType[] parameters);

        FieldInstance ImplementStaticAssignedField(String staticFieldName, Object fieldValue);

        FieldInstance ImplementStaticAssignedField(String staticFieldName, Type fieldType, Object fieldValue);

        PropertyInstance ImplementAssignedReadonlyProperty(String propertyName, Object fieldValue);

        FieldInstance ImplementField(FieldInstance field);

        FieldInstance ImplementField(FieldInstance field, FScript script);

        PropertyInstance ImplementLazyInitProperty(PropertyInstance property, Script script, params String[] fireThisOnPropertyNames);

        PropertyInstance ImplementProperty(PropertyInstance property, Script getterScript, Script setterScript);

        MethodInstance ImplementSetter(MethodInstance method, FieldInstance field);

        PropertyInstance ImplementSetter(PropertyInstance property, FieldInstance field);

        MethodInstance ImplementGetter(MethodInstance method, FieldInstance field);

        PropertyInstance ImplementGetter(PropertyInstance property, FieldInstance field);

        void OverrideConstructors(IOverrideConstructorDelegate overrideConstructorDelegate);

        IMethodVisitor StartOverrideWithSuperCall(MethodInstance superMethod);

        MethodInstance ImplementSwitchByIndex(MethodInstance method, String exceptionMessageOnIllegalIndex, int indexSize, ScriptWithIndex script);
        
        void Visit(TypeAttributes access, String name, Type superName, Type[] interfaces);

        void VisitEnd();
    }
}
