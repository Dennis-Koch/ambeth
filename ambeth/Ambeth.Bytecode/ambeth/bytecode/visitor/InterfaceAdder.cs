using De.Osthus.Ambeth.Collections;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Visitor
{
    public class InterfaceAdder : ClassVisitor
    {
        private readonly IISet<Type> newInterfaces;

        public InterfaceAdder(IClassVisitor cv, IISet<Type> newInterfaces)
            : base(cv)
        {
            this.newInterfaces = newInterfaces;
        }

        public InterfaceAdder(IClassVisitor cv, params Type[] newInterfaces)
            : base(cv)
        {
            this.newInterfaces = new CHashSet<Type>(newInterfaces);
        }

        public override void Visit(TypeAttributes access, String name, Type superName, Type[] interfaces)
        {
            LinkedHashSet<Type> ints = new LinkedHashSet<Type>(interfaces);
            ints.AddAll(newInterfaces);
            Type type = State.CurrentType;
            while (type != null && type != typeof(Object))
            {
                foreach (Type alreadyImplementedInterface in type.GetInterfaces())
                {
                    ints.Remove(alreadyImplementedInterface);
                }
                type = type.BaseType;
            }
            base.Visit(access, name, superName, ints.ToArray());
        }
    }
}