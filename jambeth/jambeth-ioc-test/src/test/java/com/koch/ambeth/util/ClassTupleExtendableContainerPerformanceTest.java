package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-ioc-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.ioc.util.ClassTupleExtendableContainer;
import com.koch.ambeth.ioc.util.ConversionKey;
import com.koch.ambeth.ioc.util.Def2Entry;
import com.koch.ambeth.ioc.util.Strong2Key;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.category.PerformanceTests;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IListElem;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.InterfaceFastList;

@Category(PerformanceTests.class)
public class ClassTupleExtendableContainerPerformanceTest extends AbstractIocTest {
	/**
	 * Old ClassTupleExtendableContainer with a normal HashMap instead of the new Tuple2 optimized
	 * HashMap. This class here is just for performance comparisions
	 *
	 * @param <V>
	 */
	public static class ClassTupleExtendableContainerOld<V>
			extends MapExtendableContainer<ConversionKey, V> {
		public static class ClassTupleEntry<V> extends HashMap<ConversionKey, Object> {
			public final HashMap<ConversionKey, Object> typeToDefEntryMap =
					new HashMap<>(0.5f);

			public final HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap =
					new HashMap<>(0.5f);

			public ClassTupleEntry() {
				super(0.5f);
			}
		}

		private static final Object alreadyHandled = new Object();

		protected volatile ClassTupleEntry<V> classEntry = new ClassTupleEntry<>();

		public ClassTupleExtendableContainerOld(String message, String keyMessage) {
			super(message, keyMessage);
		}

		public ClassTupleExtendableContainerOld(String message, String keyMessage, boolean multiValue) {
			super(message, keyMessage, multiValue);
		}

		public V getExtension(Class<?> sourceType, Class<?> targetType) {
			return getExtension(new ConversionKey(sourceType, targetType));
		}

		@SuppressWarnings("unchecked")
		@Override
		public V getExtension(ConversionKey key) {
			Object extension = classEntry.get(key);
			if (extension == null) {
				java.util.concurrent.locks.Lock writeLock = getWriteLock();
				writeLock.lock();
				try {
					extension = classEntry.get(key);
					if (extension == null) {
						ClassTupleEntry<V> classEntry = copyStructure();

						classEntry.put(key, alreadyHandled);
						classEntry.typeToDefEntryMap.put(key, alreadyHandled);
						checkToWeakRegisterExistingExtensions(key, classEntry);
						this.classEntry = classEntry;

						extension = classEntry.get(key);
						if (extension == null) {
							return null;
						}
					}
				}
				finally {
					writeLock.unlock();
				}
			}
			if (extension == alreadyHandled) {
				// Already tried
				return null;
			}
			return (V) extension;
		}

		@SuppressWarnings("unchecked")
		protected ClassTupleEntry<V> copyStructure() {
			ClassTupleEntry<V> newClassEntry = new ClassTupleEntry<>();
			HashMap<ConversionKey, Object> newTypeToDefEntryMap = newClassEntry.typeToDefEntryMap;
			HashMap<Strong2Key<V>, List<Def2Entry<V>>> newDefinitionReverseMap =
					newClassEntry.definitionReverseMap;
			IdentityHashMap<Def2Entry<V>, Def2Entry<V>> originalToCopyMap =
					new IdentityHashMap<>();
			{
				for (Entry<ConversionKey, Object> entry : classEntry.typeToDefEntryMap) {
					ConversionKey key = entry.getKey();
					Object value = entry.getValue();

					if (value == alreadyHandled) {
						newTypeToDefEntryMap.put(key, alreadyHandled);
					}
					else {
						InterfaceFastList<Def2Entry<V>> list = (InterfaceFastList<Def2Entry<V>>) value;

						InterfaceFastList<Def2Entry<V>> newList = new InterfaceFastList<>();

						IListElem<Def2Entry<V>> pointer = list.first();
						while (pointer != null) {
							Def2Entry<V> defEntry = pointer.getElemValue();
							Def2Entry<V> newDefEntry = new Def2Entry<>(defEntry.extension, defEntry.sourceType,
									defEntry.targetType, defEntry.sourceDistance, defEntry.targetDistance);
							originalToCopyMap.put(defEntry, newDefEntry);

							newList.pushLast(newDefEntry);
							pointer = pointer.getNext();
						}
						newTypeToDefEntryMap.put(key, newList);
					}
					typeToDefEntryMapChanged(newClassEntry, key);
				}
			}
			for (Entry<Strong2Key<V>, List<Def2Entry<V>>> entry : classEntry.definitionReverseMap) {
				List<Def2Entry<V>> defEntries = entry.getValue();
				ArrayList<Def2Entry<V>> newDefEntries = new ArrayList<>(defEntries.size());

				for (int a = 0, size = defEntries.size(); a < size; a++) {
					Def2Entry<V> newDefEntry = originalToCopyMap.get(defEntries.get(a));
					if (newDefEntry == null) {
						throw new IllegalStateException("Must never happen");
					}
					newDefEntries.add(newDefEntry);
				}
				newDefinitionReverseMap.put(entry.getKey(), newDefEntries);
			}
			return newClassEntry;
		}

		protected boolean checkToWeakRegisterExistingExtensions(ConversionKey conversionKey,
				ClassTupleEntry<V> classEntry) {
			boolean changesHappened = false;
			for (Entry<Strong2Key<V>, List<Def2Entry<V>>> entry : classEntry.definitionReverseMap) {
				Strong2Key<V> strongKey = entry.getKey();
				ConversionKey registeredStrongKey = strongKey.getKey();
				int sourceDistance = ClassExtendableContainer.getDistanceForType(conversionKey.sourceType,
						registeredStrongKey.sourceType);
				if (sourceDistance == ClassExtendableContainer.NO_VALID_DISTANCE) {
					continue;
				}
				int targetDistance = ClassExtendableContainer
						.getDistanceForType(registeredStrongKey.targetType, conversionKey.targetType);
				if (targetDistance == ClassExtendableContainer.NO_VALID_DISTANCE) {
					continue;
				}
				List<Def2Entry<V>> defEntries = entry.getValue();
				for (int a = defEntries.size(); a-- > 0;) {
					Def2Entry<V> defEntry = defEntries.get(a);
					changesHappened |= appendRegistration(registeredStrongKey, conversionKey,
							defEntry.extension, sourceDistance, targetDistance, classEntry);
				}
			}
			return changesHappened;
		}

		protected boolean checkToWeakRegisterExistingTypes(ConversionKey key, V extension,
				ClassTupleEntry<V> classEntry) {
			boolean changesHappened = false;
			for (Entry<ConversionKey, Object> entry : classEntry.typeToDefEntryMap) {
				ConversionKey existingRequestedKey = entry.getKey();
				int sourceDistance = ClassExtendableContainer
						.getDistanceForType(existingRequestedKey.sourceType, key.sourceType);
				if (sourceDistance == ClassExtendableContainer.NO_VALID_DISTANCE) {
					continue;
				}
				int targetDistance = ClassExtendableContainer.getDistanceForType(key.targetType,
						existingRequestedKey.targetType);
				if (targetDistance == ClassExtendableContainer.NO_VALID_DISTANCE) {
					continue;
				}
				changesHappened |= appendRegistration(key, existingRequestedKey, extension, sourceDistance,
						targetDistance, classEntry);
			}
			return changesHappened;
		}

		public void register(V extension, Class<?> sourceType, Class<?> targetType) {
			ParamChecker.assertParamNotNull(sourceType, "sourceType");
			ParamChecker.assertParamNotNull(targetType, "targetType");
			register(extension, new ConversionKey(sourceType, targetType));
		}

		@Override
		public void register(V extension, ConversionKey key) {
			ParamChecker.assertParamNotNull(extension, "extension");
			ParamChecker.assertParamNotNull(key, "key");
			java.util.concurrent.locks.Lock writeLock = getWriteLock();
			writeLock.lock();
			try {
				super.register(extension, key);

				ClassTupleEntry<V> classEntry = copyStructure();
				appendRegistration(key, key, extension, 0, 0, classEntry);
				checkToWeakRegisterExistingTypes(key, extension, classEntry);
				checkToWeakRegisterExistingExtensions(key, classEntry);
				this.classEntry = classEntry;
			}
			finally {
				writeLock.unlock();
			}
		}

		public void unregister(V extension, Class<?> sourceType, Class<?> targetType) {
			ParamChecker.assertParamNotNull(sourceType, "sourceType");
			ParamChecker.assertParamNotNull(targetType, "targetType");
			unregister(extension, new ConversionKey(sourceType, targetType));
		}

		@SuppressWarnings("unchecked")
		@Override
		public void unregister(V extension, ConversionKey key) {
			ParamChecker.assertParamNotNull(extension, "extension");
			ParamChecker.assertParamNotNull(key, "key");

			java.util.concurrent.locks.Lock writeLock = getWriteLock();
			writeLock.lock();
			try {
				super.unregister(extension, key);

				ClassTupleEntry<V> classEntry = copyStructure();
				HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap =
						classEntry.definitionReverseMap;
				List<Def2Entry<V>> weakEntriesOfStrongType =
						definitionReverseMap.remove(new Strong2Key<>(extension, key));
				if (weakEntriesOfStrongType == null) {
					return;
				}
				HashMap<ConversionKey, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
				for (int a = weakEntriesOfStrongType.size(); a-- > 0;) {
					Def2Entry<V> defEntry = weakEntriesOfStrongType.get(a);
					ConversionKey registeredKey = new ConversionKey(defEntry.sourceType, defEntry.targetType);

					Object value = typeToDefEntryMap.get(registeredKey);
					InterfaceFastList<Def2Entry<V>> list = (InterfaceFastList<Def2Entry<V>>) value;
					list.remove(defEntry);
					if (list.isEmpty()) {
						typeToDefEntryMap.remove(registeredKey);
					}
					typeToDefEntryMapChanged(classEntry, registeredKey);
				}
				this.classEntry = classEntry;
			}
			finally {
				writeLock.unlock();
			}
		}

		@SuppressWarnings("unchecked")
		protected void typeToDefEntryMapChanged(ClassTupleEntry<V> classEntry, ConversionKey key) {
			Object obj = classEntry.typeToDefEntryMap.get(key);
			if (obj == null) {
				classEntry.remove(key);
				return;
			}
			if (obj == alreadyHandled) {
				classEntry.put(key, alreadyHandled);
				return;
			}
			if (obj instanceof Def2Entry) {
				classEntry.put(key, ((Def2Entry<V>) obj).extension);
				return;
			}
			Def2Entry<V> firstDefEntry = ((InterfaceFastList<Def2Entry<V>>) obj).first().getElemValue();
			classEntry.put(key, firstDefEntry.extension);
		}

		@SuppressWarnings("unchecked")
		protected boolean appendRegistration(ConversionKey strongTypeKey, ConversionKey key,
				V extension, int sourceDistance, int targetDistance, ClassTupleEntry<V> classEntry) {
			HashMap<ConversionKey, Object> typeToDefEntryMap = classEntry.typeToDefEntryMap;
			Object fastList = typeToDefEntryMap.get(key);
			if (fastList != null && fastList != alreadyHandled) {
				IListElem<Def2Entry<V>> pointer = ((InterfaceFastList<Def2Entry<V>>) fastList).first();
				while (pointer != null) {
					Def2Entry<V> existingDefEntry = pointer.getElemValue();
					if (existingDefEntry.extension == extension
							&& existingDefEntry.sourceDistance == sourceDistance
							&& existingDefEntry.targetDistance == targetDistance) {
						// DefEntry already exists with same distance
						return false;
					}
					pointer = pointer.getNext();
				}
			}
			if (fastList == null || fastList == alreadyHandled) {
				fastList = new InterfaceFastList<Def2Entry<V>>();
				typeToDefEntryMap.put(key, fastList);
			}
			Def2Entry<V> defEntry = new Def2Entry<>(extension, key.sourceType, key.targetType,
					sourceDistance, targetDistance);

			HashMap<Strong2Key<V>, List<Def2Entry<V>>> definitionReverseMap =
					classEntry.definitionReverseMap;
			Strong2Key<V> strongKey = new Strong2Key<>(extension, strongTypeKey);
			List<Def2Entry<V>> typeEntries = definitionReverseMap.get(strongKey);
			if (typeEntries == null) {
				typeEntries = new ArrayList<>();
				definitionReverseMap.put(strongKey, typeEntries);
			}
			typeEntries.add(defEntry);

			InterfaceFastList.insertOrdered((InterfaceFastList<Def2Entry<V>>) fastList, defEntry);
			typeToDefEntryMapChanged(classEntry, key);
			return true;
		}
	}

