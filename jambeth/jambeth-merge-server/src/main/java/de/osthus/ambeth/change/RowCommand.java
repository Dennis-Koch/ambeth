package de.osthus.ambeth.change;

public class RowCommand implements IRowCommand
{
	protected IChangeCommand toBeExecuted;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.change.IRowCommand#addCommand(de.osthus.ambeth.change.IChangeCommand)
	 */
	@Override
	public void addCommand(IChangeCommand command)
	{
		if (toBeExecuted != null)
		{
			toBeExecuted = toBeExecuted.addCommand(command);
		}
		else
		{
			toBeExecuted = command;
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
		return toBeExecuted;
	}
}
