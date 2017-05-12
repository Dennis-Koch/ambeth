namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Performs a deep copy of the object. The ObjectCopier can clone any object tree in a flexible and extendable way.
    /// Use IObjectCopierExtendable to provide own extensions to the default ObjectCopier behavior if necessary.
    /// In addition the ObjectCopier recognizes native data copy scenarios as well as cyclic paths in the object tree.
    /// </summary>
    public interface IObjectCopier
    {
        /// <summary>
        /// Performs a deep copy of the object
        /// </summary>
        /// <typeparam name="T">The type of object being copied</typeparam>
        /// <param name="source">The object instance to copy</param>
        /// <returns>The copied object representing a deep clone of the source object</returns>
        T Clone<T>(T source);
    }
}