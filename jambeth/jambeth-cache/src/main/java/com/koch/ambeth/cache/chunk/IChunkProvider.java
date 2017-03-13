package com.koch.ambeth.cache.chunk;

import java.util.List;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IChunkProvider
{
	List<IChunkedResponse> getChunkedContents(List<IChunkedRequest> chunkedRequests);
}
