package de.osthus.ambeth.change;

import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.persistence.ITable;

public class DeleteCommand extends AbstractChangeCommand implements IDeleteCommand
{
	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		DeleteContainer container = (DeleteContainer) changeContainer;
		super.configureFromContainer(container, table);
	}

	@Override
	protected IChangeCommand addCommand(ICreateCommand other)
	{
		throw new IllegalCommandException("Create command for an entity to be deleted!");
	}

	@Override
	protected IChangeCommand addCommand(IUpdateCommand other)
	{
		return other.addCommand(this);
	}

	@Override
	protected IChangeCommand addCommand(IDeleteCommand other)
	{
		return this;
	}
}
