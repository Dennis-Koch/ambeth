package com.koch.ambeth.model;

public class BlobObject extends AbstractEntity
{
	protected byte[] content;

	protected BlobObject()
	{
		// Intended blank
	}

	public void setContent(byte[] content)
	{
		this.content = content;
	}

	public byte[] getContent()
	{
		return content;
	}
}
