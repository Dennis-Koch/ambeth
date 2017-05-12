using De.Osthus.Ambeth.Bytecode;
using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class InterfaceToClassVisitor : ClassVisitor
    {
        public InterfaceToClassVisitor(IClassVisitor cv)
            : base(cv)
        {
            // Intended blank
        }

        public override void Visit(TypeAttributes access, String name, Type superName, Type[] interfaces)
        {
            Type originalType = State.OriginalType;
            access &= ~TypeAttributes.Abstract;
            access &= ~TypeAttributes.Interface;
            if (originalType.IsInterface)
            {
                CHashSet<Type> interfaceSet = new CHashSet<Type>(interfaces);
                interfaceSet.Add(originalType);
                interfaces = interfaceSet.ToArray();
            }
            base.Visit(access, name, superName, interfaces);
        }
    }
}