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

import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.annotation.CascadeLoadMode;

public interface IDirectedLinkMetaData
{
	ITableMetaData getFromTable();

	IFieldMetaData getFromField();

	Class<?> getFromEntityType();

	byte getFromIdIndex();

	Member getFromMember();

	ITableMetaData getToTable();

	IFieldMetaData getToField();

	Class<?> getToEntityType();

	byte getToIdIndex();

	Member getToMember();

	String getName();

	boolean isNullable();

	boolean isReverse();

	boolean isPersistingLink();

	/**
	 * Link _not_ persisted in this table?
	 * 
	 * @return Standalone status
	 */
	boolean isStandaloneLink();

	boolean isCascadeDelete();

	Class<?> getEntityType();

	RelationMember getMember();

	ILinkMetaData getLink();

	IDirectedLinkMetaData getReverseLink();

	CascadeLoadMode getCascadeLoadMode();
}
