package de.osthus.ambeth.persistence;

import java.util.List;

public interface ILink
{
	ITable getFromTable();

	ITable getToTable();

	IField getFromField();

	IField getToField();

	boolean isNullable();

	boolean hasLinkTable();

	IDirectedLink getDirectedLink();

	IDirectedLink getReverseDirectedLink();

	String getName();

	String getTableName();

	/**
	 * Getter for the table name which is in quotes to allow to include the value directly in a query string
	 * 
	 * @return
	 */
	String getFullqualifiedEscapedTableName();

	String getArchiveTableName();

	ILinkCursor findAllLinked(IDirectedLink fromLink, List<?> fromIds);

	ILinkCursor findAllLinkedTo(IDirectedLink fromLink, List<?> toIds);

	ILinkCursor findLinked(IDirectedLink fromLink, Object fromId);

	ILinkCursor findLinkedTo(IDirectedLink fromLink, Object toId);

	void linkIds(IDirectedLink fromLink, Object fromId, List<?> toIds);

	void updateLink(IDirectedLink fromLink, Object fromId, Object toIds);

	void unlinkIds(IDirectedLink fromLink, Object fromId, List<?> toIds);

	void unlinkAllIds(IDirectedLink fromLink, Object fromId);

	void unlinkAllIds();

	void startBatch();

	int[] finishBatch();

	void clearBatch();
}
