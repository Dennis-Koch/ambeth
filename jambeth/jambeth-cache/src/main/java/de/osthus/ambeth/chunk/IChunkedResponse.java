package de.osthus.ambeth.chunk;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IChunkedResponse
{
	IChunkedRequest getReference();

	byte[] getPayload();

	/**
	 * Returns the inflated size of the payload in bytes. If <code>isDeflated()</code> is false the returned value is equal to the length of the byte array of
	 * <code>getPayload()</code>
	 * 
	 * @return the inflated size of the payload in bytes
	 */
	int getPayloadSize();

	boolean isDeflated();
}
