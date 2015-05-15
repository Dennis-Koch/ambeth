package de.osthus.ambeth.persistence;

public enum SQLState
{
	CONNECTION_NOT_OPEN("08003"), ACCESS_VIOLATION("42000"), NULL_CONSTRAINT("23502"), UNIQUE_CONSTRAINT("23505");

	private String xopen;

	private SQLState(String bothCode)
	{
		xopen = bothCode;
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
