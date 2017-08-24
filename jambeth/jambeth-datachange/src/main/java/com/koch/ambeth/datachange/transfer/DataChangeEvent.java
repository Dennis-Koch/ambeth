package com.koch.ambeth.datachange.transfer;

/*-
 * #%L
 * jambeth-datachange
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.util.collections.HashSet;

@XmlRootElement(name = "DataChangeEvent", namespace = "http://schema.kochdev.com/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataChangeEvent implements IDataChange {
	public static DataChangeEvent create(int insertCount, int updateCount, int deleteCount) {
		DataChangeEvent dce = new DataChangeEvent();
		dce.setLocalSource(true);
		dce.setChangeTime(System.currentTimeMillis());
		dce.setDeletes(createList(deleteCount));
		dce.setUpdates(createList(updateCount));
		dce.setInserts(createList(insertCount));
		return dce;
	}

	protected static List<IDataChangeEntry> createList(int size) {
		if (size == -1) {
			return new ArrayList<>();
		}
		else if (size == 0) {
			return Collections.emptyList();
		}
		return new ArrayList<>(size);
	}

	@XmlElement(required = true)
	protected long changeTime;

	@XmlElement(required = true)
	protected List<IDataChangeEntry> deletes;

	@XmlElement(required = true)
	protected List<IDataChangeEntry> updates;

	@XmlElement(required = true)
	protected List<IDataChangeEntry> inserts;

	@XmlElement(required = false)
	protected String[] causingUUIDs;

	protected transient List<IDataChangeEntry> all;

	protected transient boolean isLocalSource;

	public DataChangeEvent() {
		// Intended blank
	}

	public DataChangeEvent(List<IDataChangeEntry> inserts, List<IDataChangeEntry> updates,
			List<IDataChangeEntry> deletes, long changeTime, boolean isLocalSource) {
		this.inserts = inserts;
		this.updates = updates;
		this.deletes = deletes;
		this.changeTime = changeTime;
		this.isLocalSource = isLocalSource;
	}

	@Override
	public String[] getCausingUUIDs() {
		return causingUUIDs;
	}

	public void setCausingUUIDs(String[] causingUUIDs) {
		this.causingUUIDs = causingUUIDs;
	}

	@Override
	public long getChangeTime() {
		return changeTime;
	}

	public void setChangeTime(long changeTime) {
		this.changeTime = changeTime;
	}

	@Override
	public List<IDataChangeEntry> getDeletes() {
		return deletes;
	}

	public void setDeletes(List<IDataChangeEntry> deletes) {
		this.deletes = deletes;
	}

	@Override
	public List<IDataChangeEntry> getUpdates() {
		return updates;
	}

	public void setUpdates(List<IDataChangeEntry> updates) {
		this.updates = updates;
	}

	@Override
	public List<IDataChangeEntry> getInserts() {
		return inserts;
	}

	public void setInserts(List<IDataChangeEntry> inserts) {
		this.inserts = inserts;
	}

	@Override
	public List<IDataChangeEntry> getAll() {
		if (all == null) {
			int size = inserts.size() + updates.size() + deletes.size();
			List<IDataChangeEntry> allList;
			if (size > 0) {
				allList = new ArrayList<>(size);
				allList.addAll(inserts);
				allList.addAll(updates);
				allList.addAll(deletes);
			}
			else {
				allList = Collections.emptyList();
			}
			all = allList;
		}
		return all;
	}

	@Override
	public boolean isEmpty() {
		return deletes.isEmpty() && updates.isEmpty() && inserts.isEmpty();
	}

	@Override
	public boolean isEmptyByType(Class<?> entityType) {
		List<IDataChangeEntry> entries = inserts;
		for (int a = entries.size(); a-- > 0;) {
			if (entries.get(a).getEntityType().isAssignableFrom(entityType)) {
				return false;
			}
		}
		entries = updates;
		for (int a = entries.size(); a-- > 0;) {
			if (entries.get(a).getEntityType().isAssignableFrom(entityType)) {
				return false;
			}
		}
		entries = deletes;
		for (int a = entries.size(); a-- > 0;) {
			if (entries.get(a).getEntityType().isAssignableFrom(entityType)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLocalSource() {
		return isLocalSource;
	}

	public void setLocalSource(boolean isLocalSource) {
		this.isLocalSource = isLocalSource;
	}

	@Override
	public IDataChange derive(Class<?>... interestedEntityTypes) {
		return deriveIntern(interestedEntityTypes, false);
	}

	@Override
	public IDataChange deriveNot(Class<?>... uninterestingEntityTypes) {
		return deriveIntern(uninterestingEntityTypes, true);
	}

	@Override
	public IDataChange derive(Object... interestedEntityIds) {
		Set<Object> interestedEntityIdsSet = interestedEntityIds.length == 0
				? Collections.<Object>emptySet()
				: new HashSet<>(interestedEntityIds);

		List<IDataChangeEntry> derivedInserts = buildDerivedIds(inserts, interestedEntityIdsSet);
		List<IDataChangeEntry> derivedUpdates = buildDerivedIds(updates, interestedEntityIdsSet);
		List<IDataChangeEntry> derivedDeletes = buildDerivedIds(deletes, interestedEntityIdsSet);

		return new DataChangeEvent(derivedInserts, derivedUpdates, derivedDeletes, changeTime,
				isLocalSource);
	}

	protected IDataChange deriveIntern(Class<?>[] entityTypes, boolean reverse) {
		Set<Class<?>> entityTypesSet = entityTypes.length == 0 ? Collections.<Class<?>>emptySet()
				: new HashSet<>(entityTypes);

		List<IDataChangeEntry> derivedInserts = buildDerivedTypes(inserts, entityTypesSet, reverse);
		List<IDataChangeEntry> derivedUpdates = buildDerivedTypes(updates, entityTypesSet, reverse);
		List<IDataChangeEntry> derivedDeletes = buildDerivedTypes(deletes, entityTypesSet, reverse);

		return new DataChangeEvent(derivedInserts, derivedUpdates, derivedDeletes, changeTime,
				isLocalSource);
	}

	protected List<IDataChangeEntry> buildDerivedTypes(List<IDataChangeEntry> sourceEntries,
			Set<Class<?>> entityTypes, boolean reverse) {
		List<IDataChangeEntry> targetEntries = null;
		for (int a = 0, size = sourceEntries.size(); a < size; a++) {
			IDataChangeEntry entry = sourceEntries.get(a);
			Class<?> currentType = entry.getEntityType();
			while (currentType != null) {
				boolean include = entityTypes.contains(currentType) ^ reverse;
				if (include) {
					if (targetEntries == null) {
						targetEntries = new ArrayList<>(size - a); // Max potential match-count
					}
					targetEntries.add(entry);
					break;
				}
				currentType = currentType.getSuperclass();
				if (reverse && currentType == Object.class) {
					break;
				}
			}
		}
		if (targetEntries == null) {
			targetEntries = Collections.emptyList();
		}
		return targetEntries;
	}

	protected List<IDataChangeEntry> buildDerivedIds(List<IDataChangeEntry> sourceEntries,
			Set<Object> interestedEntityIds) {
		List<IDataChangeEntry> targetEntries = null;
		for (int a = 0, size = sourceEntries.size(); a < size; a++) {
			IDataChangeEntry entry = sourceEntries.get(a);
			if (interestedEntityIds.contains(entry.getId())) {
				if (targetEntries == null) {
					targetEntries = new ArrayList<>(size - a); // Max potential match-count
				}
				targetEntries.add(entry);
			}
		}
		if (targetEntries == null) {
			targetEntries = Collections.emptyList();
		}
		return targetEntries;
	}
}
