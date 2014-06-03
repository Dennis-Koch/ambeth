package de.osthus.ambeth.ioc.extendable;

import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IListElem;
import de.osthus.ambeth.collections.InterfaceFastList;

public class ClassExtendableListContainer<V> extends ClassExtendableContainer<V>
{
	public ClassExtendableListContainer(String message, String keyMessage)
	{
		super(message, keyMessage, true);
	}

	@Override
	public V getExtension(Class<?> key)
	{
		return getExtensions(key).get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IList<V> getExtensions(Class<?> key)
	{
		if (key == null)
		{
			return null;
		}
		Object extension = classEntry.get(key);
		if (extension == null)
		{
			Lock writeLock = getWriteLock();
			writeLock.lock();
			try
			{
				extension = classEntry.get(key);
				if (extension == null)
				{
					ClassEntry<V> classEntry = copyStructure();

					classEntry.put(key, alreadyHandled);
					classEntry.typeToDefEntryMap.put(key, alreadyHandled);
					checkToWeakRegisterExistingExtensions(key, classEntry);
					this.classEntry = classEntry;

					extension = classEntry.get(key);
					if (extension == null)
					{
						return null;
					}
				}
			}
			finally
			{
				writeLock.unlock();
			}
		}
		if (extension == alreadyHandled)
		{
			// Already tried
			return null;
		}
		return (IList<V>) extension;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void typeToDefEntryMapChanged(ClassEntry<V> classEntry, Class<?> key)
	{
		Object obj = classEntry.typeToDefEntryMap.get(key);
		if (obj == null)
		{
			classEntry.remove(key);
			return;
		}
		if (obj == alreadyHandled)
		{
			classEntry.put(key, alreadyHandled);
			return;
		}
		Object existingItem = classEntry.get(key);
		ArrayList<V> list = (ArrayList<V>) (existingItem == alreadyHandled ? null : existingItem);
		if (list == null)
		{
			list = new ArrayList<V>();
			classEntry.put(key, list);
		}
		if (obj instanceof DefEntry)
		{
			V extension = ((DefEntry<V>) obj).extension;
			if (!list.contains(extension))
			{
				list.add(extension);
			}
			return;
		}
		IListElem<DefEntry<V>> pointer = ((InterfaceFastList<DefEntry<V>>) obj).first();
		while (pointer != null)
		{
			V extension = pointer.getElemValue().extension;
			if (!list.contains(extension))
			{
				list.add(extension);
			}
			pointer = pointer.getNext();
		}
	}
}