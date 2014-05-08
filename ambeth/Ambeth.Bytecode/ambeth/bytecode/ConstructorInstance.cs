using De.Osthus.Ambeth.Bytecode.Behavior;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode
{
    public class ConstructorInstance : MethodInstance
    {
        public const String CONSTRUCTOR_NAME = ".ctor";

        public ConstructorInstance(ConstructorInfo constructor)
            : base(NewType.GetType(constructor.DeclaringType), constructor.Attributes, CONSTRUCTOR_NAME, 
                NewType.VOID_TYPE, TypeUtil.GetClassesToTypes(constructor.GetParameters()))
        {
            this.method = constructor;
        }

        public ConstructorInstance(NewType owner, ConstructorInfo constructor, params NewType[] parameters)
            : base(owner, constructor.Attributes, CONSTRUCTOR_NAME, NewType.VOID_TYPE, parameters)
        {
            this.method = constructor;
        }

        public ConstructorInstance(NewType owner, MethodAttributes access, params NewType[] parameters)
            : base(owner, access, CONSTRUCTOR_NAME, NewType.VOID_TYPE, parameters)
        {
            // Intended blank
        }

        public ConstructorInstance(Type owner, MethodAttributes access, params Type[] parameters)
            : base(NewType.GetType(owner), access, CONSTRUCTOR_NAME, NewType.VOID_TYPE, TypeUtil.GetClassesToTypes(parameters))
        {
            // Intended blank
        }

        public ConstructorInstance(MethodAttributes access, params NewType[] parameters)
            : base(BytecodeBehaviorState.State.NewType, access, CONSTRUCTOR_NAME, NewType.VOID_TYPE, parameters)
        {
            // Intended blank
        }

        public ConstructorInstance(MethodAttributes access, params Type[] parameters)
            : base(BytecodeBehaviorState.State.NewType, access, CONSTRUCTOR_NAME, NewType.VOID_TYPE, TypeUtil.GetClassesToTypes(parameters))
        {
            // Intended blank
        }
    }
}
