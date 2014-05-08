package de.osthus.ambeth.persistence.exception;

public class NullConstraintException extends AbstractAmbethPersistenceException
{
	private static final long serialVersionUID = 7448945580470173927L;

	public NullConstraintException(String message, String relatedSql, Throwable e)
	{
		super(message, relatedSql, e);
	}
}
