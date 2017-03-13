package com.koch.ambeth.cache.chunk;

public class ChunkedResponse implements IChunkedResponse
{
	protected IChunkedRequest reference;
	protected byte[] payload;
	protected boolean deflated;
	protected int payloadSize;

	/**
	 * Needed for serialization
	 */
	public ChunkedResponse()
	{
		super();
	}

	public ChunkedResponse(IChunkedRequest reference, byte[] payload, boolean deflated, int payloadSize)
	{
		super();
		this.reference = reference;
		this.payload = payload;
		this.deflated = deflated;
		this.payloadSize = payloadSize;
	}

	@Override
	public IChunkedRequest getReference()
	{
		return reference;
	}

	public void setReference(IChunkedRequest reference)
	{
		this.reference = reference;
	}

	@Override
	public byte[] getPayload()
	{
		return payload;
	}

	public void setPayload(byte[] payload)
	{
		this.payload = payload;
	}

	@Override
	public int getPayloadSize()
	{
		return payloadSize;
	}

	public void setPayloadSize(int payloadSize)
	{
		this.payloadSize = payloadSize;
	}

	@Override
	public boolean isDeflated()
	{
		return deflated;
	}

	public void setDeflated(boolean deflated)
	{
		this.deflated = deflated;
	}
}
