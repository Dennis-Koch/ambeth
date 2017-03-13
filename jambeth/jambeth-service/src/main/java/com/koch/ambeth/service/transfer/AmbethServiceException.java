package com.koch.ambeth.service.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AmbethServiceException
{
	@XmlElement(required = false)
	protected String message;

	@XmlElement(required = false)
	protected String stackTrace;

	@XmlElement(required = false)
	protected AmbethServiceException cause;

	public String getMessage()
	{
		return message;
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

	public void setStackTrace(String stackTrace)
	{
		this.stackTrace = stackTrace;
	}

	public void setCause(AmbethServiceException cause)
	{
		this.cause = cause;
	}
}
