package de.osthus.ambeth.changecontroller;

/**
 * A base class for change controller listeners. It categorizes whether a call is a create, update or delete.
 */
public abstract class AbstractRule<T> implements IChangeControllerExtension<T>
{
	@Override
	public int compareTo(IChangeControllerExtension<T> other)
	{
		final int cmp;
		if (other == null)
		{
			cmp = -1; // null comes last
		}
		else if (this.equals(other))
		{
			cmp = 0;
		}
		else
		{
			// Compare both rules by their identifier strings
			cmp = this.toString().compareTo(other.toString());
		}
		return cmp;
	}

	@Override
	public String toString()
	{
		int hashCode = System.identityHashCode(this);
		return this.getClass().getName() + "_" + Integer.toHexString(hashCode);
	}

	@Override
	public void processChange(T newEntity, T oldEntity, boolean toBeDeleted, boolean toBeCreated, CacheView views)
	{
		onChange(newEntity, oldEntity, views);
		if (toBeDeleted)
		{
			onDelete(newEntity, views);
		}
		else if (toBeCreated)
		{
			onCreate(newEntity, views);
		}
		else
		{
			onUpdate(newEntity, oldEntity, views);
		}
		if (!toBeDeleted)
		{
			validateInvariant(newEntity, oldEntity, views);
		}
	}

	/**
	 * This validation rule is called for every change (update, deletion, creation)
	 * 
	 * @param newEntity
	 *            the new entity, <code>null</code> if an entity is deleted
	 * @param oldEntity
	 *            the old entity, <code>null</code> if an entity is created
	 * @param views
	 *            an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onChange(T newEntity, T oldEntity, CacheView views)
	{
	}

	/**
	 * This validation rule is called for deletions only.
	 * 
	 * @param oldEntity
	 *            the deleted entity, never <code>null</code>
	 * @param views
	 *            an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onDelete(T oldEntity, CacheView views)
	{
	}

	/**
	 * This validation rule is called for creations only.
	 * 
	 * @param newEntity
	 *            the created entity, never <code>null</code>
	 * @param views
	 *            an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onCreate(T newEntity, CacheView views)
	{
	}

	/**
	 * This validation rule is called for updates only.
	 * 
	 * @param newEntity
	 *            the entity after the update, never <code>null</code>
	 * @param oldEntity
	 *            the entity before the update, never <code>null</code>
	 * @param views
	 *            an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onUpdate(T newEntity, T oldEntity, CacheView views)
	{
	}

	/**
	 * This validation rule is called for creates and update. It is an entry for rules that check properties that should always hold.
	 * 
	 * The old entity is also given to allow optimizations.
	 * 
	 * @param newEntity
	 *            the entity after the update or creation, never <code>null</code>
	 * @param oldEntity
	 *            the entity before update or creation, <code>null</code> in case of creation
	 * @param views
	 *            an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void validateInvariant(T newEntity, T oldEntity, CacheView views)
	{
	}
}
