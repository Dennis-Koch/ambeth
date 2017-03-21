package com.koch.ambeth.merge.changecontroller;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

/**
 * A base class for change controller listeners. It categorizes whether a call is a create, update
 * or delete.
 *
 * Rules have a defined order in which they are called. This is simply done by calling
 * {@link #toString()} and sorting them alphabetically.
 */
public abstract class AbstractRule<T> implements IChangeControllerExtension<T> {
	@Override
	public int compareTo(IChangeControllerExtension<T> other) {
		final int cmp;
		if (other == null) {
			cmp = -1; // null comes last
		}
		else if (equals(other)) {
			cmp = 0;
		}
		else {
			// Compare both rules by their identifier strings
			String otherKey =
					(other instanceof AbstractRule) ? ((AbstractRule<?>) other).getSortingKey() : toString();
			cmp = this.getSortingKey().compareTo(otherKey);
		}
		return cmp;
	}

	@Override
	public String toString() {
		int hashCode = System.identityHashCode(this);
		return this.getClass().getName() + "_" + Integer.toHexString(hashCode);
	}

	/**
	 * This property is used to specify a key by which rules are ordered.
	 *
	 * Previously, only {@link #toString()} was used for this purpose but having a distinct method
	 * prevents an unwanted change in the order when a {@link #toString()} will be overridden for
	 * other purposes like improved debugging.
	 *
	 * It is highly recommended to override this method but not enforced to provide
	 * backward-compatibility.
	 */
	public String getSortingKey() {
		return this.toString();
	}

	@Override
	public void processChange(T newEntity, T oldEntity, boolean toBeDeleted, boolean toBeCreated,
			ICacheView views) {
		onChange(newEntity, oldEntity, views);
		if (toBeDeleted) {
			onDelete(newEntity, views);
		}
		else if (toBeCreated) {
			onCreate(newEntity, views);
		}
		else {
			onUpdate(newEntity, oldEntity, views);
		}
		if (!toBeDeleted) {
			validateInvariant(newEntity, oldEntity, views);
		}
	}

	/**
	 * This validation rule is called for every change (update, deletion, creation)
	 *
	 * @param newEntity the new entity, <code>null</code> if an entity is deleted
	 * @param oldEntity the old entity, <code>null</code> if an entity is created
	 * @param views an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onChange(T newEntity, T oldEntity, ICacheView views) {
	}

	/**
	 * This validation rule is called for deletions only.
	 *
	 * @param oldEntity the deleted entity, never <code>null</code>
	 * @param views an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onDelete(T oldEntity, ICacheView views) {
	}

	/**
	 * This validation rule is called for creations only.
	 *
	 * @param newEntity the created entity, never <code>null</code>
	 * @param views an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onCreate(T newEntity, ICacheView views) {
	}

	/**
	 * This validation rule is called for updates only.
	 *
	 * @param newEntity the entity after the update, never <code>null</code>
	 * @param oldEntity the entity before the update, never <code>null</code>
	 * @param views an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void onUpdate(T newEntity, T oldEntity, ICacheView views) {
	}

	/**
	 * This validation rule is called for creates and update. It is an entry for rules that check
	 * properties that should always hold.
	 *
	 * The old entity is also given to allow optimizations.
	 *
	 * @param newEntity the entity after the update or creation, never <code>null</code>
	 * @param oldEntity the entity before update or creation, <code>null</code> in case of creation
	 * @param views an object that allows access to all modified entities, never <code>null</code>
	 */
	protected void validateInvariant(T newEntity, T oldEntity, ICacheView views) {
	}
}
