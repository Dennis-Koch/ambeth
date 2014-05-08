package de.osthus.ambeth.datachange.model;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IDataChange
{
	long getChangeTime();

	List<IDataChangeEntry> getAll();

	List<IDataChangeEntry> getDeletes();

	List<IDataChangeEntry> getUpdates();

	List<IDataChangeEntry> getInserts();

	boolean isEmpty();

	boolean isLocalSource();

	IDataChange derive(Class<?>... interestedEntityTypes);

	IDataChange deriveNot(Class<?>... uninterestingEntityTypes);

	IDataChange derive(Object... interestedEntityIds);
}