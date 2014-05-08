package de.osthus.ambeth.model;

public class ClobObject extends AbstractEntity
{
	protected char[] content;

	protected ClobObject()
	{
		// Intended blank
	}

	public void setContent(char[] content)
	{
		this.content = content;
	}

	public char[] getContent()
	{
		return content;
	}
}
