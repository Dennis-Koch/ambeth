using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class InterfaceToClassVisitor : ClassVisitor
    {
        protected static readonly NewType objType = NewType.GetType(typeof(Object));

        private readonly Type parentType;

        protected bool interfaceMode = false;

        public InterfaceToClassVisitor(IClassVisitor cv, Type parentType)
            : base(cv)
        {
            this.parentType = parentType;
        }

        public override void Visit(TypeAttributes access, string name, Type superName, Type[] interfaces)
        {
            access &= ~TypeAttributes.Abstract;
            if (access.HasFlag(TypeAttributes.Interface))
            {
                access &= ~TypeAttributes.Interface;
                if (superName.IsInterface)
                {
                    // Move parent interface (which is wrong place) to the implemented interfaces
                    CHashSet<Type> interfaceSet = new CHashSet<Type>(interfaces);
                    interfaceSet.Add(parentType);
                    interfaces = interfaceSet.ToArray();
                    if (parentType.Equals(superName))
                    {
                        superName = typeof(Object);
                        interfaceMode = true;
                    }
                }
            }
            base.Visit(access, name, superName, interfaces);
        }

        public override void VisitEnd()
        {
            MethodInstance[] methods = State.GetAlreadyImplementedMethodsOnNewType();
            bool constructorDefined = false;
            foreach (MethodInstance method in methods)
            {
                if (ConstructorInstance.CONSTRUCTOR_NAME.Equals(method.Name))
                {
                    constructorDefined = true;
                    break;
                }
            }
            if (!constructorDefined)
            {
                if (interfaceMode)
                {
                    ImplementConstructor(typeof(Object).GetConstructor(null));
                }
                else
                {
                    ConstructorInfo[] constructors = State.CurrentType.GetConstructors(BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic);
                    foreach (ConstructorInfo constructor in constructors)
                    {
                        ImplementConstructor(constructor);
                    }
                }
            }
            base.VisitEnd();
        }

        protected void ImplementConstructor(ConstructorInfo constructor)
        {
            NewType[] types = TypeUtil.GetClassesToTypes(constructor.GetParameters());
            IMethodVisitor mg = VisitConstructor(MethodAttributes.Public, types);
            mg.LoadThis();
            mg.LoadArgs();
            mg.InvokeConstructor(constructor);
            mg.ReturnValue();
            mg.EndMethod();
        }
    }
}