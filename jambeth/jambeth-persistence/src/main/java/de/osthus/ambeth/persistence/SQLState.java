package de.osthus.ambeth.persistence;

public enum SQLState
{
	CONNECTION_NOT_OPEN("08003"), ACCESS_VIOLATION("42000");

	private String xopen;

	private SQLState(String bothCode)
	{
		this.xopen = bothCode;
	}

	public String getXopen()
	{
		return xopen;
	}

	public String getMessage()
	{
		return name();
	}
}
