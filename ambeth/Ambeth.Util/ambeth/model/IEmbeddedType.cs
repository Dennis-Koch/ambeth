using System;

namespace De.Osthus.Ambeth.Model
{
    /// <summary>
    /// Marker interface to allow recognizing embedded type instances
    /// </summary>
	public interface IEmbeddedType
	{
        Object Parent { get; }

        Object Root { get; }
	}
}
