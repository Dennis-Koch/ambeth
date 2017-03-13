package com.koch.ambeth.persistence.api;

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
