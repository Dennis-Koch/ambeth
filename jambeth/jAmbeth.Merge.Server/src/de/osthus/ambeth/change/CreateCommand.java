package de.osthus.ambeth.change;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.persistence.ITable;

public class CreateCommand extends AbstractChangeCommand implements ICreateCommand
{

	protected final ILinkedMap<String, Object> items = new LinkedHashMap<String, Object>();

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
		Map<String, Object> items = other.getItems();
		Iterator<Entry<String, Object>> entryIter = items.entrySet().iterator();
		while (entryIter.hasNext())
		{
			Entry<String, Object> entry = entryIter.next();
			if (entry.getValue() != null)
			{
				this.items.put(entry.getKey(), entry.getValue());
			}
			else
			{
				this.items.putIfNotExists(entry.getKey(), entry.getValue());
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
