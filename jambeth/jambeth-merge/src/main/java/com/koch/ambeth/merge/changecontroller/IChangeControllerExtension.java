package com.koch.ambeth.merge.changecontroller;

/**
 * Rules that want to listen on changes on objects before persistence should implement this interface and be registered with the link API to the
 * {@link IChangeControllerExtendable}.
 * 
 * The {@link Comparable} interface is extended because the order in which rules are processed may be significant.
 * 
 * @param <T>
 */
public interface IChangeControllerExtension<T> extends Comparable<IChangeControllerExtension<T>>
{
	/**
	 * This method is called for each entity of type T that has been changed (i.e. created, updated or deleted)
	 * 
	 * @param newEntity
	 *            The entities' version after the change
	 * @param oldEntity
	 *            The entities' version before the change. An implementation must not change this instance in any way.
	 * @param toBeDeleted
	 *            true, if the new entity is to be deleted
	 * @param toBeCreated
	 *            true, if the new entity is to be created
	 * @param views
	 *            The views argument provides methods to access all changed entities.
	 */
	void processChange(T newEntity, T oldEntity, boolean toBeDeleted, boolean toBeCreated, ICacheView views);

	/**
	 * Returns a negative integer if this rule must be evaluated before the other rule or a positive integer if the rule must be evaluated after the other rule.
	 * 0 implies that this and the other are equal, <emph>not</emph> that the order of evaluation does not matter.
	 */
	@Override
	int compareTo(IChangeControllerExtension<T> o);
}
