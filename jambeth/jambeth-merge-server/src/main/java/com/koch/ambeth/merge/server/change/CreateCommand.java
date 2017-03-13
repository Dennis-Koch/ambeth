package com.koch.ambeth.merge.server.change;

import java.util.Map.Entry;

import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedMap;

public class CreateCommand extends AbstractChangeCommand implements ICreateCommand
{
	protected final IdentityLinkedMap<IFieldMetaData, Object> items = new IdentityLinkedMap<IFieldMetaData, Object>();

	public CreateCommand(IObjRef reference)
	{
		super(reference);
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		super.configureFromContainer(changeContainer, table);

		repackPuis(((ICreateOrUpdateContainer) changeContainer).getFullPUIs(), items);
	}

	@Override
	protected IChangeCommand addCommand(ICreateCommand other)
	{
		throw new IllegalCommandException("Duplicate create command!");
	}

	@Override
	public IChangeCommand addCommand(IUpdateCommand other)
	{
		IdentityLinkedMap<IFieldMetaData, Object> items = this.items;
		for (Entry<IFieldMetaData, Object> entry : other.getItems())
		{
			if (entry.getValue() != null)
			{
				items.put(entry.getKey(), entry.getValue());
			}
			else
			{
				items.putIfNotExists(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}

	@Override
	protected IChangeCommand addCommand(IDeleteCommand other)
	{
		throw new IllegalCommandException("Delete command for an entity to be created: " + other.getReference());
	}

	@Override
	public ILinkedMap<IFieldMetaData, Object> getItems()
	{
		return items;
	}
}
