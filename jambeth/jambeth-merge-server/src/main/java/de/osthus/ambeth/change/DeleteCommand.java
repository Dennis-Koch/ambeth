package de.osthus.ambeth.change;

import de.osthus.ambeth.merge.model.IObjRef;

public class DeleteCommand extends AbstractChangeCommand implements IDeleteCommand
{
	public DeleteCommand(IObjRef objRef)
	{
		super(objRef);
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
