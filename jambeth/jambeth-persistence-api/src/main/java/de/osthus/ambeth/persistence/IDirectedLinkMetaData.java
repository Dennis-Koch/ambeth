package de.osthus.ambeth.persistence;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;

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
