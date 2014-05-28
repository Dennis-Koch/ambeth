package de.osthus.ambeth.persistence.exception;

import javax.persistence.PersistenceException;

public abstract class AbstractAmbethPersistenceException extends PersistenceException
{
	private static final long serialVersionUID = -6289536750887364782L;

	protected final String relatedSql;

	public AbstractAmbethPersistenceException(String message, String relatedSql, Throwable cause)
	{
		super(message, cause);
		this.relatedSql = relatedSql;
	}

	public AbstractAmbethPersistenceException(String message, String relatedSql)
	{
		super(message);
		this.relatedSql = relatedSql;
	}

	public AbstractAmbethPersistenceException(String relatedSql, Throwable cause)
	{
		super(cause);
		this.relatedSql = relatedSql;
	}

	public String getRelatedSql()
	{
		return relatedSql;
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ". Related SQL: " + relatedSql;
	}
}
