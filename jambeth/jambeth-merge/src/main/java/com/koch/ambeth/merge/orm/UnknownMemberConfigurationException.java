package com.koch.ambeth.merge.orm;

public class UnknownMemberConfigurationException extends IllegalStateException
{
	private static final long serialVersionUID = 5003693068104745802L;

	public UnknownMemberConfigurationException(IMemberConfig unknownMemberConfig)
	{
		super("Unknown member configuration of type '" + unknownMemberConfig.getClass().getName() + "'");
	}
}
