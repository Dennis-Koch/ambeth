package com.koch.ambeth.merge.server.change;

import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ILinkedMap;

public abstract class AbstractChangeCommand implements IChangeCommand
{
	protected final IObjRef reference;

	protected ITable table;

	public AbstractChangeCommand(IObjRef reference)
	{
		this.reference = reference;
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		this.table = table;
	}

	@Override
	public IObjRef getReference()
	{
		return reference;
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
		return this.getClass() + " for " + reference;
	}

	protected void repackPuis(IPrimitiveUpdateItem[] puis, ILinkedMap<IFieldMetaData, Object> target)
	{
		if (puis == null)
		{
			return;
		}
		ITableMetaData table = this.table.getMetaData();
		for (int i = puis.length; i-- > 0;)
		{
			IPrimitiveUpdateItem pui = puis[i];
			if (pui == null)
			{
				continue;
			}
			IFieldMetaData field = table.getFieldByMemberName(pui.getMemberName());
			if (field == null)
			{
				// field is transient
				continue;
			}
			target.put(field, pui.getNewValue());
		}
	}

	protected abstract IChangeCommand addCommand(ICreateCommand other);

	protected abstract IChangeCommand addCommand(IUpdateCommand other);

	protected abstract IChangeCommand addCommand(IDeleteCommand other);
}
