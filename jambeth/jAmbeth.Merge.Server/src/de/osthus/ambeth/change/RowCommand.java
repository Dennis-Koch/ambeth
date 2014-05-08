package de.osthus.ambeth.change;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;

public class RowCommand implements IRowCommand
{
	@SuppressWarnings("unused")
	@LogInstance(RowCommand.class)
	private ILogger log;

	protected IObjRef reference;

	protected IChangeCommand toBeExecuted;

	@Override
	public void dispose()
	{
		this.reference = null;

		if (this.toBeExecuted != null)
		{
			this.toBeExecuted.dispose();
			this.toBeExecuted = null;
		}
	}

	@Override
	public IObjRef getReference()
	{
		return reference;
	}

	@Override
	public void setReference(IObjRef reference)
	{
		this.reference = reference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.change.IRowCommand#addCommand(de.osthus.ambeth.change.IChangeCommand)
	 */
	@Override
	public void addCommand(IChangeCommand command)
	{
		if (this.toBeExecuted != null)
		{
			this.toBeExecuted = this.toBeExecuted.addCommand(command);
		}
		else
		{
			this.toBeExecuted = command;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.change.IRowCommand#getCommand()
	 */
	@Override
	public IChangeCommand getCommand()
	{
		return this.toBeExecuted;
	}
}
