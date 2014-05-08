package de.osthus.ambeth.merge.model;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface ICUDResult
{
	List<IChangeContainer> getAllChanges();

	List<IChangeContainer> getChanges(Class<?> type);

	List<Object> getOriginalRefs();
}
