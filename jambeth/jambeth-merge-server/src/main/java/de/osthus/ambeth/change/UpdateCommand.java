package de.osthus.ambeth.change;

import java.util.Map.Entry;

import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.ReadOnlyMapWrapper;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.ICreateOrUpdateContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.persistence.ITable;

public class UpdateCommand extends AbstractChangeCommand implements IUpdateCommand
{
	private static final ILinkedMap<String, Object> emptyItems = EmptyMap.<String, Object> emptyMap();

	protected ILinkedMap<String, Object> items = emptyItems;
	protected ILinkedMap<String, Object> roItems = emptyItems;

	public UpdateCommand(IObjRef reference)
	{
		super(reference);
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table)
	{
		ICreateOrUpdateContainer container = (ICreateOrUpdateContainer) changeContainer;
		super.configureFromContainer(changeContainer, table);

		IPrimitiveUpdateItem[] fullPUIs = container.getFullPUIs();
		if (fullPUIs != null)
		{
			ensureWritableMap();
			repackPuis(fullPUIs, items);
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
		if (otherItems != emptyItems)
		{
			for (Entry<String, Object> entry : otherItems)
			{
				Object actualValue = items.get(entry.getKey());
				if (actualValue == null)
				{
					put(entry.getKey(), entry.getValue());
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
		if (items == emptyItems)
		{
			items = new LinkedHashMap<String, Object>();
			roItems = new ReadOnlyMapWrapper<String, Object>(items);
		}
	}
}