	@LogInstance
	private ILogger log;

	@Test
	public void performance() {
		ClassTupleExtendableContainer<String> newC =
				new ClassTupleExtendableContainer<>("message", "keyMessage");
		ClassTupleExtendableContainerOld<String> oldC =
				new ClassTupleExtendableContainerOld<>("message", "keyMessage");

		register(newC, oldC, "e", LinkedList.class, String.class);
		register(newC, oldC, "a", ArrayList.class, CharSequence.class);
		register(newC, oldC, "b", List.class, CharSequence.class);
		register(newC, oldC, "c", Collection.class, String.class);
		register(newC, oldC, "d", Collection.class, CharSequence.class);

		newC.getExtension(ArrayList.class, CharSequence.class);
		oldC.getExtension(ArrayList.class, CharSequence.class);

		int count = 1500 * 1000 * 1000;
		for (int a = count; a-- > 0; a--) {
			newC.getExtension(ArrayList.class, CharSequence.class);
		}
		for (int a = count; a-- > 0; a--) {
			oldC.getExtension(ArrayList.class, CharSequence.class);
		}
		long start = System.currentTimeMillis();
		for (int a = count; a-- > 0; a--) {
			oldC.getExtension(ArrayList.class, CharSequence.class);
		}
		long endOld = System.currentTimeMillis();
		for (int a = count; a-- > 0; a--) {
			newC.getExtension(ArrayList.class, CharSequence.class);
		}
		long endNew = System.currentTimeMillis();
		long newSpent = endNew - endOld;
		long oldSpent = endOld - start;
		log.info("New: " + newSpent + "ms, Old: " + oldSpent + "ms");
		Assert.assertTrue(newSpent <= oldSpent * 0.66f); // new approach should spent at most 66% of the
																											// old approach
	}

	protected void register(ClassTupleExtendableContainer<String> newC,
			ClassTupleExtendableContainerOld<String> oldC, String extension, Class<?> from, Class<?> to) {
		newC.register(extension, from, to);
		oldC.register(extension, from, to);
	}
}
