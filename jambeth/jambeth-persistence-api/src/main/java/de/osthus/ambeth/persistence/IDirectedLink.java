package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public interface IDirectedLink
{

	ITable getFromTable();

	IField getFromField();

	Class<?> getFromEntityType();

	byte getFromIdIndex();

	ITypeInfoItem getFromMember();

	ITable getToTable();

	IField getToField();

	Class<?> getToEntityType();

	byte getToIdIndex();

	ITypeInfoItem getToMember();

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

	IRelationInfoItem getMember();

	ILink getLink();

	IDirectedLink getReverseLink();

	CascadeLoadMode getCascadeLoadMode();

	ILinkCursor findLinked(Object fromId);

	ILinkCursor findLinkedTo(Object toId);

	ILinkCursor findAllLinked(List<?> fromIds);

	void linkIds(Object fromId, List<?> toIds);

	void updateLink(Object fromId, Object toId);

	void unlinkIds(Object fromId, List<?> toIds);

	void unlinkAllIds(Object fromId);
}
