package de.osthus.ambeth.change;

import java.util.Map;

import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.persistence.ITable;

public abstract class AbstractChangeCommand implements IChangeCommand
{
	protected IObjRef reference;

	protected ITable table;

	@Override
	public void dispose()
	{
		this.reference = null;
		this.table = null;
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		this.reference = changeContainer.getReference();
		this.table = table;
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

	@Override
	public IChangeCommand addCommand(IChangeCommand other)
	{
		IChangeCommand toExecute;
		if (other instanceof ICreateCommand)
		{
			ICreateCommand createCommand = (ICreateCommand) other;
			toExecute = addCommand(createCommand);
		}
		else if (other instanceof IUpdateCommand)
		{
			IUpdateCommand updateCommand = (IUpdateCommand) other;
			toExecute = addCommand(updateCommand);
		}
		else if (other instanceof IDeleteCommand)
		{
			IDeleteCommand deleteCommand = (IDeleteCommand) other;
			toExecute = addCommand(deleteCommand);
		}
		else
		{
			throw new IllegalCommandException("Unknown command object!");
		}

		return toExecute;
	}

	@Override
	public String toString()
	{
		return this.getClass() + " for " + this.reference;
	}

	protected void repackPuis(IPrimitiveUpdateItem[] puis, Map<String, Object> target)
	{
		for (int i = puis.length; i-- > 0;)
		{
			IPrimitiveUpdateItem pui = puis[i];
			target.put(table.getFieldByMemberName(pui.getMemberName()).getName(), pui.getNewValue());
		}
	}

	protected abstract IChangeCommand addCommand(ICreateCommand other);

	protected abstract IChangeCommand addCommand(IUpdateCommand other);

	protected abstract IChangeCommand addCommand(IDeleteCommand other);
}
