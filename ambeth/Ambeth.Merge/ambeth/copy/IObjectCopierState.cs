using System;

namespace De.Osthus.Ambeth.Copy
{
    /// <summary>
    /// Perform a deep copy of the object. This method is similar to the IObjectCopier.Clone(Object) method but will only be used from within IObjectCopierExtension implementations.
    /// It will be necessary if a custom copy logic is intended but only for a specific part of an object graph - remaining paths should be handled in default behavior in most cases.
    /// The ObjectCopierState managed the knowledge about already copied objects and the current position in the object graph. Extension logic must not handle any copy state
    /// by itself but do any operation only on the calling stack.
    /// </summary>
    public interface IObjectCopierState
    {
        /// <summary>
        /// Perform a deep copy of the object which encapsulates a sub-graph of the root copy operation
        /// </summary>
        /// <typeparam name="T">The type of object being copied</typeparam>
        /// <param name="source">The object instance to copy</param>
        /// <returns>The copied object representing a deep clone of the source object</returns>
        T Clone<T>(T source);
                
        /// <summary>
        /// Allows to register a clone instance before calling recursive calls to IObjectCopier. This is necessary to be safe against cycles in the object graph were an object which is
        /// copied by a custom extension refers anywhere in the transitive relations to itself
        /// </summary>
        /// <typeparam name="T">The type of object being copied</typeparam>
        /// <param name="source">The object instance being currently copied</param>
        /// <param name="clone">The copied object which is currently in progress being "filled" with content</param>
        void AddClone<T>(T source, T clone);

		/// <summary>
		/// Processes a deep clone of each property from 'source' to the given 'clone'
		/// </summary>
		/// <typeparam name="T"></typeparam>
		/// <param name="source">The object instance being currently copied</param>
		/// <param name="clone">The copied object which is currently in progress being "filled" with content</param>
		void DeepCloneProperties<T>(T source, T clone);
    }
}