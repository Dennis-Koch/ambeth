package de.osthus.ambeth.change;

import java.util.Map.Entry;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.ICreateOrUpdateContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.ITable;

public class CreateCommand extends AbstractChangeCommand implements ICreateCommand
{
	protected final LinkedHashMap<String, Object> items = new LinkedHashMap<String, Object>();

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
		return items;
	}
}
