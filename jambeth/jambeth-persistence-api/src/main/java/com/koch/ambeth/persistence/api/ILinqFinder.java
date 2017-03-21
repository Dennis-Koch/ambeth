package com.koch.ambeth.persistence.api;

/*-
 * #%L
 * jambeth-persistence-api
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

import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;

public interface ILinqFinder
{
	/**
	 * Selects ID and version of all entities with a given value in this field.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionCursor all(Object value);

	/**
	 * Selects ID and version of the only entity with a given value in this field. If there are no or more than one entities an exception is thrown.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionItem single(Object value) throws IllegalResultException;

	/**
	 * Selects ID and version of the first entity with a given value in this field. If there is no entity an exception is thrown.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionItem first(Object value) throws IllegalResultException;

	/**
	 * Selects ID and version of the first entity with a given value in this field. If there is no entity null is returned.
	 * 
	 * @param value
	 *            Identifying value.
	 * @return Cursor to ID and version of all selected entities.
	 */
	IVersionItem firstOrDefault(Object value);
}
