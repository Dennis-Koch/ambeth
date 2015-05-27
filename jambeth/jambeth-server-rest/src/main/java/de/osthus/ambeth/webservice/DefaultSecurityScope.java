package de.osthus.ambeth.webservice;

import de.osthus.ambeth.model.ISecurityScope;

/**
 * Copy'n Paste from IQ
 */
public enum DefaultSecurityScope implements ISecurityScope
{
	INSTANCE;

	@Override
	public String getName()
	{
		return "defaultScope";
	}
}
