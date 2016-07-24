package de.osthus.ambeth.query.squery;

/**
 * if Squery method name can't find a nest field, raise this exception
 */
public class InvalidateSqueryNameException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7806214579602034539L;

	public InvalidateSqueryNameException(String fieldExpress, Class<?> clazz)
	{
		super(String.format("the field expression [%s] can't be parsed as a field or nest field in the type [%s]", fieldExpress, clazz));
	}
}
