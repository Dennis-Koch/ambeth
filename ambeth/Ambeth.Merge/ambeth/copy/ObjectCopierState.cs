using System.Collections.Generic;
using System;
using De.Osthus.Ambeth.Collections;

namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Encapsulates the internal state of an ObjectCopier operation
    /// </summary>
    public class ObjectCopierState : IObjectCopierState
    {
        public readonly Object[] addArgs = new Object[1];

        public readonly IdentityDictionary<Object, Object> objectToCloneDict = new IdentityDictionary<Object, Object>();

        private readonly ObjectCopier objectCopier;

        public ObjectCopierState(ObjectCopier objectCopier)
        {
            this.objectCopier = objectCopier;
        }

        T IObjectCopierState.Clone<T>(T source)
        {
            return objectCopier.CloneRecursive(source, this);
        }

        void IObjectCopierState.AddClone<T>(T source, T clone)
        {
            objectToCloneDict.Add(source, clone);
        }

		public void DeepCloneProperties<T>(T source, T clone)
		{
			objectCopier.DeepCloneProperties(source, clone, this);
		}

        /// <summary>
        /// Called to prepare this instance for clean reusage
        /// </summary>
        public void Clear()
        {
            addArgs[0] = null;
            objectToCloneDict.Clear();
        }
    }
}