using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Reflection;

namespace De.Osthus.Ambeth.Bytecode.Behavior
{
    public interface IBytecodeBehaviorState
    {
	    Type OriginalType { get; }

	    Type CurrentType { get; }

	    NewType NewType { get; }

        IServiceContext BeanContext { get; }

	    IEnhancementHint Context { get; }
        
	    T GetContext<T>() where T : IEnhancementHint;

        Object GetContext(Type contextType);

        PropertyInstance GetProperty(String propertyName);

	    MethodInstance[] GetAlreadyImplementedMethodsOnNewType();

	    FieldInstance GetAlreadyImplementedField(String fieldName);

        bool HasMethod(MethodInstance method);

	    bool IsMethodAlreadyImplementedOnNewType(MethodInstance method);
    }
}
