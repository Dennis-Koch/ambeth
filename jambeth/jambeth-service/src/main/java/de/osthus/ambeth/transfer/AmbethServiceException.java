package de.osthus.ambeth.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "AmbethServiceException", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class AmbethServiceException
{
	@XmlElement(required = false)
	protected String message;

	@XmlElement(required = true)
	protected String exceptionType;

	@XmlElement(required = false)
	protected String stackTrace;

	@XmlElement(required = false)
	protected AmbethServiceException cause;

	public String getMessage()
	{
		return message;
	}

	public String getExceptionType()
	{
		return exceptionType;
	}

	public String getStackTrace()
	{
		return stackTrace;
	}

	public AmbethServiceException getCause()
	{
		return cause;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public void setExceptionType(String exceptionType)
	{
		this.exceptionType = exceptionType;
	}

	public void setStackTrace(String stackTrace)
	{
		this.stackTrace = stackTrace;
	}

	public void setCause(AmbethServiceException cause)
	{
		this.cause = cause;
	}
}
