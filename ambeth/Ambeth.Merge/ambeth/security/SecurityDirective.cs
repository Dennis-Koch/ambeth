using System;

namespace De.Osthus.Ambeth.Security
{
    [Flags]
    public enum SecurityDirective
    {
		DISABLE_SERVICE_CHECK = 1, //

		DISABLE_ENTITY_CHECK = 2, //

		ENABLE_SERVICE_CHECK = 4, //

		ENABLE_ENTITY_CHECK = 8 //
    }
}