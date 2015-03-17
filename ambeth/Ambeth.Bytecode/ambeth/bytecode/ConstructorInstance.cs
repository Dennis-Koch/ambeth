using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode
{
    public class ConstructorInstance : MethodInstance
    {
        public const String CONSTRUCTOR_NAME = ".ctor";

        public static readonly ConstructorInstance defaultConstructor = new ConstructorInstance(ReflectUtil.GetDeclaredConstructor(false, typeof(Object), new NewType[0]));

        public ConstructorInstance(ConstructorInfo constructor)
            : base(NewType.GetType(constructor.DeclaringType), constructor.Attributes, NewType.VOID_TYPE, CONSTRUCTOR_NAME, 
                TypeUtil.GetClassesToTypes(constructor.GetParameters()))
        {
            this.method = constructor;
        }

        public ConstructorInstance(NewType owner, ConstructorInfo constructor, params NewType[] parameters)
            : base(owner, constructor.Attributes, NewType.VOID_TYPE, CONSTRUCTOR_NAME, parameters)
        {
            this.method = constructor;
        }

        public ConstructorInstance(NewType owner, MethodAttributes access, params NewType[] parameters)
            : base(owner, access, NewType.VOID_TYPE, CONSTRUCTOR_NAME, parameters)
        {
            // Intended blank
        }

        public ConstructorInstance(Type owner, MethodAttributes access, params Type[] parameters)
            : base(NewType.GetType(owner), access, NewType.VOID_TYPE, CONSTRUCTOR_NAME, TypeUtil.GetClassesToTypes(parameters))
        {
            // Intended blank
        }

        public ConstructorInstance(MethodAttributes access, params NewType[] parameters)
            : base(BytecodeBehaviorState.State.NewType, access, NewType.VOID_TYPE, CONSTRUCTOR_NAME, parameters)
        {
            // Intended blank
        }

        public ConstructorInstance(MethodAttributes access, params Type[] parameters)
            : base(BytecodeBehaviorState.State.NewType, access, NewType.VOID_TYPE, CONSTRUCTOR_NAME, TypeUtil.GetClassesToTypes(parameters))
        {
            // Intended blank
        }
    }
}
