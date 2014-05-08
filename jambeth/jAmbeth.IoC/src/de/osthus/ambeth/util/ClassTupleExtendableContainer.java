package de.osthus.ambeth.util;

import java.util.List;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IListElem;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.InterfaceFastList;
import de.osthus.ambeth.collections.Tuple2KeyEntry;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;

public class ClassTupleExtendableContainer<V> extends MapExtendableContainer<ConversionKey, V>
{
	public static class ClassTupleEntry<V> extends Tuple2KeyHashMap<Class<?>, Class<?>, Object>
	{
		public final Tuple2KeyHashMap<Class<?>, Class<?>, Object> typeToDefEntryMap = new Tuple2KeyHashMap<Class<?>, Class<?>, Object>(0.5f);

		public final HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap = new HashMap<Strong2Key<V>, List<Def2Entry<V>>>(0.5f);

		public ClassTupleEntry()
		{
			super(0.5f);
		}
	}

	private static final Object alreadyHandled = new Object();

	protected volatile ClassTupleEntry<V> classEntry = new ClassTupleEntry<V>();

	public ClassTupleExtendableContainer(String message, String keyMessage)
	{
		super(message, keyMessage);
	}

	public ClassTupleExtendableContainer(String message, String keyMessage, boolean multiValue)
	{
		super(message, keyMessage, multiValue);
	}

