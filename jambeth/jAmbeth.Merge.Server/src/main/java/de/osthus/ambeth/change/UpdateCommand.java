package de.osthus.ambeth.change;

import java.util.Map.Entry;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.ReadOnlyMapWrapper;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.persistence.ITable;

public class UpdateCommand extends AbstractChangeCommand implements IUpdateCommand
{
	private static final ILinkedMap<String, Object> emtpyItems = new ReadOnlyMapWrapper<String, Object>(new LinkedHashMap<String, Object>());

	protected ILinkedMap<String, Object> items = emtpyItems;
	protected ILinkedMap<String, Object> roItems = emtpyItems;

	@Override
	public void dispose()
	{
		super.dispose();
		if (items != emtpyItems)
		{
			items.clear();
			items = null;
			roItems = null;
		}
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		UpdateContainer container = (UpdateContainer) changeContainer;
		super.configureFromContainer(container, table);

		if (container.getPrimitives() != null)
		{
			ensureWritableMap();
			repackPuis(container.getPrimitives(), items);
		}
	}

	@Override
	public IChangeCommand addCommand(ICreateCommand other)
	{
		return other.addCommand(this);
	}

	@Override
	public IChangeCommand addCommand(IUpdateCommand other)
	{
		IMap<String, Object> otherItems = other.getItems();
		if (otherItems != emtpyItems)
		{
			for (Entry<String, Object> entry : otherItems)
			{
				Object actualValue = items.get(entry.getKey());
				if (actualValue == null)
				{
					ensureWritableMap();
					items.put(entry.getKey(), entry.getValue());
				}
				else if (entry.getValue() != null && !actualValue.equals(entry.getValue()))
				{
					throw new IllegalCommandException("Two different values for the same field (" + entry.getKey() + ": '" + actualValue + "' <-> '"
							+ entry.getValue() + "'");
				}
			}
		}

		return this;
	}

	@Override
	public IChangeCommand addCommand(IDeleteCommand other)
	{
		return other;
	}

	public void put(String fieldName, Object foreignKey)
	{
		ensureWritableMap();
		items.put(fieldName, foreignKey);
	}

	@Override
	public ILinkedMap<String, Object> getItems()
	{
		return roItems;
	}

	protected void ensureWritableMap()
	{
		if (items == emtpyItems)
		{
			items = new LinkedHashMap<String, Object>();
			roItems = new ReadOnlyMapWrapper<String, Object>(items);
		}
	}
}
