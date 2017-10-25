package com.koch.ambeth.datachange.model;

/*-
 * #%L
 * jambeth-datachange
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

import java.util.List;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

/**
 * Core event object for the DCE infrastructure. It encapsulates the model transition of one or more
 * entities implied by one or more merge process instances. This event may be grouped with other
 * instances of itself so there is no backwards relationship from an instance of this to any
 * specific merge process instance or any potential data repository transaction.<br>
 * <br>
 * If at least one remote source initiated at least one of the results of the included merge process
 * instances then the {@link #getCausingUUIDs()} properties gives those sources the information to
 * know whether 'their' pending merge process instances they are interested in are enclosed.<br>
 * <br>
 * Please note that this object is by itself not designed to be immutable but handlers are highly
 * requested to not modify the content - e.g. of the enclosed collections - for the sake of
 * consistency.
 */
@XmlType
public interface IDataChange {
	long getChangeTime();

	/**
	 * Gets the UUIDs of remote instances of a merge process which contributed to the enclosed model
	 * transition.
	 *
	 * @return The UUIDs of remote instances of a merge process. May be null or empty. May contain
	 *         more than one UUID which means that this DCE is (at least) the union of multiple former
	 *         UUID-dedicated DCEs.
	 */
	String[] getCausingUUIDs();

	/**
	 * Provides all entity-based model transitions enclosed in this DCE (creates + updates + deletes)
	 *
	 * @return All entity-based model transitions (creates + updates + deletes)
	 */
	List<IDataChangeEntry> getAll();

	/**
	 * Provides all entity deletes enclosed in this DCE
	 *
	 * @return All entity deletes
	 */
	List<IDataChangeEntry> getDeletes();

	/**
	 * Provides all entity updates enclosed in this DCE
	 *
	 * @return All entity updates
	 */
	List<IDataChangeEntry> getUpdates();

	/**
	 * Provides all entity creates enclosed in this DCE
	 *
	 * @return All entity creates
	 */
	List<IDataChangeEntry> getInserts();

	/**
	 * Provides the information whether this DCE has no content (that is: creates, updates and deletes
	 * are all empty)
	 *
	 * @return false if any of creates, updates or deletes is not empty
	 */
	boolean isEmpty();

	/**
	 * Provides the information whether this DCE has no content based on the requested entity type
	 * (that is: creates, updates and deletes are all empty considering the given entity type
	 * criteria)
	 *
	 * @param entityType
	 * @return
	 */
	boolean isEmptyByType(Class<?> entityType);

	boolean isLocalSource();

	/**
	 * Allows to create another DCE instance containing only the model transition of the interested
	 * entity types.<br>
	 * <br>
	 * Note that this functionality considers polymorphism and inheritance and can therefore also used
	 * to derive by behavior-specific marker interfaces of entity types and is therefore not
	 * restricted solely to the discrete mapped entity types
	 *
	 * @param interestedEntityTypes The entity types you want this DCE to be filtered with
	 * @return The filtered DCE derived from this DCE considering the filter criteria
	 */
	IDataChange derive(Class<?>... interestedEntityTypes);

	/**
	 * Allows to create another DCE instance containing the model transition of all but the
	 * uninteresting entity types.<br>
	 * <br>
	 * Note that this functionality considers polymorphism and inheritance and can therefore also used
	 * to derive by behavior-specific marker interfaces of entity types and is therefore not
	 * restricted solely to the discrete mapped entity types
	 *
	 * @param uninterestingEntityTypes The entity types you want this DCE to be filtered with
	 * @return The filtered DCE derived from this DCE considering the filter criteria
	 */
	IDataChange deriveNot(Class<?>... uninterestingEntityTypes);

	/**
	 * Allows to create another DCE instance containing only the model transition of entities with the
	 * given primary identifier.<br>
	 * <br>
	 * Note that the given type of the identifier must match exactly the one dealt with by the Ambeth
	 * Entity MetaData. E.g. if the identifier is of type long or Long you may have to pass a value of
	 * e.g. 1 as 1L because the Java Boxing would treat 1 as an java.lang.Integer (which is not
	 * exactly equal to java.lang.Long with the same value).
	 *
	 * @param idIndex The type of the identifier you want to provide. For the primary identifier you
	 *        should pass {@link IObjRef# identifiers you want this DCE to be filtered with
	 * @param interestedEntityIds The identifiers you want this DCE to be filtered with
	 * @return The filtered DCE derived from this DCE considering the filter criteria
	 */
	IDataChange derive(int idIndex, Object... interestedEntityIds);
}
