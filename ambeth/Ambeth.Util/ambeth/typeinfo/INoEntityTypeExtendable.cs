using System;

namespace De.Osthus.Ambeth.Typeinfo
{
    public interface INoEntityTypeExtendable
	{
		void RegisterNoEntityType(Type noEntityType);

		void UnregisterNoEntityType(Type noEntityType);
	}
}
