package de.osthus.ambeth.ioc.exception;

import de.osthus.ambeth.ioc.link.AbstractLinkContainer;

@SuppressWarnings("serial")
public class LinkException extends RuntimeException
{
	transient protected final AbstractLinkContainer linkContainer;

	public LinkException(String message, Throwable cause, AbstractLinkContainer linkContainer)
	{
		super(message, cause);
		this.linkContainer = linkContainer;
	}

	public LinkException(String message, AbstractLinkContainer linkContainer)
	{
		super(message);
		this.linkContainer = linkContainer;
	}
}
