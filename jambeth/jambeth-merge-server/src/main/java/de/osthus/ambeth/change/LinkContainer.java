package de.osthus.ambeth.change;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.transfer.AbstractChangeContainer;

public class LinkContainer extends AbstractChangeContainer
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
		this.tableName = this.command.getDirectedLink().getLink().getTableName();
	}

	public String getTableName()
	{
		return tableName;
	}
}
