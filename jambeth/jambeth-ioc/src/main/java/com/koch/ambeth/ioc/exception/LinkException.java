package com.koch.ambeth.ioc.exception;

import com.koch.ambeth.ioc.link.AbstractLinkContainer;

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

	public AbstractLinkContainer getLinkContainer()
	{
		return linkContainer;
	}
}
