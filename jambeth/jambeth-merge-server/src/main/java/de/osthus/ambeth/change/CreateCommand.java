package de.osthus.ambeth.change;

import java.util.Map.Entry;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.persistence.ITable;

public class CreateCommand extends AbstractChangeCommand implements ICreateCommand
{
	protected final LinkedHashMap<String, Object> items = new LinkedHashMap<String, Object>();

	@Override
	public void dispose()
	{
		super.dispose();
		items.clear();
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		CreateContainer container = (CreateContainer) changeContainer;
		super.configureFromContainer(container, table);

		if (container.getPrimitives() != null)
		{
			repackPuis(container.getPrimitives(), this.items);
		}
	}

	@Override
	protected IChangeCommand addCommand(ICreateCommand other)
	{
		throw new IllegalCommandException("Duplicate create command!");
	}

	@Override
	public IChangeCommand addCommand(IUpdateCommand other)
	{
		LinkedHashMap<String, Object> items = this.items;
		for (Entry<String, Object> entry : other.getItems())
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
	public ILinkedMap<String, Object> getItems()
	{
		return this.items;
	}
}
