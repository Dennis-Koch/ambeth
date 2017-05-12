using System;

namespace De.Osthus.Ambeth.Copy
{
    public abstract class AbstractCustomConstructorOCE : IObjectCopierExtension
	{
		public Object DeepClone(Object original, IObjectCopierState objectCopierState)
		{
			Object clone = CreateCloneInstance(original, objectCopierState);
			objectCopierState.AddClone(original, clone);
			objectCopierState.DeepCloneProperties(original, clone);
			return clone;
		}

		protected abstract Object CreateCloneInstance(Object original, IObjectCopierState objectCopierState);
	}
}