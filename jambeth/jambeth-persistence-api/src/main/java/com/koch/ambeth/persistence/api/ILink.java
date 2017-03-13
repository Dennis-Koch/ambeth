package com.koch.ambeth.persistence.api;

import java.util.List;

public interface ILink
{
	ILinkMetaData getMetaData();

	ITable getFromTable();

	ITable getToTable();

	IDirectedLink getDirectedLink();

	IDirectedLink getReverseDirectedLink();

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
