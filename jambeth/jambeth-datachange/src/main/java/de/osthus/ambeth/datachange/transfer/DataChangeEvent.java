package de.osthus.ambeth.datachange.transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;

@XmlRootElement(name = "DataChangeEvent", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataChangeEvent implements IDataChange
{
	public static DataChangeEvent create(int insertCount, int updateCount, int deleteCount)
	{
		DataChangeEvent dce = new DataChangeEvent();
		dce.setLocalSource(true);
		dce.setChangeTime(System.currentTimeMillis());
		dce.setDeletes(createList(deleteCount));
		dce.setUpdates(createList(updateCount));
		dce.setInserts(createList(insertCount));
		return dce;
	}

	protected static List<IDataChangeEntry> createList(int size)
	{
		if (size == -1)
		{
			return new ArrayList<IDataChangeEntry>();
		}
		else if (size == 0)
		{
			return Collections.emptyList();
		}
		return new ArrayList<IDataChangeEntry>(size);
	}

	@XmlElement(required = true)
	protected long changeTime;

	@XmlElement(required = true)
	protected List<IDataChangeEntry> deletes;

	@XmlElement(required = true)
	protected List<IDataChangeEntry> updates;

	@XmlElement(required = true)
	protected List<IDataChangeEntry> inserts;

	protected transient List<IDataChangeEntry> all;

	protected transient boolean isLocalSource;

	public DataChangeEvent()
	{
		// Intended blank
	}

	public DataChangeEvent(List<IDataChangeEntry> inserts, List<IDataChangeEntry> updates, List<IDataChangeEntry> deletes, long changeTime,
			boolean isLocalSource)
	{
		this.inserts = inserts;
		this.updates = updates;
		this.deletes = deletes;
		this.changeTime = changeTime;
		this.isLocalSource = isLocalSource;
	}

	@Override
	public long getChangeTime()
	{
		return changeTime;
	}

	public void setChangeTime(long changeTime)
	{
		this.changeTime = changeTime;
	}

	@Override
	public List<IDataChangeEntry> getDeletes()
	{
		return deletes;
	}

	public void setDeletes(List<IDataChangeEntry> deletes)
	{
		this.deletes = deletes;
	}

	@Override
	public List<IDataChangeEntry> getUpdates()
	{
		return updates;
	}

	public void setUpdates(List<IDataChangeEntry> updates)
	{
		this.updates = updates;
	}

	@Override
	public List<IDataChangeEntry> getInserts()
	{
		return inserts;
	}

	public void setInserts(List<IDataChangeEntry> inserts)
	{
		this.inserts = inserts;
	}

	@Override
	public List<IDataChangeEntry> getAll()
	{
		if (all == null)
		{
			int size = inserts.size() + updates.size() + deletes.size();
			List<IDataChangeEntry> allList;
			if (size > 0)
			{
				allList = new ArrayList<IDataChangeEntry>(size);
				allList.addAll(inserts);
				allList.addAll(updates);
				allList.addAll(deletes);
			}
			else
			{
				allList = Collections.emptyList();
			}
			all = allList;
		}
		return all;
	}

	@Override
	public boolean isEmpty()
	{
		return deletes.size() == 0 && updates.size() == 0 && inserts.size() == 0;
	}

	@Override
	public boolean isEmptyByType(Class<?> entityType)
	{
		List<IDataChangeEntry> entries = inserts;
		for (int a = entries.size(); a-- > 0;)
		{
			if (entries.get(a).getEntityType().isAssignableFrom(entityType))
			{
				return false;
			}
		}
		entries = updates;
		for (int a = entries.size(); a-- > 0;)
		{
			if (entries.get(a).getEntityType().isAssignableFrom(entityType))
			{
				return false;
			}
		}
		entries = deletes;
		for (int a = entries.size(); a-- > 0;)
		{
			if (entries.get(a).getEntityType().isAssignableFrom(entityType))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isLocalSource()
	{
		return isLocalSource;
	}

	public void setLocalSource(boolean isLocalSource)
	{
		this.isLocalSource = isLocalSource;
	}

	@Override
	public IDataChange derive(Class<?>... interestedEntityTypes)
	{
		return deriveIntern(interestedEntityTypes, false);
	}

	@Override
	public IDataChange deriveNot(Class<?>... uninterestingEntityTypes)
	{
		return deriveIntern(uninterestingEntityTypes, true);
	}

	@Override
	public IDataChange derive(Object... interestedEntityIds)
	{
		Set<Object> interestedEntityIdsSet = interestedEntityIds.length == 0 ? Collections.<Object> emptySet() : new HashSet<Object>(interestedEntityIds);

		List<IDataChangeEntry> derivedInserts = buildDerivedIds(inserts, interestedEntityIdsSet);
		List<IDataChangeEntry> derivedUpdates = buildDerivedIds(updates, interestedEntityIdsSet);
		List<IDataChangeEntry> derivedDeletes = buildDerivedIds(deletes, interestedEntityIdsSet);

		return new DataChangeEvent(derivedInserts, derivedUpdates, derivedDeletes, changeTime, isLocalSource);
	}

	protected IDataChange deriveIntern(Class<?>[] entityTypes, boolean reverse)
	{
		Set<Class<?>> entityTypesSet = entityTypes.length == 0 ? Collections.<Class<?>> emptySet() : new HashSet<Class<?>>(entityTypes);

		List<IDataChangeEntry> derivedInserts = buildDerivedTypes(inserts, entityTypesSet, reverse);
		List<IDataChangeEntry> derivedUpdates = buildDerivedTypes(updates, entityTypesSet, reverse);
		List<IDataChangeEntry> derivedDeletes = buildDerivedTypes(deletes, entityTypesSet, reverse);

		return new DataChangeEvent(derivedInserts, derivedUpdates, derivedDeletes, changeTime, isLocalSource);
	}

	protected List<IDataChangeEntry> buildDerivedTypes(List<IDataChangeEntry> sourceEntries, Set<Class<?>> entityTypes, boolean reverse)
	{
		List<IDataChangeEntry> targetEntries = null;
		for (int a = 0, size = sourceEntries.size(); a < size; a++)
		{
			IDataChangeEntry entry = sourceEntries.get(a);
			Class<?> currentType = entry.getEntityType();
			while (currentType != null)
			{
				boolean include = entityTypes.contains(currentType) ^ reverse;
				if (include)
				{
					if (targetEntries == null)
					{
						targetEntries = new ArrayList<IDataChangeEntry>(size - a); // Max potential match-count
					}
					targetEntries.add(entry);
					break;
				}
				currentType = currentType.getSuperclass();
				if (reverse && currentType == Object.class)
				{
					break;
				}
			}
		}
		if (targetEntries == null)
		{
			targetEntries = Collections.emptyList();
		}
		return targetEntries;
	}

	protected List<IDataChangeEntry> buildDerivedIds(List<IDataChangeEntry> sourceEntries, Set<Object> interestedEntityIds)
	{
		List<IDataChangeEntry> targetEntries = null;
		for (int a = 0, size = sourceEntries.size(); a < size; a++)
		{
			IDataChangeEntry entry = sourceEntries.get(a);
			if (interestedEntityIds.contains(entry.getId()))
			{
				if (targetEntries == null)
				{
					targetEntries = new ArrayList<IDataChangeEntry>(size - a); // Max potential match-count
				}
				targetEntries.add(entry);
			}
		}
		if (targetEntries == null)
		{
			targetEntries = Collections.emptyList();
		}
		return targetEntries;
	}
}
