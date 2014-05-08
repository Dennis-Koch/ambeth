package de.osthus.ambeth.merge.model;

import java.util.List;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IOriCollection
{
	List<IObjRef> getAllChangeORIs();

	List<IObjRef> getChangeRefs(Class<?> type);

	Long getChangedOn();

	String getChangedBy();
}
