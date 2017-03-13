package com.koch.ambeth.merge.server.change;

import com.koch.ambeth.merge.transfer.AbstractChangeContainer;

public class LinkContainer extends AbstractChangeContainer
{
	protected ILinkChangeCommand command;

	protected String tableName;

	public ILinkChangeCommand getCommand()
	{
		return command;
	}

	public void setCommand(ILinkChangeCommand command)
	{
		this.command = command;
		setReference(command.getReference());
		tableName = this.command.getDirectedLink().getLink().getMetaData().getTableName();
	}

	public String getTableName()
	{
		return tableName;
	}
}