	@SuppressWarnings("unchecked")
	public V getExtension(Class<?> sourceType, Class<?> targetType)
	{
		Object extension = classEntry.get(sourceType, targetType);
		if (extension == null)
		{
			Lock writeLock = getWriteLock();
			writeLock.lock();
			try
			{
				extension = classEntry.get(sourceType, targetType);
				if (extension == null)
				{
					ClassTupleEntry<V> classEntry = copyStructure();

					classEntry.put(sourceType, targetType, alreadyHandled);
					classEntry.typeToDefEntryMap.put(sourceType, targetType, alreadyHandled);
					checkToWeakRegisterExistingExtensions(sourceType, targetType, classEntry);
					this.classEntry = classEntry;

					extension = classEntry.get(sourceType, targetType);
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
		return (V) extension;
	}

	@Override
	public V getExtension(ConversionKey key)
	{
		return getExtension(key.sourceType, key.targetType);
	}

	@SuppressWarnings("unchecked")
	protected ClassTupleEntry<V> copyStructure()
	{
		ClassTupleEntry<V> newClassEntry = new ClassTupleEntry<V>();
		Tuple2KeyHashMap<Class<?>, Class<?>, Object> newTypeToDefEntryMap = newClassEntry.typeToDefEntryMap;
		HashMap<Strong2Key<V>, List<Def2Entry<V>>> newDefinitionReverseMap = newClassEntry.definitionReverseMap;
		IdentityHashMap<Def2Entry<V>, Def2Entry<V>> originalToCopyMap = new IdentityHashMap<Def2Entry<V>, Def2Entry<V>>();
		for (Tuple2KeyEntry<Class<?>, Class<?>, Object> entry : classEntry.typeToDefEntryMap)
		{
			Class<?> sourceType = entry.getKey1();
			Class<?> targetType = entry.getKey2();
			Object value = entry.getValue();

			if (value == alreadyHandled)
			{
				newTypeToDefEntryMap.put(sourceType, targetType, alreadyHandled);
			}
			else
			{
				InterfaceFastList<Def2Entry<V>> list = (InterfaceFastList<Def2Entry<V>>) value;

				InterfaceFastList<Def2Entry<V>> newList = new InterfaceFastList<Def2Entry<V>>();

				IListElem<Def2Entry<V>> pointer = list.first();
				while (pointer != null)
				{
					Def2Entry<V> defEntry = pointer.getElemValue();
					Def2Entry<V> newDefEntry = new Def2Entry<V>(defEntry.extension, defEntry.sourceType, defEntry.targetType, defEntry.sourceDistance,
							defEntry.targetDistance);
					originalToCopyMap.put(defEntry, newDefEntry);

					newList.pushLast(newDefEntry);
					pointer = pointer.getNext();
				}
				newTypeToDefEntryMap.put(sourceType, targetType, newList);
			}
			typeToDefEntryMapChanged(newClassEntry, sourceType, targetType);
		}
		for (Entry<Strong2Key<V>, List<Def2Entry<V>>> entry : classEntry.definitionReverseMap)
		{
			List<Def2Entry<V>> defEntries = entry.getValue();
			ArrayList<Def2Entry<V>> newDefEntries = new ArrayList<Def2Entry<V>>(defEntries.size());

			for (int a = 0, size = defEntries.size(); a < size; a++)
			{
				Def2Entry<V> newDefEntry = originalToCopyMap.get(defEntries.get(a));
				if (newDefEntry == null)
				{
					throw new IllegalStateException("Must never happen");
				}
				newDefEntries.add(newDefEntry);
			}
			newDefinitionReverseMap.put(entry.getKey(), newDefEntries);
		}
		return newClassEntry;
	}

	protected boolean checkToWeakRegisterExistingExtensions(Class<?> sourceType, Class<?> targetType, ClassTupleEntry<V> classEntry)
	{
		boolean changesHappened = false;
		for (Entry<Strong2Key<V>, List<Def2Entry<V>>> entry : classEntry.definitionReverseMap)
		{
			Strong2Key<V> strongKey = entry.getKey();
			ConversionKey registeredStrongKey = strongKey.key;
			int sourceDistance = ClassExtendableContainer.getDistanceForType(sourceType, registeredStrongKey.sourceType);
			if (sourceDistance == ClassExtendableContainer.NO_VALID_DISTANCE)
			{
				continue;
			}
			int targetDistance = ClassExtendableContainer.getDistanceForType(registeredStrongKey.targetType, targetType);
			if (targetDistance == ClassExtendableContainer.NO_VALID_DISTANCE)
			{
				continue;
			}
			List<Def2Entry<V>> defEntries = entry.getValue();
			for (int a = defEntries.size(); a-- > 0;)
			{
				Def2Entry<V> defEntry = defEntries.get(a);
				changesHappened |= appendRegistration(registeredStrongKey, sourceType, targetType, defEntry.extension, sourceDistance, targetDistance,
						classEntry);
			}
		}
		return changesHappened;
	}

	protected boolean checkToWeakRegisterExistingTypes(ConversionKey key, V extension, ClassTupleEntry<V> classEntry)
	{
		boolean changesHappened = false;
		for (Tuple2KeyEntry<Class<?>, Class<?>, Object> entry : classEntry.typeToDefEntryMap)
		{
			Class<?> existingRequestedSourceType = entry.getKey1();
			Class<?> existingRequestedTargetType = entry.getKey2();
			int sourceDistance = ClassExtendableContainer.getDistanceForType(existingRequestedSourceType, key.sourceType);
			if (sourceDistance == ClassExtendableContainer.NO_VALID_DISTANCE)
			{
				continue;
			}
			int targetDistance = ClassExtendableContainer.getDistanceForType(key.targetType, existingRequestedTargetType);
			if (targetDistance == ClassExtendableContainer.NO_VALID_DISTANCE)
			{
				continue;
			}
			changesHappened |= appendRegistration(key, existingRequestedSourceType, existingRequestedTargetType, extension, sourceDistance, targetDistance,
					classEntry);
		}
		return changesHappened;
	}

	public void register(V extension, Class<?> sourceType, Class<?> targetType)
	{
		ParamChecker.assertParamNotNull(sourceType, "sourceType");
		ParamChecker.assertParamNotNull(targetType, "targetType");
		register(extension, new ConversionKey(sourceType, targetType));
	}

	@Override
	public void register(V extension, ConversionKey key)
	{
		ParamChecker.assertParamNotNull(extension, "extension");
		ParamChecker.assertParamNotNull(key, "key");
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			super.register(extension, key);

			ClassTupleEntry<V> classEntry = copyStructure();
			appendRegistration(key, key.sourceType, key.targetType, extension, 0, 0, classEntry);
			checkToWeakRegisterExistingTypes(key, extension, classEntry);
			checkToWeakRegisterExistingExtensions(key.sourceType, key.targetType, classEntry);
			this.classEntry = classEntry;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void unregister(V extension, Class<?> sourceType, Class<?> targetType)
	{
		ParamChecker.assertParamNotNull(sourceType, "sourceType");
		ParamChecker.assertParamNotNull(targetType, "targetType");
		unregister(extension, new ConversionKey(sourceType, targetType));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unregister(V extension, ConversionKey key)
	{
		ParamChecker.assertParamNotNull(extension, "extension");
		ParamChecker.assertParamNotNull(key, "key");

		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			super.unregister(extension, key);

			ClassTupleEntry<V> classEntry = copyStructure();
			HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap = classEntry.definitionReverseMap;
			List<Def2Entry<V>> weakEntriesOfStrongType = definitionReverseMap.remove(new Strong2Key<V>(extension, key));
			if (weakEntriesOfStrongType == null)
			{
				return;
			}
			Tuple2KeyHashMap<Class<?>, Class<?>, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
			for (int a = weakEntriesOfStrongType.size(); a-- > 0;)
			{
				Def2Entry<V> defEntry = weakEntriesOfStrongType.get(a);
				Class<?> sourceType = defEntry.sourceType;
				Class<?> targetType = defEntry.targetType;

				Object value = typeToDefEntryMap.get(sourceType, targetType);
				InterfaceFastList<Def2Entry<V>> list = (InterfaceFastList<Def2Entry<V>>) value;
				list.remove(defEntry);
				if (list.size() == 0)
				{
					typeToDefEntryMap.remove(sourceType, targetType);
				}
				typeToDefEntryMapChanged(classEntry, sourceType, targetType);
			}
			this.classEntry = classEntry;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	protected void typeToDefEntryMapChanged(ClassTupleEntry<V> classEntry, Class<?> sourceType, Class<?> targetType)
	{
		Object obj = classEntry.typeToDefEntryMap.get(sourceType, targetType);
		if (obj == null)
		{
			classEntry.remove(sourceType, targetType);
			return;
		}
		if (obj == alreadyHandled)
		{
			classEntry.put(sourceType, targetType, alreadyHandled);
			return;
		}
		if (obj instanceof Def2Entry)
		{
			classEntry.put(sourceType, targetType, ((Def2Entry<V>) obj).extension);
			return;
		}
		Def2Entry<V> firstDefEntry = ((InterfaceFastList<Def2Entry<V>>) obj).first().getElemValue();
		classEntry.put(sourceType, targetType, firstDefEntry.extension);
	}

	@SuppressWarnings("unchecked")
	protected boolean appendRegistration(ConversionKey strongTypeKey, Class<?> sourceType, Class<?> targetType, V extension, int sourceDistance,
			int targetDistance, ClassTupleEntry<V> classEntry)
	{
		Tuple2KeyHashMap<Class<?>, Class<?>, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
		Object fastList = typeToDefEntryMap.get(sourceType, targetType);
		if (fastList != null && fastList != alreadyHandled)
		{
			IListElem<Def2Entry<V>> pointer = ((InterfaceFastList<Def2Entry<V>>) fastList).first();
			while (pointer != null)
			{
				Def2Entry<V> existingDefEntry = pointer.getElemValue();
				if (existingDefEntry.extension == extension && existingDefEntry.sourceDistance == sourceDistance
						&& existingDefEntry.targetDistance == targetDistance)
				{
					// DefEntry already exists with same distance
					return false;
				}
				pointer = pointer.getNext();
			}
		}
		if (fastList == null || fastList == alreadyHandled)
		{
			fastList = new InterfaceFastList<Def2Entry<V>>();
			typeToDefEntryMap.put(sourceType, targetType, fastList);
		}
		Def2Entry<V> defEntry = new Def2Entry<V>(extension, sourceType, targetType, sourceDistance, targetDistance);

		HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap = classEntry.definitionReverseMap;
		Strong2Key<V> strongKey = new Strong2Key<V>(extension, strongTypeKey);
		List<Def2Entry<V>> typeEntries = definitionReverseMap.get(strongKey);
		if (typeEntries == null)
		{
			typeEntries = new ArrayList<Def2Entry<V>>();
			definitionReverseMap.put(strongKey, typeEntries);
		}
		typeEntries.add(defEntry);

		InterfaceFastList.insertOrdered((InterfaceFastList<Def2Entry<V>>) fastList, defEntry);
		typeToDefEntryMapChanged(classEntry, sourceType, targetType);
		return true;
	}
}