package de.osthus.ambeth.chunk;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IChunkProvider
{
	List<IChunkedResponse> getChunkedContents(List<IChunkedRequest> chunkedRequests);
}
