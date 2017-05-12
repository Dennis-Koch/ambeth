using System;

namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Implement this interface to encapsulate copy logic which extends the default ObjectCopier behavior
    /// </summary>
    public interface IObjectCopierExtension
    {
        /// <summary>
        /// Implement this interface to encapsulate copy logic which extends the default ObjectCopier behavior
        /// </summary>
        /// <param name="original">The object instance to copy</param>
        /// <param name="objectCopierState">Encapsulates the current copy state. It may be called in cascaded custom/default copy behaviors</param>
        /// <returns>The copied object representing a deep clone of the source object</returns>
        Object DeepClone(Object original, IObjectCopierState objectCopierState);
    }
}
