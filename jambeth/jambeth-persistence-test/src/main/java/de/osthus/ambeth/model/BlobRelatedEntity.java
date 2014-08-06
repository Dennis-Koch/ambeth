package de.osthus.ambeth.model;

public class BlobRelatedEntity extends AbstractEntity
{
	protected String name;

	protected BlobObject blob;

	protected BlobRelatedEntity()
	{
		// Intended blank
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public BlobObject getBlob()
	{
		return blob;
	}

	public void setBlob(BlobObject blob)
	{
		this.blob = blob;
	}
}
