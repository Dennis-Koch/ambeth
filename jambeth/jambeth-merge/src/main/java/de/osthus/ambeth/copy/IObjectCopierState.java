package de.osthus.ambeth.copy;

/**
 * Perform a deep copy of the object. This method is similar to the IObjectCopier.Clone(Object) method but will only be used from within IObjectCopierExtension
 * implementations. It will be necessary if a custom copy logic is intended but only for a specific part of an object graph - remaining paths should be handled
 * in default behavior in most cases. The ObjectCopierState managed the knowledge about already copied objects and the current position in the object graph.
 * Extension logic must not handle any copy state by itself but do any operation only on the calling stack.
 */
public interface IObjectCopierState
{
	/**
	 * Perform a deep copy of the object which encapsulates a sub-graph of the root copy operation
	 * 
	 * @param <T>
	 *            The type of object being copied
	 * @param source
	 *            The object instance to copy
	 * @return The copied object representing a deep clone of the source object
	 */
	<T> T clone(T source);

	/**
	 * Allows to register a clone instance before calling recursive calls to IObjectCopier. This is necessary to be safe against cycles in the object graph were
	 * an object which is copied by a custom extension refers anywhere in the transitive relations to itself
	 * 
	 * @param <T>
	 *            The type of object being copied
	 * @param source
	 *            The object instance being currently copied
	 * @param clone
	 *            The copied object which is currently in progress being "filled" with content
	 */
	<T> void addClone(T source, T clone);

	/**
	 * Processes a deep clone of each property from 'source' to the given 'clone'
	 * 
	 * @param source The object instance being currently copied
	 * @param clone The copied object which is currently in progress being "filled" with content
	 */
	<T> void deepCloneProperties(T source, T clone);
}
