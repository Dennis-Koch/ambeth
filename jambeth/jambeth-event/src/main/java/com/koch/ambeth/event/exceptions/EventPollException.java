package com.koch.ambeth.event.exceptions;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public class EventPollException extends RuntimeException
{
	private static final long serialVersionUID = -8293188742825456112L;

	public EventPollException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public EventPollException(String message)
	{
		super(message);
	}
}
