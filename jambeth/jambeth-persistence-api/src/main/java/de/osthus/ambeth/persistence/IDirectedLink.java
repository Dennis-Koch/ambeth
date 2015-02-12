package de.osthus.ambeth.persistence;

import java.util.List;

public interface IDirectedLink
{
	IDirectedLinkMetaData getMetaData();

	ITable getFromTable();

	ITable getToTable();

	ILink getLink();

	IDirectedLink getReverseLink();

	ILinkCursor findLinked(Object fromId);

	ILinkCursor findLinkedTo(Object toId);

	ILinkCursor findAllLinked(List<?> fromIds);

	void linkIds(Object fromId, List<?> toIds);

	void updateLink(Object fromId, Object toId);

	void unlinkIds(Object fromId, List<?> toIds);

	void unlinkAllIds(Object fromId);
}
