using System;

namespace De.Osthus.Ambeth.Bytecode
{
	public interface IValueResolveDelegate
	{
        Type ValueType { get; }

        Object Invoke(String fieldName, Type enhancedType);
	}
}